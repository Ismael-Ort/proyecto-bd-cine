# Validaciones que serán manejadas en Java

Este documento describe las validaciones que serán implementadas desde la aplicación Java y no directamente mediante restricciones del modelo físico de la base de datos.

El modelo físico garantiza la integridad mediante claves primarias, claves foráneas, restricciones `NOT NULL`, `UNIQUE`, `CHECK` y valores por defecto. Además, `database/procedimientos_triggers.sql` agrega procedimientos almacenados, funciones y triggers que cubren varias reglas que antes se planeaban resolver solo en Java (cálculo de montos, disponibilidad de butaca, acumulación de puntos, transacción de la venta). Ese archivo está explicado en detalle en **`docs/procedimientos_bd.md`**; léelo primero si no lo has hecho.

> Regla general: la aplicación **no** debe insertar/actualizar `venta`, `entrada` ni `historial_puntos` con SQL suelto. Debe llamar siempre a los procedimientos de `database/procedimientos_triggers.sql` mediante `CallableStatement`. Lo que queda documentado aquí es lo que esos procedimientos **no** resuelven (elegir butacas en pantalla, validar formularios, manejar sesiones, etc.).

---

## 1. Disponibilidad de butacas (para mostrar en pantalla)

La verificación **definitiva** de disponibilidad ya no depende de una consulta previa en Java: `sp_registrar_venta_simple` y `sp_agregar_entrada_a_venta` bloquean la fila con `SELECT ... FOR UPDATE` y rechazan la operación (`SIGNAL`) si la butaca ya está `RESERVADA`, `PAGADA` o `UTILIZADA` para esa función. Aun así, Java necesita una consulta de solo lectura para **pintar el mapa de butacas** antes de que el usuario elija una:

```sql
SELECT b.id_butaca, b.fila, b.numero,
       (e.id_entrada IS NOT NULL AND e.estado IN ('RESERVADA','PAGADA','UTILIZADA')) AS ocupada
FROM butaca b
LEFT JOIN entrada e ON e.id_butaca = b.id_butaca AND e.id_funcion = ?
WHERE b.id_sala = ? AND b.estado = 'ACTIVA';
```

```java
public List<ButacaDisponibilidad> obtenerMapaDeButacas(int idSala, int idFuncion)
```

Como puede haber una carrera entre que Java consulta el mapa y el usuario confirma la compra, el chequeo real sigue siendo el de la base de datos: si dos personas eligen la misma butaca casi al mismo tiempo, una de las dos llamadas a `sp_registrar_venta_simple`/`sp_agregar_entrada_a_venta` fallará con el mensaje "La butaca ya esta ocupada para esta funcion." Java debe capturar ese error y refrescar el mapa.

---

## 2. Validación de pertenencia de la butaca

Java debe comprobar que la butaca pertenezca a la sala donde se realizará la función (esto se puede resolver con el mismo `JOIN` de la sección 1, filtrando por `b.id_sala = funcion.id_sala`).

```java
public boolean butacaPerteneceASalaFuncion(int idFuncion, int idButaca)
```

---

## 3. Validación de choque de horarios

Antes de registrar una función se debe verificar que no exista otra función en la misma sala cuyo horario se solape. No hay forma de expresar esto con un `CHECK` de una sola fila, así que sigue siendo responsabilidad de Java.

```java
public boolean existeChoqueHorario(int idSala, LocalDate fecha, LocalTime horaInicio, int duracionMinutosPelicula)
```

---

## 4. Cálculo de montos (ahora lo hace la base de datos)

`sp_registrar_venta_simple` y `sp_agregar_entrada_a_venta` calculan automáticamente, dentro de la transacción:

- `precio_base` = `funcion.tarifa_base`.
- `descuento` = `precio_base * tipoentrada.descuento_porcentaje / 100`.
- `precio_final` = `precio_base - descuento`.
- `venta.total_pagado` = suma de `precio_final` de las entradas no canceladas de la venta.

Java **no** necesita reimplementar estas fórmulas para guardar datos; solo puede usarlas para **previsualizar** el precio en pantalla antes de confirmar la compra (por ejemplo, al mostrar el precio antes de llamar al procedimiento):

```java
public BigDecimal previsualizarPrecioFinal(BigDecimal tarifaBase, BigDecimal descuentoPorcentaje)
```

---

## 5. Validación del programa de fidelidad

El saldo de puntos ya no se calcula sumando manualmente en Java: existe la función `fn_puntos_disponibles(id_cliente)` en la base de datos.

```java
public int calcularPuntosDisponibles(int idCliente) {
    // SELECT fn_puntos_disponibles(?)
}

public boolean puedeCanjearEntradaGratis(int idCliente) {
    return calcularPuntosDisponibles(idCliente) >= 9;
}
```

Cuando el cliente tenga 9 puntos o más y decida canjear, Java llama a `sp_canjear_puntos` (ver `docs/procedimientos_bd.md`, sección 4.5); no hay que construir el `INSERT` a mano.

---

## 6. Registro de movimientos de puntos (automático)

Java **ya no inserta manualmente** movimientos `ACUMULACIÓN`: el trigger `trg_entrada_au_acumula_puntos` los genera solo cuando `sp_marcar_entrada_pagada` deja una entrada en estado `PAGADA` con `precio_final > 0`.

Los movimientos `CANJE` los genera `sp_canjear_puntos`.

Lo único que Java debe hacer es, después de una compra, refrescar el saldo (`fn_puntos_disponibles`) para mostrarlo al cliente.

---

## 7. Validación del canal de venta

Los procedimientos ya validan que `TAQUILLA` requiera empleado (y la restricción `chk_venta_canal_empleado` lo respalda), pero Java debe hacer la misma validación **antes** de llamar al procedimiento, para mostrar un mensaje amigable en el formulario en vez de esperar la excepción de la base de datos:

```java
public boolean validarEmpleadoSegunCanal(String canalVenta, Integer idEmpleado)
```

---

## 8. Validación del usuario

Los únicos roles del sistema son:

- ADMINISTRADOR
- CAJERO
- CLIENTE

`usuario` solo tiene una llave foránea a `persona` (`id_persona`), no a `cliente` ni a `empleado` directamente. Por lo tanto, Java deberá verificar la coherencia entre el rol y los registros de esa persona:

- Administrador y Cajero: la persona referenciada debe tener un registro en `empleado`.
- Cliente: la persona referenciada debe tener un registro en `cliente`.

```java
public boolean validarUsuarioSegunRol(...)
```

---

## 9. Registro de personas compartidas

Antes de crear un Cliente o un Empleado, Java debe verificar si la persona (por documento o correo) ya existe en `persona` para reutilizar el mismo `id_persona` en lugar de duplicarla. También debe impedir crear dos registros de Cliente o dos de Empleado para la misma persona (la base de datos ya lo impide con `uq_cliente_persona` y `uq_empleado_persona`, pero Java debe mostrar un mensaje claro en vez de dejar que falle la restricción).

```java
public Persona buscarOPersonaPorDocumento(String documento)
```

---

## 10. Validación de estados

Antes de vender una entrada, parte de esto ya lo cubre la base de datos y parte sigue siendo de Java:

| Validación | Dónde se aplica |
|---|---|
| Función no cancelada/finalizada | `trg_entrada_bi_valida_funcion` / `trg_entrada_bu_valida_reventa` (DB) |
| Butaca activa | `trg_entrada_bi_valida_funcion` / `trg_entrada_bu_valida_reventa` (DB) |
| Película activa | Java |
| Sala activa | Java |
| Método de pago activo | Java |
| Tipo de entrada activo | Java |

Java debe seguir validando película, sala, método de pago y tipo de entrada activos **antes** de llamar al procedimiento correspondiente, para dar un mensaje de error más específico que un `SIGNAL` genérico de la base de datos.

---

## 11. Venta como transacción

La transacción principal (crear la venta y su primera entrada, o agregar una entrada a una venta existente) ya está implementada **dentro** de cada procedimiento (`START TRANSACTION` / `COMMIT` / `ROLLBACK`, ver `docs/procedimientos_bd.md`). Java solo necesita envolver en su propia transacción JDBC la **secuencia** de llamadas cuando una compra incluye varias butacas, para poder deshacer todo si el usuario cancela a mitad de camino:

```java
connection.setAutoCommit(false);
try (CallableStatement cs = connection.prepareCall(
        "{call sp_registrar_venta_simple(?,?,?,?,?,?,?,?,?,?,?)}")) {
    // ... setear parametros de la primera butaca ...
    cs.execute();
    int idVenta = cs.getInt(10);

    // por cada butaca adicional:
    // CallableStatement cs2 = connection.prepareCall("{call sp_agregar_entrada_a_venta(?,?,?,?,?,?)}");

    connection.commit();
} catch (SQLException e) {
    connection.rollback();
    throw e;
}
```

---

## 12. Resumen de responsabilidades

| Regla | Base de datos | Java |
|-------|---------------|------|
| Claves primarias / foráneas | ✔ | |
| UNIQUE (incluye `documento`/`correo` de Persona) | ✔ | |
| CHECK (estados, rangos, canal-empleado, precio final) | ✔ | |
| Cálculo de montos (`precio_final`, `total_pagado`) | ✔ (procedimientos) | Solo previsualización |
| Exclusividad y reventa de butaca | ✔ (procedimientos + `FOR UPDATE`) | Solo mapa de disponibilidad |
| Función/butaca activa antes de vender | ✔ (triggers) | |
| Acumulación automática de puntos | ✔ (trigger) | Solo mostrar saldo |
| Cierre automático de la venta (`COMPLETADA`/`CANCELADA`) | ✔ (trigger) | |
| Canje de puntos | ✔ (procedimiento) | Validación previa amigable |
| Transacción de cada operación individual | ✔ (procedimientos) | Transacción JDBC si son varias llamadas |
| Choque de horarios entre funciones | | ✔ |
| Película/sala/método de pago/tipo de entrada activos | | ✔ |
| Coherencia rol de Usuario ↔ Cliente/Empleado (vía Persona) | | ✔ |
| Persona única para Cliente/Empleado | ✔ (`uq_cliente_persona`, `uq_empleado_persona`) | ✔ (mensaje amigable) |

---

## Conclusión

El modelo físico y los procedimientos/triggers de `database/procedimientos_triggers.sql` concentran las reglas dinámicas más críticas del negocio (montos, disponibilidad de butacas, acumulación de puntos, transacciones). Java se enfoca en las validaciones que solo tienen sentido en la capa de aplicación (interfaz, mensajes amigables antes de tocar la base de datos, choque de horarios) y en invocar los procedimientos con `CallableStatement` dentro de sus propias transacciones cuando encadena varias llamadas. Esta separación mantiene el diseño de la base de datos limpio, consistente y fácil de mantener.

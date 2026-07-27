# Validaciones que serán manejadas en Java

Este documento describe las validaciones que serán implementadas desde la aplicación Java y no directamente mediante restricciones del modelo físico de la base de datos.

El modelo físico garantiza la integridad mediante claves primarias, claves foráneas, restricciones `NOT NULL`, `UNIQUE`, `CHECK` y valores por defecto. Además, `database/procedimientos_triggers.sql` agrega un trigger y dos procedimientos almacenados que cubren algunas reglas que antes se planeaban resolver solo en Java (cálculo de montos, exclusividad de butaca, acumulación de puntos, transacción de la venta). Ese archivo se ejecuta aparte (no al crear la base de datos) y está explicado en detalle en **`docs/procedimientos_bd.md`**; léelo primero si no lo has hecho. No se usan funciones almacenadas en la base de datos (no se vieron en clase), así que cualquier cálculo de solo lectura (por ejemplo, el saldo de puntos) se resuelve con una consulta SQL normal desde Java.

> Regla general: la aplicación **no** debe insertar/actualizar `venta` ni `entrada` con SQL suelto. Debe llamar siempre a `sp_registrar_venta` y `sp_confirmar_pago` mediante `CallableStatement`. Lo que queda documentado aquí es lo que esos procedimientos **no** resuelven (elegir butacas en pantalla, validar formularios, choque de horarios, etc.).

---

## 1. Disponibilidad de butacas (para mostrar en pantalla)

La verificación **definitiva** de disponibilidad ya no depende de una consulta previa en Java: `sp_registrar_venta` bloquea la fila con `SELECT ... FOR UPDATE` y rechaza la operación (`SIGNAL`) si la butaca ya está `RESERVADA`, `PAGADA` o `UTILIZADA` para esa función. Aun así, Java necesita una consulta de solo lectura para **pintar el mapa de butacas** antes de que el usuario elija una:

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

Como puede haber una carrera entre que Java consulta el mapa y el usuario confirma la compra, el chequeo real sigue siendo el de la base de datos: si dos personas eligen la misma butaca casi al mismo tiempo, una de las dos llamadas a `sp_registrar_venta` fallará con el mensaje "La butaca ya esta ocupada para esta funcion." Java debe capturar ese error y refrescar el mapa.

---

## 2. Validación de pertenencia de la butaca

Java debe comprobar que la butaca pertenezca a la sala donde se realizará la función (esto se puede resolver con el mismo `JOIN` de la sección 1, filtrando por `b.id_sala = funcion.id_sala`).

```java
public boolean butacaPerteneceASalaFuncion(int idFuncion, int idButaca)
```

---

## 3. Validación de choque de horarios

Antes de registrar una función se debe verificar que no exista otra función en la misma sala cuyo horario (`hora_inicio`/`hora_fin`) se solape. No hay forma de expresar esto con un `CHECK` de una sola fila, así que sigue siendo responsabilidad de Java.

```java
public boolean existeChoqueHorario(int idSala, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin)
```

---

## 4. Cálculo de montos (ahora lo hace la base de datos)

`sp_registrar_venta` calcula automáticamente, dentro de su transacción:

- `precio_base` = `funcion.tarifa_base`.
- `descuento` = `precio_base * tipoentrada.descuento_porcentaje / 100`.
- `precio_final` = `precio_base - descuento`.
- `venta.total_pagado` = `precio_final` de la entrada.

Java **no** necesita reimplementar estas fórmulas para guardar datos; solo puede usarlas para **previsualizar** el precio en pantalla antes de confirmar la compra:

```java
public BigDecimal previsualizarPrecioFinal(BigDecimal tarifaBase, BigDecimal descuentoPorcentaje)
```

---

## 5. Validación del programa de fidelidad

No hay una función almacenada para esto: Java calcula el saldo de puntos con una consulta normal.

```java
public int calcularPuntosDisponibles(int idCliente) {
    String sql = "SELECT COALESCE(SUM(" +
        "CASE WHEN tipo_movimiento = 'ACUMULACIÓN' THEN cantidad_puntos ELSE -cantidad_puntos END" +
        "), 0) FROM historial_puntos WHERE id_cliente = ?";
    // ejecutar y devolver el resultado
}

public boolean puedeCanjearEntradaGratis(int idCliente) {
    return calcularPuntosDisponibles(idCliente) >= 9;
}
```

Cuando el cliente tenga 9 puntos o más y decida canjear, Java llama a `sp_registrar_venta` usando un `tipoentrada` con `descuento_porcentaje = 100` (ver `docs/procedimientos_bd.md`, sección 4); no existe un procedimiento aparte para el canje.

---

## 6. Registro de movimientos de puntos

Java **ya no inserta manualmente** movimientos `ACUMULACIÓN`: el trigger `trg_acumula_puntos` los genera solo cuando `sp_confirmar_pago` deja una entrada en estado `PAGADA` con `precio_final > 0`.

Los movimientos `CANJE` sí los inserta Java directamente (es un solo `INSERT`, no hace falta un procedimiento):

```sql
INSERT INTO historial_puntos (tipo_movimiento, cantidad_puntos, descripcion, id_cliente, id_venta)
VALUES ('CANJE', 9, 'Canje de puntos por entrada gratuita', ?, ?);
```

Java debe llamar a `calcularPuntosDisponibles` (sección 5) **antes** de permitir el canje, para no depender de una restricción de la base de datos que valide el saldo (no existe: sumar/restar entre filas no se puede expresar con un `CHECK`).

---

## 7. Validación del canal de venta

`sp_registrar_venta` ya valida que `TAQUILLA` requiera empleado (y la restricción `chk_venta_canal_empleado` lo respalda), pero Java debe hacer la misma validación **antes** de llamar al procedimiento, para mostrar un mensaje amigable en el formulario en vez de esperar la excepción de la base de datos:

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

Al registrar una persona, Java también debe pedir `fecha_nacimiento` y `sexo` (`M` o `F`), que ahora son obligatorios.

```java
public Persona buscarOPersonaPorDocumento(String documento)
```

---

## 10. Validación de estados

Antes de vender una entrada, parte de esto ya lo cubre la base de datos y parte sigue siendo de Java:

| Validación | Dónde se aplica |
|---|---|
| Función no cancelada/finalizada | `sp_registrar_venta` (DB) |
| Película activa | Java |
| Sala activa | Java |
| Butaca activa | Java |
| Método de pago activo | Java |
| Tipo de entrada activo | Java |

Java debe seguir validando película, sala, butaca, método de pago y tipo de entrada activos **antes** de llamar al procedimiento correspondiente, para dar un mensaje de error más específico que un `SIGNAL` genérico de la base de datos.

---

## 11. Venta como transacción

Cada llamada a `sp_registrar_venta` o a `sp_confirmar_pago` ya es, por sí sola, una transacción completa (`START TRANSACTION` / `COMMIT` / `ROLLBACK` viven dentro del procedimiento, ver `docs/procedimientos_bd.md`). Java no necesita abrir su propia transacción JDBC solo para una llamada:

```java
try (CallableStatement cs = connection.prepareCall(
        "{call sp_registrar_venta(?,?,?,?,?,?,?,?,?,?)}")) {
    cs.setInt(1, idCliente);
    cs.setNull(2, Types.INTEGER); // sin empleado (EN_LINEA)
    cs.setString(3, "EN_LINEA");
    cs.setInt(4, idMetodoPago);
    cs.setString(5, observacion);
    cs.setInt(6, idFuncion);
    cs.setInt(7, idButaca);
    cs.setInt(8, idTipoEntrada);
    cs.registerOutParameter(9, Types.INTEGER);
    cs.registerOutParameter(10, Types.INTEGER);
    cs.execute();

    int idVenta = cs.getInt(9);
    int idEntrada = cs.getInt(10);
} catch (SQLException e) {
    // el procedimiento ya hizo ROLLBACK internamente; aquí solo se
    // captura el mensaje para mostrarlo al usuario
    throw e;
}
```

Si más adelante se agrega una venta con varias entradas (llamando a `sp_registrar_venta` varias veces seguidas para la misma venta), ahí sí conviene envolver esa secuencia de llamadas en `connection.setAutoCommit(false)` / `commit()` / `rollback()`.

---

## 12. Resumen de responsabilidades

| Regla | Base de datos | Java |
|-------|---------------|------|
| Claves primarias / foráneas | ✔ | |
| UNIQUE (incluye `documento`/`correo` de Persona) | ✔ | |
| CHECK (estados, rangos, canal-empleado, precio final, horario de función, sexo) | ✔ | |
| Cálculo de montos (`precio_final`, `total_pagado`) | ✔ (`sp_registrar_venta`) | Solo previsualización |
| Exclusividad y reventa de butaca | ✔ (`sp_registrar_venta` + `FOR UPDATE`) | Solo mapa de disponibilidad |
| Función activa antes de vender | ✔ (`sp_registrar_venta`) | |
| Acumulación automática de puntos | ✔ (`trg_acumula_puntos`) | Solo mostrar saldo |
| Cierre de la venta a `COMPLETADA` | ✔ (`sp_confirmar_pago`) | |
| Canje de puntos | Parcial (`sp_registrar_venta` con tipo 100% descuento) | Validar saldo + `INSERT` del `CANJE` |
| Transacción de cada operación individual | ✔ (procedimientos) | Transacción JDBC solo si son varias llamadas |
| Choque de horarios entre funciones | | ✔ |
| Película/sala/butaca/método de pago/tipo de entrada activos | | ✔ |
| Coherencia rol de Usuario ↔ Cliente/Empleado (vía Persona) | | ✔ |
| Persona única para Cliente/Empleado | ✔ (`uq_cliente_persona`, `uq_empleado_persona`) | ✔ (mensaje amigable) |

---

## Conclusión

El modelo físico y los objetos de `database/procedimientos_triggers.sql` (un trigger y dos procedimientos, ambos transaccionales) concentran las reglas dinámicas más críticas del negocio: montos, exclusividad de butacas, acumulación de puntos. Java se enfoca en las validaciones que solo tienen sentido en la capa de aplicación (interfaz, mensajes amigables antes de tocar la base de datos, choque de horarios, saldo de puntos) y en invocar los procedimientos con `CallableStatement`. Esta separación mantiene el diseño de la base de datos simple, consistente y fácil de mantener por un equipo que recién está aprendiendo.

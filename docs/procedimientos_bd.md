# Procedimientos, Funciones, Triggers y Transacciones

Este documento explica, paso a paso, los objetos de programación que agrega `database/procedimientos_triggers.sql` sobre el esquema de `database/schema.sql`. La idea es que cualquiera del equipo pueda leer este documento, entender **por qué** existe cada objeto y **cómo probarlo**, sin depender de que alguien más (o una IA) se lo explique de nuevo.

El proyecto requiere, como mínimo, un procedimiento almacenado, un trigger y una transacción. Aquí se implementan **2 funciones, 3 triggers y 5 procedimientos**, y **todos los procedimientos que escriben datos usan una transacción explícita** (`START TRANSACTION` / `COMMIT` / `ROLLBACK`).

---

## 0. Orden de ejecución

1. `database/schema.sql` (crea las tablas).
2. `database/procedimientos_triggers.sql` (crea funciones, triggers y procedimientos).

Si tu cliente de base de datos (DBeaver, HeidiSQL, la extensión "Database Client" de VS Code, `mysql` por consola, etc.) **no entiende el comando `DELIMITER`**, ejecuta cada bloque `CREATE FUNCTION` / `CREATE TRIGGER` / `CREATE PROCEDURE` por separado (selecciona el bloque completo, desde `CREATE` hasta el `END` final, y ejecútalo solo). Con el cliente de línea de comandos `mysql` puedes ejecutar el archivo completo tal cual:

```bash
mysql -h <host> -P <puerto> -u <usuario> -p cine < database/schema.sql
mysql -h <host> -P <puerto> -u <usuario> -p cine < database/procedimientos_triggers.sql
```

---

## 1. ¿Por qué existen estos objetos? (mapa a las reglas de negocio)

| Objeto | Tipo | Regla de negocio que resuelve |
|---|---|---|
| `fn_funcion_disponible` | Función | Apoyo de BR-09/validación de estados |
| `fn_puntos_disponibles` | Función | BR-30, BR-31 (saldo de puntos) |
| `trg_entrada_bi_valida_funcion` | Trigger | No vender entradas de funciones canceladas/finalizadas o butacas inactivas |
| `trg_entrada_bu_valida_reventa` | Trigger | Misma validación, pero al revender una butaca que estaba `CANCELADA` |
| `trg_entrada_au_acumula_puntos` | Trigger | BR-28 (acumulación automática), BR-29 (gratis no acumula) y cierre automático de la venta |
| `sp_registrar_venta_simple` | Procedimiento + Transacción | BR-35 (venta como transacción única), BR-20 (cálculo del monto) |
| `sp_agregar_entrada_a_venta` | Procedimiento + Transacción | BR-19 (venta con múltiples entradas) |
| `sp_marcar_entrada_pagada` | Procedimiento + Transacción | Cambia estado de entrada; dispara la acumulación de puntos |
| `sp_cancelar_entrada` | Procedimiento + Transacción | BR-24 (liberación de butacas) |
| `sp_canjear_puntos` | Procedimiento + Transacción | BR-29, BR-31 (canje de puntos por entrada gratuita) |

---

## 2. Funciones

### 2.1. `fn_funcion_disponible(p_id_funcion INT) RETURNS BOOLEAN`

Devuelve `TRUE` si la función todavía admite ventas, es decir, si su `estado` es `PROGRAMADA` o `EN_CURSO` (no `FINALIZADA` ni `CANCELADA`).

```sql
SELECT fn_funcion_disponible(3);
```

> Nota técnica: se declaró como `DETERMINISTIC` a propósito, aunque su resultado puede cambiar si el estado de la función cambia en la base de datos. Esto es una convención común para funciones de solo lectura: evita el error `binary logging is enabled` que MySQL lanza al crear funciones en servidores gestionados (como Aiven) que tienen el binary log activado. La función no escribe datos, así que no hay riesgo real de inconsistencia en la réplica.

### 2.2. `fn_puntos_disponibles(p_id_cliente INT) RETURNS INT`

Calcula el saldo de puntos de fidelidad de un cliente: suma los movimientos `ACUMULACIÓN` y resta los `CANJE` de `historial_puntos`.

```sql
SELECT fn_puntos_disponibles(5);
```

Java puede usar esta misma consulta (`SELECT fn_puntos_disponibles(?)`) en lugar de reimplementar la suma en la aplicación (ver `docs/validaciones_en_java.md`, sección 6).

---

## 3. Triggers

### 3.1. `trg_entrada_bi_valida_funcion` (`BEFORE INSERT ON entrada`)

Antes de insertar una entrada nueva, verifica:
- que `fn_funcion_disponible(NEW.id_funcion)` sea verdadero;
- que la butaca (`NEW.id_butaca`) esté en estado `ACTIVA`.

Si algo falla, lanza un error personalizado con `SIGNAL SQLSTATE '45000'` y el `INSERT` completo se cancela.

### 3.2. `trg_entrada_bu_valida_reventa` (`BEFORE UPDATE ON entrada`)

La tabla `entrada` tiene la restricción `uq_entrada_funcion_butaca` (única por `id_funcion, id_butaca`). Esto significa que **una butaca cancelada para una función no se vuelve a insertar como fila nueva: se reutiliza la misma fila** (se hace `UPDATE`, no `INSERT`). Este trigger repite, en ese caso puntual (`OLD.estado = 'CANCELADA'` y `NEW.estado` cambia a otra cosa), las mismas validaciones que `trg_entrada_bi_valida_funcion`.

### 3.3. `trg_entrada_au_acumula_puntos` (`AFTER UPDATE ON entrada`)

Tiene dos responsabilidades:

1. **Acumulación automática de puntos (BR-28/BR-29):** si `NEW.estado = 'PAGADA'` y antes no lo estaba, y el `precio_final` es mayor que cero (o sea, no es una entrada gratuita), inserta un movimiento `ACUMULACIÓN` de 1 punto en `historial_puntos`, asociado al cliente y a la venta.
2. **Cierre automático de la venta:** cada vez que una entrada cambia a un estado final (`PAGADA`, `UTILIZADA` o `CANCELADA`), revisa si **todas** las entradas de esa venta ya están en un estado final. Si es así, marca la venta como `COMPLETADA` (si al menos una entrada fue pagada o utilizada) o como `CANCELADA` (si todas las entradas de la venta terminaron canceladas).

Esto significa que la aplicación Java **no necesita** actualizar manualmente `historial_puntos` ni el estado de `venta` cuando se paga o cancela una entrada: solo llama a `sp_marcar_entrada_pagada` o `sp_cancelar_entrada`, y el trigger hace el resto.

---

## 4. Procedimientos almacenados (todos son transacciones)

Los cinco procedimientos siguen el mismo patrón:

```sql
CREATE PROCEDURE sp_algo(...)
BEGIN
    DECLARE ... ;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- validaciones que no requieren bloquear filas
    START TRANSACTION;
    -- lecturas con FOR UPDATE + escrituras
    COMMIT;
END
```

Si cualquier sentencia dentro de la transacción falla (una restricción `CHECK`, una `SIGNAL`, una llave foránea inexistente, etc.), el `EXIT HANDLER` ejecuta `ROLLBACK` y vuelve a lanzar el error original con `RESIGNAL`, para que la aplicación Java se entere de qué salió mal. Esto cumple **BR-35: "el registro de una venta deberá ejecutarse como una única transacción; si ocurre un error, todas las operaciones deben revertirse."**

> Gotcha clásico de MySQL: si un parámetro o variable local se llama igual que una columna (por ejemplo, un parámetro `estado`), dentro de un `WHERE estado = estado` MySQL no siempre sabe si te refieres a la columna o al parámetro, y el resultado puede ser silenciosamente incorrecto. Por eso **todos** los parámetros usan el prefijo `p_` y las variables locales `v_`: nunca coinciden con un nombre de columna real.

### 4.1. `sp_registrar_venta_simple`

Registra una venta con su **primera** entrada. Calcula el precio automáticamente a partir de `funcion.tarifa_base` y `tipoentrada.descuento_porcentaje` (BR-26), en lugar de recibirlo como parámetro.

```sql
CALL sp_registrar_venta_simple(
    5,              -- p_id_cliente
    NULL,           -- p_id_empleado (NULL porque es EN_LINEA)
    'EN_LINEA',     -- p_canal_venta
    2,              -- p_id_metodopago
    'Compra desde la web', -- p_observacion
    3,              -- p_id_funcion
    12,             -- p_id_butaca
    1,              -- p_id_tipoentrada
    NULL,           -- p_motivo
    @id_venta,      -- OUT
    @id_entrada     -- OUT
);
SELECT @id_venta, @id_entrada;
```

Qué hace, en orden:
1. Valida que si `canal_venta = 'TAQUILLA'` haya un empleado.
2. Busca la tarifa de la función y el descuento del tipo de entrada; si alguno no existe, aborta con un mensaje claro.
3. Calcula `precio_base`, `descuento` y `precio_final`.
4. Abre la transacción, crea la fila de `venta` con `total_pagado = 0`.
5. Bloquea (`FOR UPDATE`) la fila de `entrada` para esa `(id_funcion, id_butaca)`, si existe. Si existe y no está `CANCELADA`, aborta ("butaca ya ocupada"). Si no existe, la inserta; si existe y está `CANCELADA`, la reutiliza con `UPDATE`.
6. Actualiza `venta.total_pagado` con el precio final calculado.
7. `COMMIT`.

### 4.2. `sp_agregar_entrada_a_venta`

Agrega otra entrada a una venta que ya existe y sigue `PENDIENTE` (para vender varias butacas en la misma compra, BR-19). Al final, recalcula `total_pagado` sumando **todas** las entradas no canceladas de esa venta.

```sql
CALL sp_agregar_entrada_a_venta(
    @id_venta,      -- la venta abierta por sp_registrar_venta_simple
    3,              -- p_id_funcion (debe ser la misma función)
    13,             -- p_id_butaca (otra butaca)
    1,              -- p_id_tipoentrada
    NULL,           -- p_motivo
    @id_entrada_2   -- OUT
);
```

### 4.3. `sp_marcar_entrada_pagada`

Confirma el pago de una entrada `RESERVADA`. Dispara `trg_entrada_au_acumula_puntos`.

```sql
CALL sp_marcar_entrada_pagada(@id_entrada);
```

### 4.4. `sp_cancelar_entrada`

Cancela una entrada (libera la butaca para esa función) y recalcula `total_pagado` de la venta. También dispara `trg_entrada_au_acumula_puntos`, que puede cerrar la venta como `CANCELADA` si todas sus entradas terminan canceladas.

```sql
CALL sp_cancelar_entrada(@id_entrada);
```

### 4.5. `sp_canjear_puntos`

Implementa el canje de puntos de fidelidad (9 puntos = 1 entrada gratis). Valida el saldo con `fn_puntos_disponibles`, crea la venta y la entrada gratuita (`precio_final = 0`), marca la venta como `COMPLETADA` y registra el movimiento `CANJE` de 9 puntos.

```sql
CALL sp_canjear_puntos(
    5,              -- p_id_cliente
    2,              -- p_id_empleado
    'TAQUILLA',     -- p_canal_venta
    1,              -- p_id_metodopago
    3,              -- p_id_funcion
    14,             -- p_id_butaca
    1,              -- p_id_tipoentrada
    @id_venta_gratis,
    @id_entrada_gratis
);
```

---

## 5. Cómo probar cada objeto manualmente

Antes de conectar la aplicación Java, prueba cada objeto directamente en la base de datos. Sugerencia de guion de pruebas (asumiendo que ya insertaste datos base: al menos una película, sala, butacas, función, tipo de entrada, método de pago, persona+cliente y persona+empleado):

1. **Venta simple:** llama a `sp_registrar_venta_simple` y confirma con `SELECT * FROM venta; SELECT * FROM entrada;` que los montos son correctos.
2. **Butaca duplicada:** vuelve a llamar a `sp_registrar_venta_simple` con la **misma** `id_funcion`/`id_butaca` y confirma que lanza el error "La butaca ya esta ocupada para esta funcion."
3. **Venta con varias entradas:** usa `sp_agregar_entrada_a_venta` sobre la venta anterior con otra butaca y revisa que `venta.total_pagado` sume ambas entradas.
4. **Pago y acumulación:** llama a `sp_marcar_entrada_pagada` y verifica con `SELECT * FROM historial_puntos WHERE id_venta = @id_venta;` que se creó un movimiento `ACUMULACIÓN`.
5. **Cancelación y reventa:** llama a `sp_cancelar_entrada` sobre una entrada `RESERVADA`, confirma que su estado pasa a `CANCELADA`, y luego llama de nuevo a `sp_registrar_venta_simple` (o `sp_agregar_entrada_a_venta`) con la misma butaca/función: debe reutilizar la fila (mismo `id_entrada`) en lugar de fallar por duplicado.
6. **Función inactiva:** cambia el estado de una función a `CANCELADA` (`UPDATE funcion SET estado = 'CANCELADA' WHERE id_funcion = ...`) e intenta vender una entrada para ella: debe fallar por el trigger `trg_entrada_bi_valida_funcion`.
7. **Canje de puntos:** acumula al menos 9 puntos pagando 9 entradas de un mismo cliente y luego llama a `sp_canjear_puntos`; confirma que la entrada resultante tiene `precio_final = 0` y que se registró el movimiento `CANJE`.

---

## 6. Cómo se conecta esto con la aplicación Java

La aplicación **no** debe reconstruir esta lógica con INSERT/UPDATE sueltos: debe invocar estos procedimientos con `CallableStatement`, dentro de su propia transacción JDBC quando encadene varias llamadas (por ejemplo, `sp_registrar_venta_simple` + varias `sp_agregar_entrada_a_venta` antes de mostrarle el resumen al cliente):

```java
connection.setAutoCommit(false);
try (CallableStatement cs = connection.prepareCall(
        "{call sp_registrar_venta_simple(?,?,?,?,?,?,?,?,?,?,?)}")) {
    cs.setInt(1, idCliente);
    cs.setNull(2, Types.INTEGER); // sin empleado (EN_LINEA)
    cs.setString(3, "EN_LINEA");
    cs.setInt(4, idMetodoPago);
    cs.setString(5, observacion);
    cs.setInt(6, idFuncion);
    cs.setInt(7, idButaca);
    cs.setInt(8, idTipoEntrada);
    cs.setNull(9, Types.VARCHAR);
    cs.registerOutParameter(10, Types.INTEGER);
    cs.registerOutParameter(11, Types.INTEGER);
    cs.execute();
    int idVenta = cs.getInt(10);
    // ... llamar sp_agregar_entrada_a_venta por cada butaca adicional ...
    connection.commit();
} catch (SQLException e) {
    connection.rollback();
    throw e;
}
```

Ver `docs/validaciones_en_java.md` para el detalle de qué queda del lado de Java (elegir butacas, armar el carrito, mostrar mensajes de error) y qué ya cubre la base de datos (cálculo de montos, exclusividad de butaca, acumulación de puntos, cierre automático de la venta).

---

## 7. Estado del documento

- [x] Objeto por objeto: para qué sirve y qué regla de negocio resuelve.
- [x] Ejemplos de `CALL` para cada procedimiento.
- [x] Guion de pruebas manuales paso a paso.
- [x] Ejemplo de integración con Java (`CallableStatement` + transacción JDBC).
- [ ] Ejecutar el guion de pruebas contra la base de datos real y documentar resultados (Fase 7 de `plan_trabajo.md`).

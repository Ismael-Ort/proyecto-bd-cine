# Procedimientos Almacenados, Trigger y Transacciones

Este documento explica, paso a paso, el archivo `database/procedimientos_triggers.sql`. La idea es que cualquiera del equipo pueda leerlo y entender **por qué** existe cada objeto y **cómo probarlo**, antes de ejecutarlo.

**Este archivo no se ejecuta al crear la base de datos.** `database/schema.sql` (que incluye su propio `DROP DATABASE` / `CREATE DATABASE`) solo crea las tablas. `database/procedimientos_triggers.sql` se ejecuta **aparte**, más adelante, cuando en el programa Java lleguemos a la parte de registrar ventas y confirmar pagos. Así evitamos correr algo que todavía no hemos terminado de entender.

El proyecto pide, como mínimo, un procedimiento almacenado, un trigger y una transacción. Aquí hay exactamente eso — ni más ni menos — porque no vimos funciones almacenadas en clase y no tiene sentido meter cosas de más solo por meterlas:

- **1 trigger:** `trg_acumula_puntos`
- **2 procedimientos almacenados**, cada uno con su propia transacción (`START TRANSACTION` / `COMMIT` / `ROLLBACK`): `sp_registrar_venta` y `sp_confirmar_pago`.

No se usan funciones almacenadas (`CREATE FUNCTION`) en ningún lado.

---

## 0. Orden de ejecución

1. `database/schema.sql` (crea la base de datos `cine` y sus tablas).
2. `database/procedimientos_triggers.sql` — **solo cuando estemos listos para programar esa parte**, no antes.

Si tu cliente de base de datos no entiende el comando `DELIMITER` (algunos clientes gráficos no lo necesitan porque ya saben separar bloques `CREATE TRIGGER`/`CREATE PROCEDURE` por su cuenta), ejecuta cada bloque completo (desde `CREATE` hasta el `END$$` final) por separado. Con el cliente de línea de comandos `mysql` se puede correr el archivo entero tal cual:

```bash
mysql -h <host> -P <puerto> -u <usuario> -p cine < database/procedimientos_triggers.sql
```

---

## 1. ¿Por qué un trigger aquí?

Un trigger es código que la base de datos ejecuta **sola**, automáticamente, cuando pasa algo en una tabla (un `INSERT`, `UPDATE` o `DELETE`). No lo llama Java: se dispara solo.

### `trg_acumula_puntos` (`AFTER UPDATE ON entrada`)

Se ejecuta **después** de cada `UPDATE` sobre la tabla `entrada`. Revisa dos cosas:

1. ¿El estado de la entrada acaba de cambiar a `PAGADA` (antes no lo era)?
2. ¿El `precio_final` es mayor que cero? (si es una entrada gratuita por canje de puntos, no debe generar un punto nuevo).

Si ambas se cumplen, inserta una fila en `historial_puntos` con `tipo_movimiento = 'ACUMULACIÓN'` y `cantidad_puntos = 1`, asociada al cliente y a la venta de esa entrada.

```sql
-- Dentro del trigger, NEW representa la fila DESPUÉS del UPDATE
-- y OLD representa la fila ANTES del UPDATE.
IF NEW.estado = 'PAGADA' AND OLD.estado <> 'PAGADA' AND NEW.precio_final > 0 THEN
    ...
END IF;
```

Gracias a este trigger, el procedimiento `sp_confirmar_pago` (sección 3) **no tiene que insertar el punto manualmente**: le basta con cambiar el estado de la entrada a `PAGADA`, y el trigger hace el resto solo.

**Cómo comprobarlo manualmente**, después de correr `sp_confirmar_pago` sobre una entrada:

```sql
SELECT * FROM historial_puntos WHERE id_venta = @id_venta;
```

Debe aparecer una fila nueva con `ACUMULACIÓN` y `cantidad_puntos = 1`.

---

## 2. ¿Por qué dos procedimientos y no uno solo?

Porque una venta real de cine tiene dos momentos distintos:

1. **Reservar la butaca** (elegir función, butaca y tipo de entrada) → `sp_registrar_venta`.
2. **Confirmar que se pagó** (cuando el cliente paga en caja, o cuando se confirma el pago en línea) → `sp_confirmar_pago`.

Separarlos en dos procedimientos refleja el flujo real y además nos deja un `UPDATE` claro (RESERVADA → PAGADA) para que el trigger de la sección 1 tenga algo a qué reaccionar.

### 2.1. `sp_registrar_venta`

Registra una venta con su entrada. Calcula el precio automáticamente a partir de `funcion.tarifa_base` y `tipoentrada.descuento_porcentaje`, así Java no tiene que repetir esa cuenta.

```sql
CALL sp_registrar_venta(
    5,              -- p_id_cliente
    NULL,           -- p_id_empleado (NULL porque es EN_LINEA)
    'EN_LINEA',     -- p_canal_venta
    2,              -- p_id_metodopago
    'Compra desde la web', -- p_observacion
    3,              -- p_id_funcion
    12,             -- p_id_butaca
    1,              -- p_id_tipoentrada
    @id_venta,      -- OUT
    @id_entrada     -- OUT
);
SELECT @id_venta, @id_entrada;
```

Qué hace, en orden:

1. Si `canal_venta = 'TAQUILLA'`, exige que haya un empleado.
2. Busca la función: si no existe, o si está `CANCELADA`/`FINALIZADA`, aborta con un mensaje claro.
3. Busca el tipo de entrada: si no existe, aborta.
4. Calcula `precio_base`, `descuento` y `precio_final`.
5. Abre la transacción (`START TRANSACTION`), crea la fila de `venta`.
6. Revisa si esa butaca ya tiene una entrada para esa función (`SELECT ... FOR UPDATE`, para evitar que dos personas la reserven al mismo tiempo):
   - Si existe y **no** está `CANCELADA` → aborta ("butaca ya ocupada").
   - Si no existe → la crea (`INSERT`).
   - Si existe y está `CANCELADA` → la reutiliza (`UPDATE`), porque `uq_entrada_funcion_butaca` no permite dos filas para el mismo `(id_funcion, id_butaca)`. Esto es lo que hace posible "revender" una butaca que alguien canceló.
7. `COMMIT`.

### 2.2. `sp_confirmar_pago`

Confirma el pago de una entrada `RESERVADA`.

```sql
CALL sp_confirmar_pago(@id_entrada);
```

Qué hace:

1. Busca la entrada (bloqueándola con `FOR UPDATE`): si no existe, o si no está `RESERVADA`, aborta.
2. Cambia su estado a `PAGADA` (esto dispara `trg_acumula_puntos`).
3. Marca la venta como `COMPLETADA`.
4. `COMMIT`.

---

## 3. Transacciones: ¿dónde están y por qué?

Los dos procedimientos siguen el mismo patrón:

```sql
CREATE PROCEDURE sp_algo(...)
BEGIN
    DECLARE ...;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- validaciones que no necesitan bloquear filas
    START TRANSACTION;
    -- lecturas con FOR UPDATE + escrituras
    COMMIT;
END
```

`START TRANSACTION` marca el inicio de un bloque de operaciones que deben ocurrir **todas juntas o ninguna**. Si cualquier sentencia dentro falla (una restricción `CHECK`, una `SIGNAL`, una llave foránea inexistente, etc.), el `EXIT HANDLER` ejecuta `ROLLBACK` — deshace todo lo que se había hecho desde el `START TRANSACTION` — y vuelve a lanzar el error original con `RESIGNAL`, para que Java sepa qué pasó. Si todo sale bien, `COMMIT` hace permanentes los cambios.

Ejemplo de por qué importa: si en `sp_registrar_venta` el `INSERT INTO venta` se ejecuta pero luego el `INSERT INTO entrada` falla (por ejemplo, porque la butaca ya estaba ocupada), **no queremos** que quede una venta "huérfana" sin ninguna entrada. Gracias a la transacción, ese `INSERT INTO venta` también se revierte.

---

## 4. ¿Y el canje de puntos? (sin procedimiento aparte)

No hace falta un tercer procedimiento para vender una entrada gratis por canje de puntos. La tabla `tipoentrada` ya tiene `descuento_porcentaje` (0 a 100). Basta con:

1. Registrar un tipo de entrada, por ejemplo `'Canje de puntos'`, con `descuento_porcentaje = 100`.
2. Llamar a `sp_registrar_venta` normal, usando ese `id_tipoentrada`: el procedimiento calculará `descuento = precio_base` y `precio_final = 0` automáticamente.
3. Como `precio_final` queda en `0`, el trigger `trg_acumula_puntos` **no** generará un punto nuevo cuando se confirme el pago (esto es justamente la regla de negocio: una entrada gratis no debe generar más puntos).
4. Desde Java, después de confirmar el pago, se inserta el descuento de puntos con un `INSERT` simple:

```sql
INSERT INTO historial_puntos (tipo_movimiento, cantidad_puntos, descripcion, id_cliente, id_venta)
VALUES ('CANJE', 9, 'Canje de puntos por entrada gratuita', ?, ?);
```

Antes de hacer ese `INSERT`, Java debe comprobar que el cliente tiene 9 puntos o más disponibles (ver `docs/validaciones_en_java.md`).

---

## 5. Cómo probar todo manualmente (antes de conectar Java)

Con datos base ya cargados (al menos una película, sala, butacas, función, tipo de entrada, método de pago, una persona+cliente y una persona+empleado):

1. **Venta simple:** llama a `sp_registrar_venta` y revisa `SELECT * FROM venta; SELECT * FROM entrada;`.
2. **Butaca duplicada:** llama de nuevo a `sp_registrar_venta` con la misma `id_funcion`/`id_butaca` → debe fallar con "La butaca ya esta ocupada para esta funcion."
3. **Confirmar pago:** llama a `sp_confirmar_pago(@id_entrada)` y confirma que `entrada.estado = 'PAGADA'`, `venta.estado = 'COMPLETADA'` y que apareció un movimiento `ACUMULACIÓN` en `historial_puntos`.
4. **Función cancelada:** cambia el estado de una función a `CANCELADA` (`UPDATE funcion SET estado = 'CANCELADA' WHERE id_funcion = ...`) e intenta llamar a `sp_registrar_venta` para ella → debe fallar.
5. **Canje de puntos:** crea un `tipoentrada` con `descuento_porcentaje = 100`, llama a `sp_registrar_venta` con ese tipo, confirma el pago con `sp_confirmar_pago` y verifica que `entrada.precio_final = 0` y que **no** se generó un punto nuevo.

---

## 6. Cómo se conecta esto con la aplicación Java

La aplicación **no** debe construir a mano los `INSERT`/`UPDATE` de `venta` y `entrada`: debe llamar a estos dos procedimientos con `CallableStatement`.

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
}
```

```java
try (CallableStatement cs = connection.prepareCall("{call sp_confirmar_pago(?)}")) {
    cs.setInt(1, idEntrada);
    cs.execute();
}
```

Cada `CALL` ya es, por sí solo, una transacción completa (el `START TRANSACTION`/`COMMIT` vive dentro del procedimiento), así que **no** hace falta que Java abra su propia transacción JDBC solo para esto. Si más adelante se agrega una venta con varias entradas (llamando a `sp_registrar_venta` varias veces seguidas), ahí sí conviene envolver esa secuencia de llamadas en `connection.setAutoCommit(false)` / `commit()` / `rollback()`.

Ver `docs/validaciones_en_java.md` para el resto de validaciones que siguen siendo responsabilidad de Java (elegir butacas en pantalla, choque de horarios, coherencia de roles, saldo de puntos, etc.).

---

## 7. Estado del documento

- [x] Explicación del trigger y de los dos procedimientos.
- [x] Ejemplos de `CALL`.
- [x] Guion de pruebas manuales.
- [x] Ejemplo de integración con Java (`CallableStatement`).
- [ ] Ejecutar el guion de pruebas de la sección 5 contra la base de datos real.
- [ ] Ejecutar `database/procedimientos_triggers.sql` cuando el equipo esté listo para programar esa parte.

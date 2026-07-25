# Reglas de Negocio - Sistema de Gestión de Cine

Este documento establece las reglas de negocio que deben cumplirse para garantizar el correcto funcionamiento del Sistema de Gestión de Cine. Estas reglas son consistentes con el modelo conceptual, el modelo lógico y el script `database/schema.sql` del proyecto.

> Nota de actualización: el modelo lógico introdujo la entidad **Persona** como base de Cliente, Empleado y Usuario, y simplificó varias tablas (Venta, Entrada, Historial_Puntos). Esta versión del documento refleja esos cambios.

---

# 1. Personas y catálogo base

## BR-01. Registro de personas
Toda persona deberá registrar nombres, apellidos, documento, teléfono y correo. El documento y el correo serán únicos en el sistema (`uq_persona_documento`, `uq_persona_correo`).

## BR-02. Persona como base de Cliente, Empleado y Usuario
Cliente, Empleado y Usuario no almacenarán datos personales propios (nombres, documento, teléfono, correo); estos se obtienen siempre a través de la Persona asociada mediante `id_persona`.

## BR-03. Unicidad de rol de negocio por persona
Una misma persona no podrá tener más de un registro de Cliente ni más de un registro de Empleado (`uq_cliente_persona`, `uq_empleado_persona`). Una persona podrá ser cliente, empleado, ambos, o ninguno.

## BR-04. Persona con múltiples usuarios
Una persona podrá tener una o varias cuentas de Usuario asociadas (`usuario.id_persona` no es único).

## BR-05. Géneros de película
Una película podrá pertenecer a uno o varios géneros, y un género podrá aplicarse a varias películas, mediante la tabla asociativa `pelicula_genero`.

---

# 2. Infraestructura y cartelera

## BR-06. Registro de salas
Toda función deberá programarse en una sala previamente registrada.

## BR-07. Registro de butacas
Una sala deberá tener butacas registradas antes de poder programar funciones. Las butacas no manejan clasificación VIP/Regular en el alcance actual, solo fila, número y estado.

## BR-08. Programación de funciones
Toda función deberá estar asociada a una película, una sala, una fecha, una hora de inicio y una tarifa base. El idioma de audio y de subtítulos son opcionales.

## BR-09. Solapamiento de funciones
No podrán existir dos funciones programadas simultáneamente en la misma sala. Esta regla se valida desde la aplicación (ver `validaciones_en_java.md`), ya que depende de comparar horarios entre varias filas.

---

# 3. Usuarios, clientes y empleados

## BR-10. Usuarios del sistema
El sistema manejará una entidad Usuario, vinculada a una Persona, que permitirá el acceso tanto de clientes como de empleados.

## BR-11. Roles
Todo usuario deberá tener uno de los siguientes roles: `ADMINISTRADOR`, `CAJERO`, `CLIENTE`.

## BR-12. Coherencia entre rol y persona
Un usuario con rol `ADMINISTRADOR` o `CAJERO` deberá corresponder a una persona que tenga un registro de Empleado. Un usuario con rol `CLIENTE` deberá corresponder a una persona que tenga un registro de Cliente. Como Usuario solo referencia a Persona (no directamente a Cliente/Empleado), esta coherencia se valida desde la aplicación.

## BR-13. Empleados
Los empleados realizarán las operaciones internas del sistema según los permisos de su rol.

## BR-14. Clientes
Los clientes podrán realizar compras en línea o en taquilla y participar en el programa de fidelidad.

---

# 4. Ventas

## BR-15. Canales de venta
Toda venta deberá indicar el canal por el cual fue realizada: `TAQUILLA` o `EN_LINEA`.

## BR-16. Venta en taquilla
Las ventas en taquilla deberán estar asociadas a un empleado (`chk_venta_canal_empleado`).

## BR-17. Venta en línea
Las ventas en línea podrán realizarse sin empleado asociado (`id_empleado` es `NULL` en este caso).

## BR-18. Cliente en la venta
Toda venta deberá estar asociada a un cliente registrado (`id_cliente` es obligatorio). Si el cliente es nuevo, debe registrarse antes de completar la compra.

## BR-19. Venta con múltiples entradas
Una venta podrá contener una o varias entradas.

## BR-20. Monto de la venta
La tabla Venta ya no almacena `subtotal` ni `descuento_total`: `total_pagado` se calcula en la aplicación como la suma de los `precio_final` de las entradas asociadas a la venta.

---

# 5. Entradas y butacas

## BR-21. Asociación de entrada
Cada entrada deberá estar asociada a una venta, una función, una butaca y un tipo de entrada.

## BR-22. Exclusividad de butacas
Una misma butaca no podrá venderse dos veces para una misma función (`uq_entrada_funcion_butaca` sobre `id_funcion, id_butaca`).

## BR-23. Disponibilidad
Solo podrán seleccionarse butacas disponibles para la función correspondiente.

## BR-24. Liberación de butacas
Cuando una entrada pase a estado `CANCELADA` (mediante `sp_cancelar_entrada`), la butaca volverá a estar disponible para esa función. Como `uq_entrada_funcion_butaca` impide insertar una segunda fila para la misma `(id_funcion, id_butaca)`, "revender" una butaca cancelada significa reutilizar (`UPDATE`) la misma fila de `entrada`, no crear una nueva. Los procedimientos `sp_registrar_venta_simple` y `sp_agregar_entrada_a_venta` ya implementan este comportamiento (ver `docs/procedimientos_bd.md`, sección 4).

---

# 6. Tipos de entrada y precios

## BR-25. Tipo de entrada
Toda entrada deberá tener un tipo de entrada.

## BR-26. Precio final
El precio final de una entrada se calcula como `precio_base - descuento` (`chk_entrada_precio_final`). El modelo ya no maneja un cargo adicional por butaca.

## BR-27. Motivo de la entrada
El campo `motivo` describe la razón de un descuento o de una entrada sin costo (por ejemplo, canje de puntos o cortesía). Reemplaza al antiguo indicador booleano `es_gratis`.

---

# 7. Programa de fidelidad

## BR-28. Acumulación de puntos
Solo las entradas pagadas generarán un movimiento de tipo `ACUMULACIÓN` en el historial de puntos, asociado a la venta correspondiente. Esta regla se aplica automáticamente mediante el trigger `trg_entrada_au_acumula_puntos`: la aplicación no necesita insertar el movimiento manualmente, solo debe llamar a `sp_marcar_entrada_pagada`.

## BR-29. Entradas gratuitas por canje
Una entrada obtenida mediante canje de puntos (`sp_canjear_puntos`) tendrá `precio_final` igual a cero (el `descuento` cubre el total del `precio_base`) y no generará un nuevo movimiento de `ACUMULACIÓN`: el trigger `trg_entrada_au_acumula_puntos` solo acumula puntos cuando `precio_final > 0`.

## BR-30. Historial de puntos
Todo movimiento de `ACUMULACIÓN` o `CANJE` deberá registrar obligatoriamente el cliente y la venta relacionados (`id_cliente` e `id_venta` son `NOT NULL` en `historial_puntos`) y una `cantidad_puntos` mayor que cero.

## BR-31. Aplicación del beneficio
Cuando un cliente cumpla la condición establecida por el programa de fidelidad, el sistema aplicará automáticamente el beneficio correspondiente.

---

# 8. Estados y transacciones

## BR-32. Estados de venta
Las ventas manejarán los estados: `PENDIENTE`, `COMPLETADA`, `CANCELADA`.

## BR-33. Estados de entrada
Las entradas manejarán los estados: `RESERVADA`, `PAGADA`, `CANCELADA`, `UTILIZADA`.

## BR-34. Conservación del historial
Las ventas, entradas y movimientos de puntos no deberán eliminarse físicamente de la base de datos.

## BR-35. Transacciones
El registro de una venta deberá ejecutarse como una única transacción. Si ocurre un error durante el proceso, todas las operaciones deberán revertirse. Esta regla está implementada en `database/procedimientos_triggers.sql`: cada procedimiento que escribe datos (`sp_registrar_venta_simple`, `sp_agregar_entrada_a_venta`, `sp_marcar_entrada_pagada`, `sp_cancelar_entrada`, `sp_canjear_puntos`) abre una transacción con `START TRANSACTION`, hace `COMMIT` al final, y tiene un `EXIT HANDLER FOR SQLEXCEPTION` que ejecuta `ROLLBACK` ante cualquier error.

---

# 9. Procedimientos, funciones y triggers

Además de las restricciones declarativas (`PRIMARY KEY`, `FOREIGN KEY`, `UNIQUE`, `CHECK`), el proyecto exige implementar procedimientos almacenados, triggers y transacciones. Todo esto vive en `database/procedimientos_triggers.sql` y está explicado en detalle, con ejemplos de uso y un guion de pruebas, en **`docs/procedimientos_bd.md`**.

## BR-36. Funciones auxiliares
`fn_funcion_disponible` y `fn_puntos_disponibles` son funciones de solo lectura reutilizables tanto por los triggers/procedimientos como por consultas directas desde Java.

## BR-37. Validación de función y butaca activas
No podrá crearse ni reactivarse (revender) una entrada para una función `CANCELADA`/`FINALIZADA` o para una butaca que no esté `ACTIVA` (`trg_entrada_bi_valida_funcion`, `trg_entrada_bu_valida_reventa`).

## BR-38. Cierre automático de la venta
Cuando todas las entradas de una venta llegan a un estado final (`PAGADA`, `UTILIZADA` o `CANCELADA`), la venta se actualiza automáticamente a `COMPLETADA` (si al menos una entrada fue honrada) o `CANCELADA` (si todas se cancelaron), sin intervención de la aplicación (`trg_entrada_au_acumula_puntos`).

## BR-39. Procedimientos como única puerta de entrada a las operaciones críticas
La aplicación Java no deberá construir manualmente las sentencias `INSERT`/`UPDATE` para registrar ventas, agregar entradas, marcar pagos, cancelar entradas o canjear puntos: deberá invocar siempre los procedimientos correspondientes, para garantizar que las validaciones, los cálculos y la atomicidad se apliquen de forma consistente.

---

# Checklist de correspondencia con `database/schema.sql`

Este checklist permite verificar, tabla por tabla, que ninguna restricción del modelo físico se pierda durante el desarrollo de la aplicación.

## persona
- [x] PK `id_persona`.
- [x] `documento` único (`uq_persona_documento`).
- [x] `correo` único (`uq_persona_correo`).
- [x] `nombres`, `apellidos`, `documento`, `telefono`, `correo` obligatorios.

## genero
- [x] PK `id_genero`.
- [x] `nombre_genero` único.
- [x] `estado` restringido a `ACTIVO`/`INACTIVO`.

## sala
- [x] PK `id_sala`.
- [x] `nombre_sala` único.
- [x] `capacidad > 0`.
- [x] `estado` restringido a `ACTIVA`/`INACTIVA`.

## metodopago
- [x] PK `id_metodopago`.
- [x] `nombre_metodo` único.
- [x] `estado` restringido a `ACTIVO`/`INACTIVO`.

## tipoentrada
- [x] PK `id_tipoentrada`.
- [x] `nombre_tipo` único.
- [x] `descuento_porcentaje` entre 0 y 100.
- [x] `estado` restringido a `ACTIVO`/`INACTIVO`.

## pelicula
- [x] PK `id_pelicula`.
- [x] `duracion_minutos > 0`.
- [x] `clasificacion` obligatoria.
- [x] `estado` restringido a `ACTIVA`/`INACTIVA`.
- [x] Ya no tiene `id_genero` directo (ver `pelicula_genero`).

## pelicula_genero
- [x] PK compuesta (`id_pelicula`, `id_genero`).
- [x] FK a `pelicula` y a `genero`.

## butaca
- [x] PK `id_butaca`.
- [x] FK a `sala`.
- [x] Único por `(id_sala, fila, numero)`.
- [x] `numero > 0`.
- [x] `estado` restringido a `ACTIVA`/`INACTIVA`.
- [x] Sin `tipo_butaca` (VIP/Regular no aplica).

## funcion
- [x] PK `id_funcion`.
- [x] FK a `pelicula` y a `sala`.
- [x] `tarifa_base > 0`.
- [x] `estado` restringido a `PROGRAMADA`/`EN_CURSO`/`FINALIZADA`/`CANCELADA`.
- [x] Sin `hora_fin` (solo `hora_inicio`).
- [x] `idioma_audio` e `idioma_subtitulos` opcionales.
- [ ] Validar en Java que no exista solapamiento de horario en la misma sala (no se puede expresar como `CHECK` de una sola fila).

## empleado
- [x] PK `id_empleado`.
- [x] FK única a `persona` (`uq_empleado_persona`).
- [x] `estado` restringido a `ACTIVO`/`INACTIVO`.

## cliente
- [x] PK `id_cliente`.
- [x] FK única a `persona` (`uq_cliente_persona`).
- [x] `estado` restringido a `ACTIVO`/`INACTIVO`.

## usuario
- [x] PK `id_usuario`.
- [x] `nombre_usuario` único.
- [x] FK a `persona` (no única: una persona puede tener varios usuarios).
- [x] `rol` restringido a `ADMINISTRADOR`/`CAJERO`/`CLIENTE`.
- [x] `estado` restringido a `ACTIVO`/`INACTIVO`.
- [ ] Validar en Java la coherencia rol-persona (BR-12), ya que no hay FK directa a `cliente`/`empleado`.

## venta
- [x] PK `id_venta`.
- [x] FK a `cliente` (obligatoria), `empleado` (opcional) y `metodopago` (obligatoria).
- [x] `total_pagado >= 0`.
- [x] `canal_venta` restringido a `TAQUILLA`/`EN_LINEA`.
- [x] `estado` restringido a `PENDIENTE`/`COMPLETADA`/`CANCELADA`.
- [x] `chk_venta_canal_empleado`: `TAQUILLA` requiere empleado; `EN_LINEA` no lo requiere.
- [x] Sin `subtotal` ni `descuento_total` (solo `total_pagado`).

## entrada
- [x] PK `id_entrada`.
- [x] FK a `venta`, `funcion`, `butaca`, `tipoentrada`.
- [x] Único por `(id_funcion, id_butaca)`.
- [x] `precio_base >= 0`, `descuento >= 0`, `precio_final >= 0`.
- [x] `precio_final = precio_base - descuento`.
- [x] `estado` restringido a `RESERVADA`/`PAGADA`/`CANCELADA`/`UTILIZADA`.
- [x] Sin `cargo_butaca` ni `es_gratis` (reemplazado por `motivo`).

## historial_puntos
- [x] PK `id_historial`.
- [x] FK a `cliente` (obligatoria) y a `venta` (obligatoria).
- [x] Sin FK a `entrada`.
- [x] `cantidad_puntos > 0`.
- [x] `tipo_movimiento` restringido a `ACUMULACIÓN`/`CANJE`.

## Procedimientos, funciones y triggers (`database/procedimientos_triggers.sql`)
- [x] `fn_funcion_disponible` implementada.
- [x] `fn_puntos_disponibles` implementada.
- [x] `trg_entrada_bi_valida_funcion` implementado (BEFORE INSERT).
- [x] `trg_entrada_bu_valida_reventa` implementado (BEFORE UPDATE).
- [x] `trg_entrada_au_acumula_puntos` implementado (AFTER UPDATE): acumulación de puntos + cierre automático de venta.
- [x] `sp_registrar_venta_simple` implementado, con transacción (`START TRANSACTION`/`COMMIT`/`ROLLBACK`).
- [x] `sp_agregar_entrada_a_venta` implementado, con transacción.
- [x] `sp_marcar_entrada_pagada` implementado, con transacción.
- [x] `sp_cancelar_entrada` implementado, con transacción.
- [x] `sp_canjear_puntos` implementado, con transacción.
- [ ] Guion de pruebas de `docs/procedimientos_bd.md` (sección 5) ejecutado contra la base de datos real.

---

# Estado del documento

- [x] Reglas de personas y catálogo base definidas.
- [x] Reglas de infraestructura definidas.
- [x] Reglas de usuarios definidas.
- [x] Reglas de ventas definidas.
- [x] Reglas de entradas y butacas definidas.
- [x] Reglas del programa de fidelidad definidas.
- [x] Reglas de estados y transacciones definidas.
- [x] Reglas de procedimientos, funciones y triggers definidas.
- [x] Checklist de correspondencia con `database/schema.sql` definido.
- [x] Documento consistente con el modelo conceptual.
- [x] Documento consistente con el modelo lógico.
- [ ] Validaciones pendientes de implementar en Java (solapamiento de horario, coherencia rol-persona).

---

# Conclusión

Las reglas de negocio establecen las restricciones necesarias para garantizar la integridad, consistencia y trazabilidad del Sistema de Gestión de Cine. Estas reglas sirven como base para la implementación de la base de datos y el desarrollo de la aplicación, asegurando que las operaciones del sistema se realicen de forma segura y conforme al diseño definido.

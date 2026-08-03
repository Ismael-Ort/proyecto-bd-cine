# Reglas de Negocio - Sistema de Gestión de Cine

Este documento establece las reglas de negocio que deben cumplirse para garantizar el correcto funcionamiento del Sistema de Gestión de Cine. Estas reglas son consistentes con el modelo conceptual, el modelo lógico y el script `database/schema.sql` del proyecto.

> Nota de actualización: el modelo lógico introdujo la entidad **Persona** como base de Cliente, Empleado y Usuario, y simplificó varias tablas (Venta, Entrada, Historial_Puntos). Después, por indicación del profesor, se agregaron `fecha_nacimiento` y `sexo` a Persona, se restauró `hora_fin` en Función y se quitó el campo `motivo` de Entrada. Esta versión del documento refleja el estado actual de `database/schema.sql`.

---

# 1. Personas y catálogo base

## BR-01. Registro de personas
Toda persona deberá registrar nombres, apellidos, fecha de nacimiento, sexo (`M` o `F`), documento, teléfono y correo. El documento y el correo serán únicos en el sistema (`uq_persona_documento`, `uq_persona_correo`).

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
Toda función deberá estar asociada a una película, una sala, una fecha, una hora de inicio y una hora de fin (`hora_fin > hora_inicio`, `chk_funcion_horario`), además de una tarifa base. La hora de fin permite reservar tiempo para anuncios, limpieza y preparación de la sala, no solo la duración de la película. El idioma de audio y de subtítulos son opcionales.

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
La tabla Venta ya no almacena `subtotal` ni `descuento_total`. `total_pagado` se calcula automáticamente dentro del procedimiento `sp_registrar_venta` (ver `docs/procedimientos_bd.md`) a partir de la tarifa base de la función y el descuento del tipo de entrada.

---

# 5. Entradas y butacas

## BR-21. Asociación de entrada
Cada entrada deberá estar asociada a una venta, una función, una butaca y un tipo de entrada.

## BR-22. Exclusividad de butacas
Una misma butaca no podrá venderse dos veces para una misma función (`uq_entrada_funcion_butaca` sobre `id_funcion, id_butaca`).

## BR-23. Disponibilidad
Solo podrán seleccionarse butacas disponibles para la función correspondiente.

## BR-24. Liberación de butacas
Cuando una entrada pase a estado `CANCELADA`, la butaca volverá a estar disponible para esa función. Como `uq_entrada_funcion_butaca` impide insertar una segunda fila para la misma `(id_funcion, id_butaca)`, "revender" una butaca cancelada significa reutilizar (`UPDATE`) la misma fila de `entrada`, no crear una nueva. El procedimiento `sp_registrar_venta` ya implementa este comportamiento (ver `docs/procedimientos_bd.md`, sección 2.1).

---

# 6. Tipos de entrada y precios

## BR-25. Tipo de entrada
Toda entrada deberá tener un tipo de entrada.

## BR-26. Precio final
El precio final de una entrada se calcula como `precio_base - descuento` (`chk_entrada_precio_final`). El modelo ya no maneja un cargo adicional por butaca ni un campo `motivo`: el precio se calcula siempre a partir de `funcion.tarifa_base` y `tipoentrada.descuento_porcentaje` dentro de `sp_registrar_venta`.

---

# 7. Programa de fidelidad

## BR-27. Acumulación de puntos
Solo las entradas pagadas generarán un movimiento de tipo `ACUMULACIÓN` en el historial de puntos, asociado a la venta correspondiente. Esta regla se aplica automáticamente mediante el trigger `trg_acumula_puntos`: la aplicación no necesita insertar el movimiento manualmente, solo debe llamar a `sp_confirmar_pago`.

## BR-28. Entradas gratuitas por canje
Una entrada obtenida mediante canje de puntos tendrá `precio_final` igual a cero. No se necesita un procedimiento aparte: basta con registrar un `tipoentrada` con `descuento_porcentaje = 100` y usar el mismo `sp_registrar_venta`. El trigger `trg_acumula_puntos` solo acumula puntos cuando `precio_final > 0`, así que una entrada gratuita nunca genera un punto nuevo. El descuento de los 9 puntos se registra desde Java con un `INSERT` directo en `historial_puntos` (ver `docs/procedimientos_bd.md`, sección 4). Ver BR-30 sobre cómo se confirma el pago cuando la entrada gratuita todavía está `RESERVADA`.

## BR-29. Historial de puntos
Todo movimiento de `ACUMULACIÓN` o `CANJE` deberá registrar obligatoriamente el cliente y la venta relacionados (`id_cliente` e `id_venta` son `NOT NULL` en `historial_puntos`) y una `cantidad_puntos` mayor que cero.

## BR-30. Aplicación del beneficio
Cuando un cliente cumpla la condición establecida por el programa de fidelidad (9 puntos disponibles), el sistema permitirá aplicar el beneficio correspondiente (una entrada gratuita). El canje puede aplicarse sobre una entrada gratuita ya confirmada por RD$0 desde Ventas, o sobre una que todavía está `RESERVADA`: en ese caso, `FidelidadControl.registrarCanje` llama primero a `sp_confirmar_pago` para cerrar esa venta pendiente y recién después inserta el `CANJE`, así que canjear puntos desde Fidelidad también sirve para completar una compra de entrada gratuita que había quedado a medias en Ventas.

---

# 8. Estados y transacciones

## BR-31. Estados de venta
Las ventas manejarán los estados: `PENDIENTE`, `COMPLETADA`, `CANCELADA`.

## BR-32. Estados de entrada
Las entradas manejarán los estados: `RESERVADA`, `PAGADA`, `CANCELADA`, `UTILIZADA`.

## BR-33. Conservación del historial
Las ventas, entradas y movimientos de puntos no deberán eliminarse físicamente de la base de datos.

## BR-34. Transacciones
El registro de una venta y la confirmación de un pago deberán ejecutarse cada uno como una única transacción. Si ocurre un error durante el proceso, todas las operaciones deberán revertirse. Esta regla está implementada en `database/procedimientos_triggers.sql`: `sp_registrar_venta` y `sp_confirmar_pago` abren una transacción con `START TRANSACTION`, hacen `COMMIT` al final, y tienen un `EXIT HANDLER FOR SQLEXCEPTION` que ejecuta `ROLLBACK` ante cualquier error.

---

# 9. Procedimientos almacenados y trigger

Además de las restricciones declarativas (`PRIMARY KEY`, `FOREIGN KEY`, `UNIQUE`, `CHECK`), el proyecto exige, como mínimo, un procedimiento almacenado, un trigger y una transacción. Esto vive en `database/procedimientos_triggers.sql` (que se ejecuta aparte, no al crear la base de datos) y está explicado en detalle, con ejemplos de uso y un guion de pruebas, en **`docs/procedimientos_bd.md`**. A propósito **no se usan funciones almacenadas** (no se vieron en clase) y se implementó solo lo mínimo necesario para no complicar de más el proyecto.

## BR-35. Validación de función al registrar una venta
No podrá registrarse una entrada para una función que no exista o que esté `CANCELADA`/`FINALIZADA`. Esta validación vive dentro de `sp_registrar_venta` (no en un trigger aparte).

## BR-36. Cierre de la venta al confirmar el pago
Al confirmar el pago de una entrada (`sp_confirmar_pago`), la venta correspondiente se marca como `COMPLETADA` en la misma transacción.

## BR-37. Procedimientos como única puerta de entrada a las operaciones críticas
La aplicación Java no deberá construir manualmente las sentencias `INSERT`/`UPDATE` para registrar una venta o confirmar un pago: deberá invocar siempre `sp_registrar_venta` y `sp_confirmar_pago`, para garantizar que las validaciones, los cálculos y la atomicidad se apliquen de forma consistente.

## BR-41. Cancelación de una entrada limitada por el tiempo
Una entrada solo podrá cancelarse mientras su función todavía no haya terminado (comparando `fecha_funcion`/`hora_fin` contra la hora del servidor, igual que `trg_actualizar_estado_funcion`). Una vez que la función ya pasó, cancelar no tiene sentido (la butaca no se puede revender) y la operación no hace nada. Esta validación vive en `EntradaBD.cancelarEntrada` (un `UPDATE` con esa condición en el `WHERE`, no un procedimiento aparte).

## BR-42. Cancelación en cascada de una función
Cancelar una función (`sp_cancelar_funcion`) cancela automáticamente, en la misma transacción, todo lo que dependía de ella:

1. Todas sus entradas pasan a `CANCELADA` (sin importar el límite de tiempo de BR-41: esta es una cancelación administrativa, no una del cliente).
2. Las ventas que tenían entradas en esa función pasan a `CANCELADA` ("se devuelve el dinero": la venta deja de estar `PENDIENTE`/`COMPLETADA`).
3. Por cada entrada que ya estaba `PAGADA` (ya había generado un punto de fidelidad), se registra un movimiento `CANJE` en `historial_puntos` que resta ese punto.

Todo esto lo hace `trg_cancelar_entradas_por_funcion` (`AFTER UPDATE ON funcion`), disparado por el `UPDATE` que hace `sp_cancelar_funcion` dentro de su propia transacción: si algo de esto falla, la función tampoco queda cancelada.

---

# 10. Autenticación y control de acceso

## BR-38. Coherencia entre cargo de Empleado y rol de Usuario
Además de BR-12 (la persona debe tener registro de Empleado), un usuario con rol `ADMINISTRADOR` o `CAJERO` solo puede crearse si el `cargo` de ese Empleado es, respectivamente, "Administrador" o "Cajero" (comparación sin distinguir mayúsculas/minúsculas). Empleados con otros cargos (ej. conserje, seguridad) quedan registrados como empleados del sistema pero sin usuario propio — el schema ya lo permite porque `usuario` no tiene FK obligatoria a `empleado`. Esta validación vive en la aplicación (`UsuarioControl`), igual que BR-12.

## BR-39. Autenticación
El inicio de sesión valida `nombre_usuario` + contraseña contra `usuario.hash_contrasena` (solo cuentas con `estado = 'ACTIVO'`). Las contraseñas se guardan con **BCrypt** (librería `jbcrypt`, salt incorporado en cada hash), nunca en texto plano ni con un hash sin salt.

## BR-40. Permisos por pantalla según rol
Cada rol ve y puede operar solo el subconjunto de pantallas que le corresponde (RF-17):

| Pantalla | ADMINISTRADOR | CAJERO | CLIENTE |
|---|---|---|---|
| Panel | Sí | No | No |
| Películas / Salas / Géneros | Sí (crear/editar) | Solo lectura | No |
| Funciones | Sí (crear/editar) | No | No |
| Ventas | Sí | Sí | Sí |
| Clientes | Sí (crear/editar) | Sí (crear/editar) | No |
| Empleados / Usuarios | Sí | No | No |
| Fidelidad | Sí | Sí | Sí |

Esta matriz se aplica en `VentanaPrincipalControl.aplicarPermisosPorRol()` (qué botones del menú lateral se muestran) y, en las pantallas de solo lectura, deshabilitando el botón "Guardar" en el controlador de cada una. Funciones quedó como pantalla exclusiva de ADMINISTRADOR (programar funciones es una decisión administrativa); CAJERO ya no tiene acceso ni siquiera en modo lectura.

---

# Checklist de correspondencia con `database/schema.sql`

Este checklist permite verificar, tabla por tabla, que ninguna restricción del modelo físico se pierda durante el desarrollo de la aplicación.

## persona
- [x] PK `id_persona`.
- [x] `documento` único (`uq_persona_documento`).
- [x] `correo` único (`uq_persona_correo`).
- [x] `nombres`, `apellidos`, `fecha_nacimiento`, `sexo`, `documento`, `telefono`, `correo` obligatorios.
- [x] `sexo` restringido a `M`/`F` (`chk_persona_sexo`).

## genero
- [x] PK `id_genero`.
- [x] `nombre_genero` único.
- [x] `estado` restringido a `ACTIVO`/`INACTIVO`.
- [x] Catálogo que sí crece con el tiempo (puede surgir un género nuevo): mantiene su propia pantalla de alta/edición en la aplicación, agrupada bajo "Administración" en el menú (no es una operación diaria como Ventas).

## sala
- [x] PK `id_sala`.
- [x] `nombre_sala` único.
- [x] `capacidad > 0`.
- [x] `estado` restringido a `ACTIVA`/`INACTIVA`.

## metodopago
- [x] PK `id_metodopago`.
- [x] `nombre_metodo` único.
- [x] `estado` restringido a `ACTIVO`/`INACTIVO`.
- [x] Catálogo fijo: se carga una sola vez con `database/datos_iniciales.sql`. La aplicación **no** tiene pantalla para crear/editar métodos de pago; en Ventas solo se **selecciona** uno existente (`ComboBox`), nunca se inventa uno nuevo desde ahí.

## tipoentrada
- [x] PK `id_tipoentrada`.
- [x] `nombre_tipo` único.
- [x] `descuento_porcentaje` entre 0 y 100.
- [x] `estado` restringido a `ACTIVO`/`INACTIVO`.
- [x] Catálogo fijo: se carga una sola vez con `database/datos_iniciales.sql` (incluye el tipo "Canje de puntos" al 100% de descuento usado en BR-28). La aplicación **no** tiene pantalla para crear/editar tipos de entrada; en Ventas solo se **selecciona** uno existente.

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
- [x] `hora_fin > hora_inicio` (`chk_funcion_horario`).
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
- [x] Validar en Java la coherencia rol-persona (BR-12) y cargo-rol (BR-38), ya que no hay FK directa a `cliente`/`empleado`.
- [x] `hash_contrasena` generado con BCrypt (BR-39), nunca en texto plano.

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
- [x] Sin `cargo_butaca`, `es_gratis` ni `motivo`.

## historial_puntos
- [x] PK `id_historial`.
- [x] FK a `cliente` (obligatoria) y a `venta` (obligatoria).
- [x] Sin FK a `entrada`.
- [x] `cantidad_puntos > 0`.
- [x] `tipo_movimiento` restringido a `ACUMULACIÓN`/`CANJE`.

## Procedimientos y trigger (`database/procedimientos_triggers.sql`)
- [x] `trg_acumula_puntos` implementado (AFTER UPDATE ON entrada).
- [x] `trg_actualizar_estado_funcion` implementado (BEFORE UPDATE ON funcion).
- [x] `trg_cancelar_entradas_por_funcion` implementado (AFTER UPDATE ON funcion; BR-42).
- [x] `sp_registrar_venta` implementado, con transacción (`START TRANSACTION`/`COMMIT`/`ROLLBACK`).
- [x] `sp_confirmar_pago` implementado, con transacción.
- [x] `sp_cancelar_funcion` implementado, con transacción (BR-42).
- [x] Sin funciones almacenadas (a propósito).
- [x] `database/procedimientos_triggers.sql` ejecutado contra la base de datos real.
- [x] Cascada de `sp_cancelar_funcion` probada manualmente contra la base de datos real (entrada/venta/historial_puntos quedan consistentes).

---

# Estado del documento

- [x] Reglas de personas y catálogo base definidas.
- [x] Reglas de infraestructura definidas.
- [x] Reglas de usuarios definidas.
- [x] Reglas de ventas definidas.
- [x] Reglas de entradas y butacas definidas.
- [x] Reglas del programa de fidelidad definidas.
- [x] Reglas de estados y transacciones definidas.
- [x] Reglas de procedimientos y trigger definidas.
- [x] Reglas de autenticación y control de acceso definidas (BR-38 a BR-40).
- [x] Checklist de correspondencia con `database/schema.sql` definido.
- [x] Documento consistente con el modelo conceptual.
- [x] Documento consistente con el modelo lógico.
- [x] Filtro de ventas por rol implementado en `VentaBD.listarVentas`: Administrador ve todas las ventas, Cajero solo las que él registró (por `id_empleado`) y Cliente solo las suyas (por `id_cliente`). Fidelidad filtra igual por cliente propio (`FidelidadControl.clienteConsultado`, oculta el selector de cliente para el rol `CLIENTE`).

---

# Conclusión

Las reglas de negocio establecen las restricciones necesarias para garantizar la integridad, consistencia y trazabilidad del Sistema de Gestión de Cine. Estas reglas sirven como base para la implementación de la base de datos y el desarrollo de la aplicación, asegurando que las operaciones del sistema se realicen de forma segura y conforme al diseño definido.

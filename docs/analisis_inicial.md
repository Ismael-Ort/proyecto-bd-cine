# Análisis inicial del sistema de cine

## Propuesta base

La propuesta inicial del proyecto plantea el desarrollo de un sistema para un cine que actualmente maneja sus ventas de entradas de forma manual o mediante un sistema básico que solo registra transacciones. Esta situación ha generado problemas operativos importantes, especialmente en el control de disponibilidad de las salas y en el manejo del programa de fidelidad de los clientes.

Uno de los problemas principales es que los empleados no tienen visibilidad en tiempo real del aforo disponible para cada función. Como consecuencia, en varias ocasiones se han vendido más entradas que butacas disponibles, provocando quejas de clientes, devoluciones de dinero y una mala experiencia de servicio.

Otro problema identificado es el manejo manual del programa de fidelidad. El cine ofrece una promoción donde, por cada 10 entradas compradas, la siguiente entrada es gratuita. Actualmente, este control se realiza de forma manual mediante la revisión de boletos físicos o registros poco confiables. Esto puede generar errores de cálculo, fraudes por parte de clientes y pérdida de ingresos para el cine.

A partir de esta propuesta, el equipo desarrollará un sistema de gestión interna para el cine, enfocado en el control de funciones, butacas, ventas de entradas, clientes, empleados y programa de fidelidad.

---

## Problema identificado

El cine necesita un sistema que permita controlar de forma organizada y segura la venta de entradas, la disponibilidad de butacas por función y el programa de fidelidad de los clientes.

El sistema actual presenta varias debilidades:

- No controla en tiempo real la disponibilidad de butacas.
- Permite que se vendan más entradas que la capacidad real de una sala.
- No evita que una misma butaca pueda asignarse dos veces para una misma función.
- El control de fidelidad depende demasiado del cajero.
- No existe un historial claro de los puntos ganados y canjeados por los clientes.
- No se tiene suficiente trazabilidad sobre ventas anuladas, entradas gratuitas o beneficios aplicados.
- El sistema no contempla de forma organizada diferentes tipos de entradas ni variaciones de precio según el tipo de cliente, horario o tipo de sala.

Estos problemas afectan la eficiencia operativa del cine, la experiencia del cliente y el control interno de los ingresos.

---

## Objetivo general

Diseñar y desarrollar un sistema de gestión para un cine que permita controlar funciones, salas, butacas, ventas de entradas, clientes, empleados y programa de fidelidad, utilizando una base de datos relacional que garantice mayor organización, integridad, trazabilidad y seguridad en el manejo de la información.

---

## Objetivos específicos

- Registrar clientes del cine.
- Registrar empleados responsables de las operaciones del sistema.
- Crear usuarios de acceso para los empleados.
- Controlar roles de usuario, como administrador, cajero o supervisor.
- Registrar películas y sus géneros.
- Registrar salas y sus butacas físicas.
- Programar funciones por película, sala, fecha y horario.
- Permitir la venta de entradas para funciones específicas.
- Consultar la disponibilidad de butacas antes de realizar una venta.
- Evitar la sobreventa de entradas.
- Evitar que una misma butaca sea vendida dos veces para la misma función.
- Registrar diferentes tipos de entrada, como Adulto, Niño, Estudiante o Adulto Mayor.
- Manejar precios según tipo de entrada, tipo de butaca o tarifa base de la función.
- Registrar métodos de pago utilizados en las ventas.
- Automatizar el control del programa de fidelidad.
- Registrar un historial de puntos ganados y canjeados por los clientes.
- Permitir el registro de entradas gratuitas cuando aplique el beneficio de fidelidad.
- Mantener trazabilidad de ventas activas, anuladas o devueltas.
- Diseñar una estructura que pueda ampliarse con módulos futuros, como cafetería, productos, inventario, promociones o reservas.

---

## Alcance del proyecto

El sistema estará enfocado principalmente en la gestión interna de ventas de entradas para un cine. Se trabajará con clientes, empleados, usuarios, películas, géneros, salas, butacas, funciones, ventas, entradas, tipos de entrada, métodos de pago y programa de fidelidad.

El sistema será utilizado por empleados del cine. Los clientes no iniciarán sesión ni tendrán usuario o contraseña dentro del sistema. Su participación será como compradores registrados, permitiendo asociar sus compras y movimientos de puntos al programa de fidelidad.

Como valor agregado, el diseño incluirá el manejo de butacas físicas por sala, de manera que cada entrada esté asociada a una butaca específica dentro de una función. Esto permitirá controlar la disponibilidad con mayor precisión y evitar duplicidades.

También se incluirá un sistema de historial de puntos para el programa de fidelidad. En lugar de guardar solamente un campo con los puntos acumulados del cliente, se registrarán los movimientos de puntos ganados y canjeados. Esto permitirá auditar el beneficio aplicado y evitar fraudes o errores de cálculo.

El proyecto podrá considerar en una etapa futura un módulo complementario de cafetería, con productos, inventario, combos y detalle de productos vendidos. Sin embargo, este módulo queda definido inicialmente como una extensión futura y no como parte obligatoria del alcance principal del proyecto.

---

## Valor agregado del diseño

Además de cumplir con la propuesta base, el diseño agrega varios elementos que hacen el sistema más completo y cercano a un cine real.

### 1. Gestión avanzada de butacas

El sistema no se limitará a contar la cantidad total de entradas vendidas. También manejará un mapa de butacas físicas por sala, donde cada butaca tendrá fila, número y tipo.

Cada entrada vendida estará relacionada con una función y una butaca específica. Para evitar duplicidades, se propondrá una regla de integridad que impida vender la misma butaca dos veces en la misma función.

Regla principal:

Una misma combinación de función y butaca no puede repetirse en entradas activas.

### 2. Programa de fidelidad con historial

El sistema no reiniciará los puntos del cliente sin dejar evidencia. En su lugar, se manejará un historial de puntos donde se registren los puntos ganados y canjeados por cada cliente.

Esto permitirá saber cuándo un cliente ganó puntos, cuándo los usó y a qué venta estuvo asociado el movimiento. De esta forma, el sistema será más seguro y auditable.

### 3. Tipos de entrada y tarifas

El sistema podrá manejar distintos tipos de entradas, como Adulto, Niño, Estudiante o Adulto Mayor. Esto permitirá aplicar precios diferentes según el tipo de entrada seleccionado.

También se podrá considerar una estructura de precios tomando en cuenta la tarifa base de la función y el tipo de butaca. Por ejemplo, una butaca VIP podría tener un cargo adicional.

### 4. Control de ventas anuladas o devueltas

Las ventas no deben eliminarse directamente de la base de datos. En caso de anulación o devolución, la venta cambiará de estado. Esto permite mantener el historial de operaciones y mejorar el control interno.

Estados posibles:

- Activa.
- Anulada.
- Devuelta.

Cuando una venta o entrada sea anulada o devuelta, las butacas asociadas no deberán considerarse como ocupadas para futuras consultas de disponibilidad.

### 5. Separación entre clientes, empleados y usuarios

El sistema diferenciará claramente entre clientes, empleados y usuarios.

Los clientes serán las personas que compran entradas y acumulan puntos de fidelidad.

Los empleados serán las personas que trabajan en el cine y realizan operaciones internas, como ventas, consultas, anulaciones o administración de información.

Los usuarios serán las cuentas de acceso asociadas a empleados, utilizadas para iniciar sesión en el sistema y controlar permisos según el rol asignado.

### 6. Posible módulo de cafetería

Como extensión futura, el sistema puede incluir un módulo de cafetería para registrar productos como palomitas, refrescos y combos. Esta parte permitiría que una venta general incluya tanto entradas como productos de cafetería.

Sin embargo, este módulo no forma parte del alcance principal actual y solo será considerado si el tiempo y los criterios del profesor lo permiten.

---

## Lógica inicial del acceso al sistema

El sistema será utilizado por empleados del cine mediante un usuario y contraseña.

Los clientes no tendrán usuario ni contraseña. Su participación en el sistema será como compradores registrados, permitiendo asociar sus ventas y movimientos de puntos al programa de fidelidad.

La lógica inicial será la siguiente:

- Un empleado inicia sesión mediante un usuario.
- El sistema identifica el rol del usuario.
- Según el rol, el empleado podrá acceder a determinadas funciones.
- El cajero podrá registrar clientes, consultar funciones, seleccionar butacas y realizar ventas.
- El administrador podrá gestionar información base del sistema, como empleados, usuarios, películas, salas, butacas y funciones.
- El supervisor podrá consultar operaciones, revisar ventas y gestionar anulaciones o devoluciones según el alcance definido.

Esta decisión permite separar claramente los datos de los clientes de los datos de acceso al sistema.

---

## Roles iniciales del sistema

El sistema podrá manejar roles para controlar los permisos de los usuarios.

### Administrador

El administrador tendrá acceso a la gestión general del sistema.

Podrá realizar acciones como:

- Registrar y actualizar empleados.
- Crear y administrar usuarios.
- Gestionar películas y géneros.
- Gestionar salas y butacas.
- Programar funciones.
- Consultar ventas.
- Revisar historial de puntos.
- Apoyar en anulaciones o devoluciones.

### Cajero

El cajero será el usuario encargado de las operaciones de venta.

Podrá realizar acciones como:

- Buscar clientes.
- Registrar clientes nuevos.
- Consultar funciones disponibles.
- Consultar butacas disponibles.
- Registrar ventas de entradas.
- Aplicar el programa de fidelidad cuando corresponda.
- Consultar puntos de clientes.

### Supervisor

El supervisor tendrá funciones de revisión y control.

Podrá realizar acciones como:

- Consultar ventas.
- Revisar operaciones realizadas por cajeros.
- Consultar historial de puntos.
- Gestionar anulaciones o devoluciones, según el alcance definido.
- Revisar reportes o consultas básicas.

Estos roles podrán ajustarse durante el desarrollo según los criterios oficiales del proyecto.

## Ajuste final del enfoque del sistema

Después de la revisión del modelo conceptual, el equipo decidió ajustar el enfoque del sistema para contemplar dos canales de venta: venta presencial en taquilla y venta en línea.

En la venta presencial, un empleado del cine inicia sesión en el sistema, atiende al cliente, consulta las funciones disponibles, selecciona una butaca y registra la venta.

En la venta en línea, el cliente puede iniciar sesión mediante un usuario, consultar la cartelera, seleccionar una función, elegir una butaca disponible y realizar la compra desde una plataforma digital.

Para evitar duplicar la lógica de acceso, el sistema manejará una entidad general llamada Usuario. Esta entidad permitirá el acceso tanto a empleados como a clientes, diferenciando sus permisos mediante el atributo Rol.

Los roles definidos inicialmente serán ADMIN, CAJERO, SUPERVISOR y CLIENTE.

De esta forma, el sistema no queda limitado a ventas presenciales en taquilla, sino que también permite representar un modelo más actual de compra en línea. Al mismo tiempo, se mantiene el enfoque principal del proyecto: controlar funciones, butacas, ventas, entradas y programa de fidelidad.

---

## Escenario esperado

Un cliente llamado María tiene 9 puntos acumulados en el programa de fidelidad. María llega a la taquilla y desea adquirir una entrada para una función de las 8:00 PM en la Sala 2.

El cajero inicia sesión en el sistema con su usuario y contraseña. Luego, ingresa el ID, documento o dato de búsqueda de María en el sistema y selecciona la función correspondiente.

El sistema muestra las butacas disponibles para esa función. María selecciona una butaca específica.

Antes de registrar la venta, el sistema valida que la butaca seleccionada no haya sido vendida previamente para esa misma función. También verifica los puntos acumulados de María a través del historial de puntos.

Como María tiene 9 puntos acumulados antes de realizar la compra y la regla de fidelidad indica que al cumplir esa condición se aplica una entrada gratuita, el sistema aplica automáticamente el beneficio correspondiente.

La entrada se registra con descuento del 100%, el precio final queda en 0, y se genera el movimiento de canje en el historial de puntos. Esta entrada gratuita no acumula nuevos puntos.

La operación queda almacenada con los datos del cliente, el empleado que realizó la venta, la función, la butaca seleccionada, el tipo de entrada, el método de pago, el estado de la venta y el movimiento de puntos correspondiente.

---

## Entidades principales definidas inicialmente

A partir del análisis inicial, los requerimientos y las reglas de negocio, el equipo decidió trabajar inicialmente con las siguientes entidades principales:

- Cliente.
- Empleado.
- Usuario.
- Género.
- Película.
- Sala.
- Butaca.
- Función.
- Tipo de entrada.
- Método de pago.
- Venta.
- Entrada.
- Historial de puntos.

Estas entidades serán utilizadas como base para elaborar el modelo conceptual del sistema.

---

## Entidades opcionales o de extensión futura

También se consideran como posibles entidades futuras:

- Producto.
- Inventario.
- Combo.
- Detalle de productos vendidos.
- Tarifa.
- Reserva.

Estas entidades no forman parte del alcance principal inicial. Podrán evaluarse más adelante si el tiempo disponible, la complejidad del desarrollo o los criterios oficiales del profesor lo permiten.

---

## Reglas de negocio iniciales

A partir del análisis del problema, se identifican las siguientes reglas iniciales:

- Una película puede tener varias funciones.
- Una función pertenece a una sola película.
- Una función se realiza en una sola sala.
- Una sala puede tener muchas butacas.
- Una butaca pertenece a una sola sala.
- Una venta puede incluir una o varias entradas.
- Cada entrada pertenece a una venta.
- Cada entrada pertenece a una función específica.
- Cada entrada debe estar asociada a una butaca.
- Una misma butaca no puede venderse dos veces para la misma función en entradas activas.
- Cada venta debe estar asociada a un cliente registrado o a un cliente genérico si se permite venta sin registro.
- Cada venta debe ser registrada por un empleado.
- Cada empleado puede tener un usuario de acceso al sistema.
- Cada usuario tendrá un rol que determine sus permisos.
- Cada entrada puede tener un tipo de entrada.
- El precio final de una entrada puede variar según la tarifa base, el tipo de entrada y el tipo de butaca.
- El cliente acumula puntos por entradas pagadas.
- Las entradas gratuitas por fidelidad no deben acumular puntos.
- El uso de puntos debe registrarse en el historial de puntos.
- Una venta anulada o devuelta no se elimina, solo cambia de estado.
- Si una venta o entrada se anula, las butacas asociadas deben volver a estar disponibles.
- El módulo de cafetería se mantiene como una posible extensión futura.

---

## Consideraciones técnicas

El sistema deberá utilizar una base de datos relacional. Para garantizar la integridad de los datos, se propondrán restricciones como llaves primarias, llaves foráneas y restricciones únicas cuando sea necesario.

Una restricción importante será evitar que una misma butaca se repita para la misma función en entradas activas. Esto puede lograrse mediante reglas de integridad en la base de datos y validaciones en la lógica del sistema.

También se considerará el uso de transacciones para que el proceso de venta sea seguro. La verificación de disponibilidad, el registro de la venta, el registro de entradas y la actualización del historial de puntos deben realizarse como una sola operación. Si ocurre un error durante el proceso, la operación debe cancelarse para evitar inconsistencias.

El sistema podrá apoyarse en procedimientos almacenados o disparadores para automatizar ciertas reglas, como el control de puntos de fidelidad. Sin embargo, el uso de triggers será evaluado según el nivel de complejidad permitido y el tiempo disponible.

---

## Decisiones iniciales tomadas

Durante esta etapa del análisis, el equipo tomó las siguientes decisiones iniciales:

- El sistema será interno para empleados del cine.
- Los clientes no iniciarán sesión.
- Los clientes serán registrados para asociarlos a ventas y puntos de fidelidad.
- Los empleados podrán tener usuarios de acceso.
- Los usuarios tendrán roles para controlar permisos.
- El rol del usuario determinará las funciones disponibles dentro del sistema.
- El control de butacas físicas será parte del alcance principal.
- El historial de puntos será parte del alcance principal.
- Las ventas anuladas o devueltas no se eliminarán físicamente.
- El módulo de cafetería queda como extensión futura.
- El modelo conceptual iniciará con 13 entidades principales.

---

## Decisiones pendientes

Durante las próximas etapas del diseño, el equipo deberá definir:

- Si las butacas se seleccionarán visualmente o mediante una lista.
- Si una venta podrá incluir entradas de una misma función o de varias funciones.
- Si las ventas sin cliente registrado se manejarán mediante un cliente genérico llamado “Consumidor final”.
- Cómo se calcularán exactamente los puntos de fidelidad en el modelo físico.
- Si la entrada gratuita se aplicará en la décima compra o en la compra siguiente, según la interpretación final de la regla.
- Qué tipos de entrada se manejarán inicialmente.
- Si se implementarán cargos por butaca VIP desde la primera versión.
- Qué reportes o consultas se incluirán.
- Qué parte será implementada primero en Java.
- Qué ajustes serán necesarios según los criterios oficiales del profesor.

---

## Alcance recomendado para tres semanas

Para mantener el proyecto dentro de un tiempo razonable, se recomienda trabajar por prioridades.

### Alcance principal

- Clientes.
- Empleados.
- Usuarios.
- Películas.
- Géneros.
- Salas.
- Butacas.
- Funciones.
- Ventas.
- Entradas.
- Tipos de entrada.
- Métodos de pago.
- Programa de fidelidad.
- Historial de puntos.

### Alcance complementario

- Tarifas por horario o tipo de sala.
- Ventas anuladas o devueltas.
- Reportes o consultas básicas.
- Roles diferenciados con permisos específicos.

### Alcance opcional

- Cafetería.
- Productos.
- Inventario.
- Combos.
- Detalle de productos vendidos.
- Reservas.

De esta forma, el proyecto mantiene una base sólida y permite agregar complejidad sin comprometer el funcionamiento principal.

---

## Estado actual del análisis

- [x] Propuesta base revisada.
- [x] Problema identificado.
- [x] Objetivo general definido.
- [x] Objetivos específicos definidos.
- [x] Alcance inicial definido.
- [x] Valor agregado identificado.
- [x] Lógica de empleados, usuarios y clientes aclarada.
- [x] Entidades principales iniciales definidas.
- [x] Reglas de negocio iniciales identificadas.
- [x] Módulos opcionales definidos como extensión futura.
- [ ] Ajustes finales según criterios oficiales del profesor.

---

## Próximo paso

El siguiente paso del proyecto es elaborar el modelo conceptual del sistema.

En esta etapa se representarán las entidades principales, sus relaciones y cardinalidades, sin entrar todavía en detalles técnicos como tipos de datos, llaves primarias, llaves foráneas o sentencias SQL.

---

## Conclusión inicial

El sistema propuesto busca resolver problemas operativos reales de un cine, especialmente la sobreventa de entradas y el control manual del programa de fidelidad. A partir de la propuesta base, el diseño se fortalecerá con el manejo de butacas físicas, historial de puntos, tipos de entrada, métodos de pago, roles de usuario y control de estados de venta.

Este enfoque permite construir una base de datos más robusta, con reglas claras de integridad y trazabilidad. Al mismo tiempo, se mantiene un alcance manejable para ser desarrollado en un período aproximado de tres semanas.

El análisis inicial servirá como punto de partida para definir y representar el modelo conceptual, el cual permitirá continuar posteriormente con el modelo lógico, modelo físico, scripts SQL e implementación del sistema en Java.

\# Análisis inicial del sistema de cine



\## Propuesta base



La propuesta inicial del proyecto plantea el desarrollo de un sistema para un cine que actualmente maneja sus ventas de entradas de forma manual o mediante un sistema básico que solo registra transacciones. Esta situación ha generado problemas operativos importantes, especialmente en el control de disponibilidad de las salas y en el manejo del programa de fidelidad de los clientes.



Uno de los problemas principales es que los empleados no tienen visibilidad en tiempo real del aforo disponible para cada función. Como consecuencia, en varias ocasiones se han vendido más entradas que butacas disponibles, provocando quejas de clientes, devoluciones de dinero y una mala experiencia de servicio.



Otro problema identificado es el manejo manual del programa de fidelidad. El cine ofrece una promoción donde, por cada 10 entradas compradas, la siguiente entrada es gratuita. Actualmente, este control se realiza de forma manual mediante la revisión de boletos físicos o registros poco confiables. Esto puede generar errores de cálculo, fraudes por parte de clientes y pérdida de ingresos para el cine.



\## Problema identificado



El cine necesita un sistema que permita controlar de forma organizada y segura la venta de entradas, la disponibilidad de butacas por función y el programa de fidelidad de los clientes.



El sistema actual presenta varias debilidades:



\- No controla en tiempo real la disponibilidad de butacas.

\- Permite que se vendan más entradas que la capacidad real de una sala.

\- No evita que una misma butaca pueda asignarse dos veces para una misma función.

\- El control de fidelidad depende demasiado del cajero.

\- No existe un historial claro de los puntos ganados y canjeados por los clientes.

\- No se tiene suficiente trazabilidad sobre ventas anuladas, entradas gratuitas o beneficios aplicados.

\- El sistema no contempla diferentes tipos de entradas ni variaciones de precio según horario, tipo de cliente o tipo de sala.



Estos problemas afectan la eficiencia operativa del cine, la experiencia del cliente y el control interno de los ingresos.



\## Objetivo general



Diseñar y desarrollar un sistema de gestión para un cine que permita controlar funciones, butacas, ventas de entradas y programa de fidelidad, utilizando una base de datos relacional que garantice mayor organización, integridad y seguridad en el manejo de la información.



\## Objetivos específicos



\- Registrar clientes del cine.

\- Registrar empleados o cajeros responsables de las ventas.

\- Registrar películas y sus géneros.

\- Registrar salas y sus butacas físicas.

\- Programar funciones por película, sala, fecha y horario.

\- Permitir la venta de entradas para funciones específicas.

\- Evitar la sobreventa de entradas.

\- Evitar que una misma butaca sea vendida dos veces para la misma función.

\- Registrar diferentes tipos de entrada, como adulto, niño, estudiante o adulto mayor.

\- Manejar precios según tipo de entrada, horario o tipo de sala.

\- Automatizar el control del programa de fidelidad.

\- Registrar un historial de puntos ganados y canjeados por los clientes.

\- Permitir el registro de entradas gratuitas cuando aplique el beneficio de fidelidad.

\- Mantener trazabilidad de ventas activas, anuladas o devueltas.

\- Diseñar una estructura que pueda ampliarse con un módulo de cafetería si el tiempo lo permite.



\## Alcance del proyecto



El sistema estará enfocado principalmente en la gestión de ventas de entradas para un cine. Se trabajará con clientes, empleados, películas, salas, butacas, funciones, ventas, entradas, métodos de pago, tipos de entrada y programa de fidelidad.



Como valor agregado, el diseño incluirá el manejo de butacas físicas por sala, de manera que cada entrada esté asociada a una butaca específica dentro de una función. Esto permitirá controlar la disponibilidad con mayor precisión y evitar duplicidades.



También se incluirá un sistema de historial de puntos para el programa de fidelidad. En lugar de guardar solamente un campo con los puntos acumulados del cliente, se registrarán los movimientos de puntos ganados y canjeados. Esto permitirá auditar el beneficio aplicado y evitar fraudes o errores de cálculo.



El proyecto podrá considerar un módulo complementario de cafetería, con productos, inventario y detalle de productos vendidos. Sin embargo, este módulo será considerado como una extensión del sistema y no como el núcleo principal del proyecto, ya que el enfoque principal será la venta de entradas y el control de fidelidad.



\## Valor agregado del diseño



Además de cumplir con la propuesta base, nuestro diseño agrega varios elementos que hacen el sistema más completo y cercano a un cine real.



\### 1. Gestión avanzada de butacas



El sistema no se limitará a contar la cantidad total de entradas vendidas. También manejará un mapa de butacas físicas por sala, donde cada butaca tendrá fila, número y tipo.



Cada entrada vendida estará relacionada con una función y una butaca específica. Para evitar duplicidades, se propondrá una restricción de integridad que impida vender la misma butaca dos veces en la misma función.



Ejemplo de regla:



Una misma combinación de función y butaca no puede repetirse en la venta de entradas.



\### 2. Programa de fidelidad con historial



El sistema no reiniciará los puntos del cliente sin dejar evidencia. En su lugar, se manejará un historial de puntos donde se registren los puntos ganados y canjeados por cada cliente.



Esto permitirá saber cuándo un cliente ganó puntos, cuándo los usó y a qué venta estuvo asociado el movimiento. De esta forma, el sistema será más seguro y auditable.



\### 3. Tipos de entrada y tarifas



El sistema podrá manejar distintos tipos de entradas, como adulto, niño, estudiante o adulto mayor. Esto permitirá aplicar precios diferentes según el tipo de cliente o entrada.



También se podrá considerar una estructura de tarifas según horario, día o tipo de sala. Por ejemplo, funciones nocturnas, fines de semana o salas VIP podrían tener precios diferentes.



\### 4. Control de ventas anuladas o devueltas



Las ventas no deben eliminarse directamente de la base de datos. En caso de anulación o devolución, la venta cambiará de estado. Esto permite mantener el historial de operaciones y mejorar el control interno.



Estados posibles:



\- Activa

\- Anulada

\- Devuelta



\### 5. Posible módulo de cafetería



Como extensión opcional, el sistema puede incluir un módulo de cafetería para registrar productos como palomitas, refrescos y combos. Esta parte permitiría que una venta general incluya tanto entradas como productos de cafetería.



Sin embargo, este módulo será evaluado según el tiempo disponible y la complejidad del desarrollo.



\## Escenario esperado



Un cliente llamado María tiene 9 puntos acumulados en el programa de fidelidad. María llega a la taquilla y desea adquirir una entrada para una función de las 8:00 PM en la Sala 2.



El cajero ingresa el ID de María en el sistema y selecciona la función correspondiente. Luego, el sistema muestra las butacas disponibles para esa función. María selecciona una butaca específica.



Antes de registrar la venta, el sistema valida que la butaca seleccionada no haya sido vendida previamente para esa misma función. También verifica los puntos acumulados de María a través del historial de puntos.



Como María tiene 9 puntos acumulados y la regla de fidelidad indica que cada 10 entradas compradas permiten obtener una entrada gratuita, el sistema aplica automáticamente el beneficio correspondiente. La entrada se registra con descuento del 100%, la venta queda marcada como gratuita y se registra el movimiento de canje en el historial de puntos.



La operación queda almacenada con los datos del cliente, el empleado que realizó la venta, la función, la butaca seleccionada, el tipo de entrada y el estado de la venta.



\## Entidades iniciales candidatas



A partir del análisis inicial, se identifican las siguientes entidades principales:



\- Cliente

\- Empleado

\- Usuario

\- Película

\- Género

\- Sala

\- Butaca

\- Función

\- Venta

\- Entrada

\- Tipo de entrada

\- Método de pago

\- Historial de puntos



También se consideran como entidades opcionales o de extensión:



\- Producto

\- Inventario

\- Combo

\- Detalle de productos

\- Tarifa

\- Reserva



\## Reglas de negocio iniciales



\- Una película puede tener varias funciones.

\- Una función pertenece a una sola película.

\- Una función se realiza en una sola sala.

\- Una sala puede tener muchas butacas.

\- Una butaca pertenece a una sola sala.

\- Una venta puede incluir una o varias entradas.

\- Cada entrada pertenece a una función específica.

\- Cada entrada debe estar asociada a una butaca.

\- Una misma butaca no puede venderse dos veces para la misma función.

\- Cada venta debe estar asociada a un cliente, excepto si se decide permitir ventas a clientes no registrados.

\- Cada venta debe ser registrada por un empleado.

\- Cada entrada puede tener un tipo de entrada.

\- El precio final de una entrada puede variar según el tipo de entrada, el horario o la sala.

\- El cliente acumula puntos por entradas pagadas.

\- Las entradas gratuitas por fidelidad no deben acumular puntos.

\- El uso de puntos debe registrarse en el historial de puntos.

\- Una venta anulada o devuelta no se elimina, solo cambia de estado.

\- Si una venta se anula, las butacas asociadas deben volver a estar disponibles.

\- Si se incluye cafetería, una venta puede contener entradas y productos.



\## Consideraciones técnicas



El sistema deberá utilizar una base de datos relacional. Para garantizar la integridad de los datos, se propondrán restricciones como llaves primarias, llaves foráneas y restricciones únicas.



Una restricción importante será evitar que una misma butaca se repita para la misma función. Esto puede lograrse mediante una restricción única compuesta entre la función y la butaca.



También se considerará el uso de transacciones para que el proceso de venta sea seguro. La verificación de disponibilidad y el registro de la venta deben realizarse como una sola operación. Si ocurre un error durante el proceso, la operación debe cancelarse para evitar inconsistencias.



El sistema podrá apoyarse en procedimientos almacenados o disparadores para automatizar ciertas reglas, como el control de puntos de fidelidad. Sin embargo, el uso de triggers será evaluado según el nivel de complejidad permitido y el tiempo disponible.



\## Decisiones pendientes



Durante las próximas etapas del diseño, el equipo deberá definir:



\- Si las butacas se seleccionarán visualmente o mediante una lista.

\- Si una venta podrá incluir una o varias entradas.

\- Si se permitirá venta a clientes no registrados.

\- Cómo se calcularán exactamente los puntos de fidelidad.

\- Si la entrada gratuita se aplicará en la décima compra o en la compra siguiente.

\- Qué tipos de entrada se manejarán.

\- Si se implementarán tarifas por horario, día o tipo de sala.

\- Si el módulo de cafetería será parte del alcance obligatorio o quedará como extensión.

\- Qué reportes o consultas se incluirán.

\- Qué parte será implementada primero en Java.



\## Alcance recomendado para tres semanas



Para mantener el proyecto dentro de un tiempo razonable, se recomienda trabajar por prioridades.



\### Alcance principal



\- Clientes.

\- Empleados o usuarios.

\- Películas.

\- Géneros.

\- Salas.

\- Butacas.

\- Funciones.

\- Ventas.

\- Entradas.

\- Métodos de pago.

\- Programa de fidelidad.

\- Historial de puntos.



\### Alcance complementario



\- Tipos de entrada.

\- Tarifas por horario o tipo de sala.

\- Ventas anuladas o devueltas.



\### Alcance opcional



\- Cafetería.

\- Productos.

\- Inventario.

\- Combos.

\- Detalle de productos vendidos.



De esta forma, el proyecto mantiene una base sólida y permite agregar complejidad sin comprometer el funcionamiento principal.



\## Conclusión inicial



El sistema propuesto busca resolver problemas operativos reales de un cine, especialmente la sobreventa de entradas y el control manual del programa de fidelidad. A partir de la propuesta base, el diseño se fortalecerá con el manejo de butacas físicas, historial de puntos, tipos de entrada, tarifas y control de estados de venta.



Este enfoque permite construir una base de datos más robusta, con reglas claras de integridad y trazabilidad. Al mismo tiempo, se mantiene un alcance manejable para ser desarrollado en un período aproximado de tres semanas.



El análisis inicial servirá como punto de partida para definir los requerimientos, reglas de negocio y modelos conceptual, lógico y físico del sistema.


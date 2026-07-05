\# Especificación de Requerimientos del Sistema







\## Requerimientos funcionales



\### RF-01: Gestión de clientes



El sistema debe permitir registrar, consultar y actualizar clientes del cine. Cada cliente tendrá un identificador único que permitirá asociarlo con sus compras y con el programa de fidelidad.



\### RF-02: Gestión de empleados y usuarios



El sistema debe permitir registrar empleados, especialmente cajeros o administradores responsables de las operaciones del sistema. Cada usuario podrá tener un rol asignado para controlar sus permisos dentro del sistema.



\### RF-03: Gestión de películas y géneros



El sistema debe permitir registrar películas y asociarlas a un género cinematográfico. Cada película podrá ser programada en una o varias funciones.



\### RF-04: Gestión de salas y butacas



El sistema debe permitir registrar salas y sus butacas físicas. Cada butaca deberá tener una fila, un número y un tipo, como Regular o VIP.



\### RF-05: Programación de funciones



El sistema debe permitir programar funciones asociando una película, una sala, una fecha, una hora y una tarifa base. Una película podrá tener varias funciones en diferentes horarios o salas.



\### RF-06: Consulta de disponibilidad



El sistema debe permitir consultar la disponibilidad de butacas para una función específica antes de realizar una venta. Las butacas vendidas o reservadas no deberán aparecer como disponibles.



\### RF-07: Venta de entradas



El sistema debe permitir registrar ventas de entradas. Una venta podrá incluir una o varias entradas, y cada entrada deberá estar asociada a una función, una butaca y un tipo de entrada.



\### RF-08: Tipos de entrada



El sistema debe permitir manejar diferentes tipos de entrada, como Adulto, Niño, Estudiante y Adulto Mayor. El precio final de la entrada podrá variar según el tipo seleccionado.



\### RF-09: Métodos de pago



El sistema debe permitir registrar el método de pago utilizado en cada venta, como efectivo, tarjeta u otro método definido por el equipo.



\### RF-10: Control de estados de venta



El sistema debe permitir cambiar el estado de una venta a Activa, Anulada o Devuelta sin eliminar el registro de la base de datos. Si una venta es anulada o devuelta, las butacas asociadas deberán volver a estar disponibles.



\### RF-11: Programa de fidelidad



El sistema debe manejar un programa de fidelidad donde el cliente acumule puntos por entradas compradas. Cuando el cliente cumpla con la regla establecida, el sistema deberá aplicar una entrada gratuita o descuento del 100%.



\### RF-12: Historial de puntos



El sistema debe registrar los movimientos de puntos de cada cliente, incluyendo puntos ganados, puntos canjeados, fecha del movimiento y venta asociada. Esto permitirá mantener trazabilidad sobre el programa de fidelidad.



\### RF-13: Aplicación automática del beneficio de fidelidad



Si un cliente tiene 9 puntos acumulados al momento de realizar una nueva compra, el sistema deberá aplicar el beneficio de fidelidad a esa entrada, registrándola con un 100% de descuento y generando el movimiento correspondiente en el historial de puntos.



\### RF-14: Reportes o consultas básicas



El sistema debe permitir realizar consultas básicas relacionadas con funciones, ventas, clientes, butacas ocupadas y puntos de fidelidad.













\## Requerimientos no funcionales



\### RNF-01: Integridad de la información



La base de datos debe garantizar la integridad de los datos mediante llaves primarias, llaves foráneas, restricciones `NOT NULL` y restricciones únicas cuando sea necesario.



\### RNF-02: Exclusividad de butacas



La base de datos debe impedir que una misma butaca sea vendida dos veces para la misma función. Esto se logrará mediante una restricción única compuesta entre la función y la butaca.



\### RNF-03: Persistencia de la información



Toda la información importante del sistema deberá almacenarse en una base de datos relacional, evitando depender de registros manuales o archivos externos.



\### RNF-04: Trazabilidad



Las ventas, devoluciones, anulaciones y movimientos de puntos deberán conservarse como historial. No se deberán eliminar registros financieros o movimientos de fidelidad de forma permanente.



\### RNF-05: Seguridad transaccional



El proceso de venta debe realizarse de forma transaccional. La validación de disponibilidad, el registro de la venta, la asignación de butaca y la actualización de puntos deberán ejecutarse como una sola operación. Si ocurre un error, la operación completa deberá cancelarse.



\### RNF-06: Consistencia de estados



Las ventas, entradas y butacas deberán manejar estados claros para evitar inconsistencias. Por ejemplo, una butaca asociada a una venta anulada deberá volver a estar disponible.



\### RNF-07: Facilidad de uso



El sistema debe tener una interfaz comprensible para el usuario, especialmente para el cajero, permitiendo registrar ventas y consultar disponibilidad de forma sencilla.



\### RNF-08: Escalabilidad del diseño



La base de datos debe diseñarse de forma que permita agregar módulos futuros, como cafetería, promociones o reservas, sin tener que rehacer todo el modelo.


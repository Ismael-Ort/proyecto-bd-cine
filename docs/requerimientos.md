# Especificación de Requerimientos del Sistema

Este documento presenta los requerimientos funcionales y no funcionales del Sistema de Gestión de Cine. Los requerimientos fueron definidos a partir del análisis inicial del proyecto y servirán como base para elaborar los modelos conceptual, lógico y físico de la base de datos.

---

## Requerimientos funcionales

### RF-01: Gestión de clientes

El sistema debe permitir registrar, consultar y actualizar clientes del cine. Cada cliente tendrá un identificador único que permitirá asociarlo con sus compras y con el programa de fidelidad.

Los clientes podrán ser registrados por un empleado (venta por taquilla) o registrarse ellos mismos creando un usuario (venta en línea). El sistema debe distinguir entre cliente registrado y consumidor final (cliente genérico sin datos completos, usado en ventas por taquilla sin registro).

### RF-02: Gestión de empleados y usuarios

El sistema debe permitir registrar empleados responsables de las operaciones del cine. También debe permitir crear usuarios de acceso asociados a empleados (para operar el sistema interno) y usuarios de acceso asociados a clientes (para comprar en línea).

Cada usuario podrá tener un rol asignado, como administrador, cajero o supervisor, para controlar sus permisos dentro del sistema.

### RF-02.1: Gestión de roles

El sistema debe permitir asociar un usuario a un cliente registrado, siendo este usuario obligatorio únicamente si el cliente desea comprar en línea.

### RF-03: Gestión de películas y géneros

El sistema debe permitir registrar películas y asociarlas a un género cinematográfico. Cada película podrá ser programada en una o varias funciones.

### RF-04: Gestión de salas y butacas

El sistema debe permitir registrar salas y sus butacas físicas. Cada butaca deberá tener una fila, un número y un tipo, como Regular o VIP.

### RF-05: Programación de funciones

El sistema debe permitir programar funciones asociando una película, una sala, una fecha, una hora y una tarifa base. Una película podrá tener varias funciones en diferentes horarios o salas.

### RF-06: Consulta de disponibilidad

El sistema debe permitir consultar la disponibilidad de butacas para una función específica antes de realizar una venta. Las butacas vendidas no deberán aparecer como disponibles. La exclusividad de la butaca debe garantizarse independientemente del canal (taquilla o en línea) por el cual se realice la consulta o la venta.

Si en una etapa futura se implementan reservas, las butacas reservadas también deberán considerarse no disponibles.

### RF-07: Venta de entradas

El sistema debe permitir registrar ventas de entradas. Una venta podrá incluir una o varias entradas, y cada entrada deberá estar asociada a una función, una butaca y un tipo de entrada.

### RF-08: Tipos de entrada

El sistema debe permitir manejar diferentes tipos de entrada, como Adulto, Niño, Estudiante y Adulto Mayor. El precio final de la entrada podrá variar según el tipo seleccionado.

### RF-09: Métodos de pago

El sistema debe permitir registrar el método de pago utilizado en cada venta, como efectivo, tarjeta u otro método definido por el equipo.

### RF-10: Control de estados de venta

El sistema debe permitir cambiar el estado de una venta a Activa, Anulada o Devuelta sin eliminar el registro de la base de datos.

Si una venta es anulada o devuelta, las entradas asociadas deberán cambiar de estado y las butacas correspondientes deberán volver a estar disponibles para futuras consultas.

### RF-11: Programa de fidelidad

El sistema debe manejar un programa de fidelidad donde el cliente acumule puntos por entradas compradas y pagadas. Cuando el cliente cumpla con la regla establecida, el sistema deberá aplicar una entrada gratuita o descuento del 100%.

### RF-12: Historial de puntos

El sistema debe registrar los movimientos de puntos de cada cliente, incluyendo puntos ganados, puntos canjeados, fecha del movimiento y venta asociada. Esto permitirá mantener trazabilidad sobre el programa de fidelidad.

### RF-13: Aplicación automática del beneficio de fidelidad

Si un cliente tiene 9 puntos acumulados antes de realizar una nueva compra, el sistema deberá aplicar el beneficio de fidelidad a una entrada, registrándola con un 100% de descuento y generando el movimiento correspondiente en el historial de puntos.

La entrada gratuita no deberá generar nuevos puntos.

### RF-14: Reportes o consultas básicas

El sistema debe permitir realizar consultas básicas relacionadas con funciones, ventas, clientes, butacas ocupadas, butacas disponibles y puntos de fidelidad.

### RF-15: Acceso de usuarios

El sistema debe permitir el acceso mediante usuarios y contraseñas. Los usuarios podrán estar asociados a clientes o empleados, según el rol asignado.

### RF-16: Roles de usuario

El sistema debe manejar roles de usuario para controlar los permisos dentro del sistema. Los roles iniciales serán ADMIN, CAJERO, SUPERVISOR y CLIENTE.

### RF-17: Venta por taquilla

El sistema debe permitir registrar ventas presenciales en taquilla. En este caso, la venta será registrada por un empleado autorizado.

### RF-18: Venta en línea

El sistema debe permitir que un cliente con usuario pueda consultar funciones, seleccionar una butaca disponible y realizar una compra en línea.

### RF-19: Canal de venta

El sistema debe registrar el canal por el cual se realiza cada venta (TAQUILLA o EN_LINEA) mediante un atributo obligatorio en la entidad Venta. Si el canal es TAQUILLA, la venta debe estar asociada a un empleado. Si el canal es EN_LINEA, la venta puede no tener empleado asociado, pero debe estar asociada a un cliente que haya iniciado sesión mediante su usuario.

---

## Requerimientos no funcionales

### RNF-01: Integridad de la información

La base de datos debe garantizar la integridad de los datos mediante llaves primarias, llaves foráneas, restricciones `NOT NULL` y restricciones únicas cuando sea necesario.

### RNF-02: Exclusividad de butacas

La base de datos y la lógica del sistema deben impedir que una misma butaca esté asociada a más de una entrada activa dentro de la misma función.

### RNF-03: Persistencia de la información

Toda la información importante del sistema deberá almacenarse en una base de datos relacional, evitando depender de registros manuales o archivos externos.

### RNF-04: Trazabilidad

Las ventas, devoluciones, anulaciones y movimientos de puntos deberán conservarse como historial. No se deberán eliminar registros financieros o movimientos de fidelidad de forma permanente.

### RNF-05: Seguridad transaccional

El proceso de venta debe realizarse de forma transaccional. La validación de disponibilidad, el registro de la venta, la asignación de butaca y la actualización de puntos deberán ejecutarse como una sola operación. Si ocurre un error, la operación completa deberá cancelarse.

### RNF-06: Consistencia de estados

Las ventas y entradas deberán manejar estados claros para evitar inconsistencias. Por ejemplo, una entrada asociada a una venta anulada o devuelta no debe considerarse como ocupación activa de una butaca.

### RNF-07: Facilidad de uso

El sistema debe tener una interfaz comprensible para el usuario, especialmente para el cajero, permitiendo registrar ventas y consultar disponibilidad de forma sencilla.

### RNF-08: Escalabilidad del diseño

La base de datos debe diseñarse de forma que permita agregar módulos futuros, como cafetería, promociones o reservas, sin tener que rehacer todo el modelo.

### RNF-09: Escalabilidad hacia ventas presenciales y en línea

El diseño de la base de datos debe permitir manejar tanto ventas presenciales como ventas en línea, sin duplicar la estructura principal de ventas, entradas, funciones, butacas y clientes.

---

## Estado del documento

- [x] Requerimientos funcionales definidos.
- [x] Requerimientos no funcionales definidos.
- [x] Lógica de clientes y empleados aclarada.
- [x] Alcance principal definido.
- [ ] Ajustes finales según criterios oficiales del profesor.

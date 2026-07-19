# Reglas de Negocio - Sistema de Gestión de Cine

Este documento establece las reglas de negocio que deben cumplirse para garantizar el correcto funcionamiento del Sistema de Gestión de Cine. Estas reglas son consistentes con el modelo conceptual y el modelo lógico del proyecto.

---

# 1. Infraestructura y cartelera

## BR-01. Registro de salas
Toda función deberá programarse en una sala previamente registrada.

## BR-02. Registro de butacas
Una sala deberá tener butacas registradas antes de poder programar funciones.

## BR-03. Programación de funciones
Toda función deberá estar asociada a una película, una sala, una fecha, una hora y una tarifa base.

## BR-04. Solapamiento de funciones
No podrán existir dos funciones programadas simultáneamente en la misma sala.

---

# 2. Usuarios, clientes y empleados

## BR-05. Usuarios del sistema
El sistema manejará una entidad Usuario que permitirá el acceso tanto de clientes como de empleados.

## BR-06. Roles
Todo usuario deberá tener uno de los siguientes roles:
- Administrador
- Cajero
- Cliente

## BR-07. Asociación de usuarios
Un usuario podrá estar asociado a un cliente o a un empleado, pero no a ambos al mismo tiempo.

## BR-08. Empleados
Los empleados realizarán las operaciones internas del sistema según los permisos de su rol.

## BR-09. Clientes
Los clientes podrán realizar compras en línea y participar en el programa de fidelidad.

---

# 3. Ventas

## BR-10. Canales de venta
Toda venta deberá indicar el canal por el cual fue realizada:
- Taquilla
- En línea

## BR-11. Venta en taquilla
Las ventas en taquilla deberán estar asociadas a un empleado.

## BR-12. Venta en línea
Las ventas en línea podrán realizarse sin empleado y estarán asociadas al cliente autenticado cuando corresponda.

## BR-13. Cliente en la venta
Una venta podrá asociarse a un cliente registrado o realizarse sin cliente.

## BR-14. Venta con múltiples entradas
Una venta podrá contener una o varias entradas.

---

# 4. Entradas y butacas

## BR-15. Asociación de entrada
Cada entrada deberá estar asociada a una venta, una función, una butaca y un tipo de entrada.

## BR-16. Exclusividad de butacas
Una misma butaca no podrá venderse dos veces para una misma función.

## BR-17. Disponibilidad
Solo podrán seleccionarse butacas disponibles para la función correspondiente.

## BR-18. Liberación de butacas
Cuando una entrada sea cancelada, la butaca volverá a estar disponible.

---

# 5. Tipos de entrada y precios

## BR-19. Tipo de entrada
Toda entrada deberá tener un tipo de entrada.

## BR-20. Precio final
El precio final se calculará a partir de la tarifa base de la función y del descuento correspondiente al tipo de entrada.

---

# 6. Programa de fidelidad

## BR-21. Acumulación de puntos
Solo las entradas pagadas generarán puntos de fidelidad.

## BR-22. Entradas gratuitas
Las entradas obtenidas mediante el programa de fidelidad tendrán precio final igual a cero y no acumularán nuevos puntos.

## BR-23. Historial de puntos
Todo movimiento de acumulación o canje deberá registrarse en el historial de puntos.

## BR-24. Aplicación del beneficio
Cuando un cliente cumpla la condición establecida por el programa de fidelidad, el sistema aplicará automáticamente el beneficio correspondiente.

---

# 7. Estados y transacciones

## BR-25. Estados de venta
Las ventas manejarán los estados:
- Pendiente
- Completada
- Cancelada

## BR-26. Estados de entrada
Las entradas manejarán los estados:
- Reservada
- Pagada
- Cancelada
- Utilizada

## BR-27. Conservación del historial
Las ventas, entradas y movimientos de puntos no deberán eliminarse físicamente de la base de datos.

## BR-28. Transacciones
El registro de una venta deberá ejecutarse como una única transacción. Si ocurre un error durante el proceso, todas las operaciones deberán revertirse.

---

# Estado del documento

- [x] Reglas de infraestructura definidas.
- [x] Reglas de usuarios definidas.
- [x] Reglas de ventas definidas.
- [x] Reglas de entradas y butacas definidas.
- [x] Reglas del programa de fidelidad definidas.
- [x] Reglas de estados y transacciones definidas.
- [x] Documento consistente con el modelo conceptual.
- [x] Documento consistente con el modelo lógico.

---

# Conclusión

Las reglas de negocio establecen las restricciones necesarias para garantizar la integridad, consistencia y trazabilidad del Sistema de Gestión de Cine. Estas reglas sirven como base para la implementación de la base de datos y el desarrollo de la aplicación, asegurando que las operaciones del sistema se realicen de forma segura y conforme al diseño definido.

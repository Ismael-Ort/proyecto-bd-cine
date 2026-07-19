# Especificación de Requerimientos del Sistema

Este documento define los requerimientos funcionales y no funcionales del Sistema de Gestión de Cine. Los requerimientos fueron elaborados a partir del análisis del problema y son consistentes con el modelo conceptual y el modelo lógico desarrollados para el proyecto.

---

# Requerimientos funcionales

## RF-01. Gestión de clientes
El sistema deberá permitir registrar, consultar y actualizar clientes, asociando sus compras y el historial del programa de fidelidad.

## RF-02. Gestión de empleados
El sistema deberá permitir registrar, consultar y actualizar empleados responsables de las operaciones del cine.

## RF-03. Gestión de usuarios
El sistema deberá permitir crear cuentas de usuario asociadas a clientes o empleados. Cada usuario iniciará sesión mediante nombre de usuario y contraseña y tendrá un rol asignado.

## RF-04. Gestión de películas y géneros
El sistema deberá permitir registrar películas, registrar géneros y asociar una película con uno o varios géneros.

## RF-05. Gestión de salas y butacas
El sistema deberá permitir registrar salas y sus butacas, identificando cada butaca mediante su fila y número.

## RF-06. Programación de funciones
El sistema deberá permitir programar funciones indicando película, sala, fecha, hora y tarifa base.

## RF-07. Consulta de disponibilidad
El sistema deberá mostrar las butacas disponibles para cada función y evitar que una misma butaca pueda ser seleccionada dos veces para la misma función.

## RF-08. Venta de entradas
El sistema deberá permitir registrar ventas que contengan una o varias entradas. Cada entrada deberá estar asociada a una función, una butaca y un tipo de entrada.

## RF-09. Tipos de entrada
El sistema deberá permitir administrar distintos tipos de entrada y aplicar el descuento correspondiente para calcular el precio final.

## RF-10. Métodos de pago
El sistema deberá registrar el método de pago utilizado en cada venta.

## RF-11. Control de estados
El sistema deberá manejar estados para ventas y entradas, permitiendo conservar el historial sin eliminar registros.

## RF-12. Programa de fidelidad
El sistema deberá acumular puntos por entradas pagadas y permitir el canje de beneficios cuando corresponda.

## RF-13. Historial de puntos
El sistema deberá registrar todos los movimientos de acumulación y canje de puntos asociados a cada cliente y a la venta correspondiente.

## RF-14. Venta en taquilla y venta en línea
El sistema deberá permitir registrar ventas realizadas en taquilla por un empleado y ventas realizadas en línea por un cliente autenticado. Cada venta deberá registrar su canal (TAQUILLA o EN_LÍNEA).

## RF-15. Consultas
El sistema deberá permitir consultar funciones, películas, disponibilidad de butacas, ventas, clientes e historial de puntos.

## RF-16. Control de acceso
El sistema deberá controlar el acceso mediante los roles:
- Administrador
- Cajero
- Cliente

Cada rol tendrá permisos acordes con sus funciones.

---

# Requerimientos no funcionales

## RNF-01. Integridad de la información
La base de datos deberá garantizar la integridad mediante llaves primarias, llaves foráneas, restricciones de unicidad y validaciones de dominio.

## RNF-02. Exclusividad de butacas
No deberá existir más de una entrada para la misma combinación de función y butaca.

## RNF-03. Persistencia
La información deberá almacenarse en una base de datos relacional.

## RNF-04. Trazabilidad
Las ventas, entradas y movimientos de puntos deberán conservar su historial mediante estados y registros históricos.

## RNF-05. Seguridad transaccional
El proceso de venta deberá ejecutarse como una transacción para evitar inconsistencias.

## RNF-06. Facilidad de uso
La interfaz deberá ser sencilla e intuitiva para empleados y clientes.

## RNF-07. Escalabilidad
El diseño deberá permitir incorporar nuevos módulos como cafetería, promociones, inventario o reservas sin modificar la estructura principal.

## RNF-08. Compatibilidad del modelo
El sistema deberá soportar tanto ventas presenciales como ventas en línea utilizando la misma estructura de datos.

---

# Estado del documento

- [x] Requerimientos funcionales definidos.
- [x] Requerimientos no funcionales definidos.
- [x] Consistente con el modelo conceptual.
- [x] Consistente con el modelo lógico.
- [x] Documento listo para la etapa de implementación.

---

# Conclusión

Los requerimientos definidos establecen las funcionalidades y restricciones que deberá cumplir el Sistema de Gestión de Cine. Constituyen la base para el diseño de la base de datos, el desarrollo de la aplicación y las validaciones necesarias para garantizar el correcto funcionamiento del sistema.

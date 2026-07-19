# Análisis inicial del sistema de gestión para cine

## Propuesta base

El proyecto consiste en el diseño de un sistema de gestión para un cine que permita administrar de forma organizada y segura la venta de entradas, las funciones, las salas, las butacas, los clientes, los empleados y el programa de fidelidad. El objetivo principal es sustituir procesos manuales o sistemas limitados que generan errores y dificultan el control de las operaciones.

Actualmente el cine presenta problemas como la sobreventa de entradas, la asignación duplicada de butacas, el manejo manual del programa de fidelidad y la poca trazabilidad de las ventas. Además, no existe un control eficiente sobre los distintos tipos de entradas, los métodos de pago ni el historial de puntos de los clientes.

Con este proyecto se busca desarrollar una base de datos relacional que garantice la integridad de la información, mejore el control interno y optimice la gestión de todas las operaciones relacionadas con la venta de entradas.

## Problema identificado

El cine necesita un sistema que permita administrar eficientemente las ventas de entradas, controlar la disponibilidad de butacas en tiempo real y gestionar correctamente el programa de fidelidad.

Principales problemáticas:

- Falta de control en tiempo real de las butacas.
- Posibilidad de vender más entradas que la capacidad de la sala.
- Asignación duplicada de una misma butaca.
- Control manual del programa de fidelidad.
- Ausencia de historial de puntos.
- Escasa trazabilidad de ventas canceladas y beneficios.
- Falta de organización en tipos de entrada y métodos de pago.

## Objetivo general

Diseñar un sistema de gestión para un cine basado en una base de datos relacional que permita administrar películas, funciones, salas, butacas, clientes, empleados, usuarios, ventas, entradas y programa de fidelidad, garantizando integridad, organización, seguridad y trazabilidad de la información.

## Objetivos específicos

- Registrar clientes y empleados.
- Gestionar usuarios.
- Administrar películas y géneros.
- Registrar salas y butacas.
- Programar funciones.
- Controlar disponibilidad de butacas.
- Registrar ventas presenciales y en línea.
- Evitar la sobreventa.
- Evitar la duplicidad de butacas.
- Manejar distintos tipos de entrada.
- Registrar métodos de pago.
- Gestionar el historial de puntos.
- Mantener la trazabilidad mediante estados.
- Diseñar una estructura escalable.

## Alcance del proyecto

El sistema administrará clientes, empleados, usuarios, películas, géneros, salas, butacas, funciones, ventas, entradas, tipos de entrada, métodos de pago e historial de puntos.

Se contemplan dos canales de venta:

- **Venta en taquilla**, realizada por un empleado.
- **Venta en línea**, realizada por un cliente autenticado.

Cada entrada estará asociada a una función, una butaca y un tipo de entrada. El programa de fidelidad se gestionará mediante un historial de movimientos de acumulación y canje de puntos.

Como posible extensión futura podrán incorporarse módulos de cafetería, inventario, productos, promociones y reservas.

## Valor agregado del diseño

### Gestión de butacas

Cada sala posee butacas identificadas por fila y número. Una misma butaca no podrá venderse dos veces para una misma función.

### Programa de fidelidad

El sistema registrará un historial de puntos para conocer cuándo un cliente acumuló o canjeó beneficios y con qué venta estuvieron relacionados.

### Tipos de entrada

Se podrán manejar distintos tipos de entrada permitiendo aplicar descuentos según el tipo seleccionado.

### Control de estados

Las ventas y entradas utilizarán estados para representar su ciclo de vida sin eliminar físicamente los registros.

### Separación de responsabilidades

El sistema diferencia entre clientes, empleados y usuarios. Tanto clientes como empleados podrán tener una cuenta de usuario con permisos definidos por su rol.

## Lógica de acceso

La entidad **Usuario** permitirá autenticar tanto a empleados como a clientes.

Roles:
- Administrador
- Cajero
- Cliente

## Escenario de funcionamiento

Cuando un cliente desea comprar una entrada, ya sea en taquilla o en línea, el sistema muestra las butacas disponibles para la función seleccionada.

Al elegir una butaca, el sistema verifica que no haya sido asignada previamente, registra la venta, genera la entrada correspondiente y actualiza el historial de puntos cuando aplique. Si el cliente posee los puntos necesarios, el sistema registra el canje y genera la entrada gratuita.

## Entidades principales

- Cliente
- Empleado
- Usuario
- Película
- Género
- Película_Género
- Sala
- Butaca
- Función
- Tipo_Entrada
- Método_Pago
- Venta
- Entrada
- Historial_Puntos

## Reglas de negocio

- Una película puede pertenecer a varios géneros.
- Un género puede asociarse a varias películas.
- Una película puede tener múltiples funciones.
- Cada función pertenece a una película y a una sala.
- Una sala posee múltiples butacas.
- Una butaca pertenece a una única sala.
- Una venta puede contener una o varias entradas.
- Cada entrada pertenece a una venta.
- Cada entrada corresponde a una función y una butaca.
- Una misma butaca no puede venderse dos veces para la misma función.
- Una venta puede asociarse a un cliente o realizarse sin cliente.
- Una venta puede ser realizada por un empleado o mediante compra en línea.
- Un cliente y un empleado pueden tener una cuenta de usuario.
- Cada usuario posee un rol.
- El precio final depende de la tarifa base y del descuento del tipo de entrada.
- Los clientes acumulan puntos únicamente por entradas pagadas.
- Las entradas gratuitas no generan nuevos puntos.
- Todo movimiento de puntos debe registrarse.
- Las ventas y entradas cambian de estado en lugar de eliminarse.

## Consideraciones técnicas

La base de datos utilizará llaves primarias, llaves foráneas, restricciones de unicidad y validaciones de integridad. El proceso de venta deberá ejecutarse mediante transacciones para garantizar consistencia.

## Decisiones finales

- Ventas en taquilla y en línea.
- Clientes y empleados podrán tener usuarios.
- Roles para controlar permisos.
- Control de butacas como parte del alcance principal.
- Historial de puntos obligatorio.
- Conservación del historial mediante estados.
- Diseño preparado para futuras ampliaciones.

## Posibles extensiones futuras

- Cafetería.
- Productos.
- Inventario.
- Combos.
- Reservas.
- Promociones.
- Reportes.

## Conclusión

El sistema ofrece una solución integral para la gestión de un cine mediante un modelo de datos que controla funciones, salas, butacas, ventas, clientes y programa de fidelidad. El análisis constituye la base para el desarrollo del modelo conceptual, el modelo lógico y la implementación del sistema.

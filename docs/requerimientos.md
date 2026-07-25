# Especificación de Requerimientos del Sistema

Este documento define los requerimientos funcionales y no funcionales del Sistema de Gestión de Cine. Los requerimientos fueron elaborados a partir del análisis del problema y son consistentes con el modelo conceptual, el modelo lógico y el script `database/schema.sql` del proyecto.

---

# Requerimientos funcionales

## RF-01. Gestión de personas
El sistema deberá permitir registrar los datos personales (nombres, apellidos, documento, teléfono, correo) de cualquier individuo que participe como cliente, empleado o usuario, mediante una entidad Persona común.

## RF-02. Gestión de clientes
El sistema deberá permitir registrar, consultar y actualizar clientes a partir de una persona existente, asociando sus compras y el historial del programa de fidelidad.

## RF-03. Gestión de empleados
El sistema deberá permitir registrar, consultar y actualizar empleados, a partir de una persona existente, responsables de las operaciones del cine.

## RF-04. Gestión de usuarios
El sistema deberá permitir crear cuentas de usuario asociadas a una persona. Cada usuario iniciará sesión mediante nombre de usuario y contraseña y tendrá un rol asignado. El sistema deberá validar que el rol del usuario sea coherente con los registros (cliente/empleado) que tenga la persona asociada.

## RF-05. Gestión de películas y géneros
El sistema deberá permitir registrar películas, registrar géneros y asociar una película con uno o varios géneros mediante la tabla `pelicula_genero`.

## RF-06. Gestión de salas y butacas
El sistema deberá permitir registrar salas y sus butacas, identificando cada butaca mediante su fila y número.

## RF-07. Programación de funciones
El sistema deberá permitir programar funciones indicando película, sala, fecha, hora de inicio y tarifa base, e indicar opcionalmente el idioma de audio y de subtítulos.

## RF-08. Consulta de disponibilidad
El sistema deberá mostrar las butacas disponibles para cada función y evitar que una misma butaca pueda ser seleccionada dos veces para la misma función.

## RF-09. Venta de entradas
El sistema deberá permitir registrar ventas que contengan una o varias entradas. Cada entrada deberá estar asociada a una función, una butaca y un tipo de entrada.

## RF-10. Tipos de entrada
El sistema deberá permitir administrar distintos tipos de entrada y aplicar el descuento correspondiente para calcular el precio final.

## RF-11. Métodos de pago
El sistema deberá registrar el método de pago utilizado en cada venta.

## RF-12. Control de estados
El sistema deberá manejar estados para ventas y entradas, permitiendo conservar el historial sin eliminar registros.

## RF-13. Programa de fidelidad
El sistema deberá acumular puntos por entradas pagadas y permitir el canje de beneficios cuando corresponda.

## RF-14. Historial de puntos
El sistema deberá registrar todos los movimientos de acumulación y canje de puntos, asociando siempre el cliente y la venta correspondiente.

## RF-15. Venta en taquilla y venta en línea
El sistema deberá permitir registrar ventas realizadas en taquilla por un empleado y ventas realizadas en línea por un cliente autenticado, sin que estas últimas requieran un empleado asociado. Cada venta deberá registrar su canal (`TAQUILLA` o `EN_LINEA`).

## RF-16. Consultas
El sistema deberá permitir consultar funciones, películas, disponibilidad de butacas, ventas, clientes e historial de puntos.

## RF-17. Control de acceso
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
El sistema deberá soportar tanto ventas presenciales como ventas en línea utilizando la misma estructura de datos, permitiendo que la venta en línea no requiera un empleado asociado.

## RNF-09. Datos personales centralizados
El sistema deberá evitar la duplicación de datos personales, manteniéndolos únicamente en la entidad Persona y referenciándolos desde Cliente, Empleado y Usuario.

---

# Checklist de trazabilidad requerimiento → restricción física

| Requerimiento | Restricción(es) en `schema.sql` | Estado |
|---|---|---|
| RF-01 Gestión de personas | `persona` (PK, `uq_persona_documento`, `uq_persona_correo`) | [x] |
| RF-02 Gestión de clientes | `cliente` (`uq_cliente_persona`, `fk_cliente_persona`) | [x] |
| RF-03 Gestión de empleados | `empleado` (`uq_empleado_persona`, `fk_empleado_persona`) | [x] |
| RF-04 Gestión de usuarios | `usuario` (`chk_usuario_rol`, `fk_usuario_persona`) | [x] DB parcial / [ ] validación de rol en Java |
| RF-05 Películas y géneros | `pelicula_genero` (PK compuesta, FKs) | [x] |
| RF-06 Salas y butacas | `butaca` (`uq_butaca_sala_fila_numero`) | [x] |
| RF-07 Programación de funciones | `funcion` (`chk_funcion_tarifa`, `chk_funcion_estado`) | [x] |
| RF-08 Disponibilidad de butacas | `entrada` (`uq_entrada_funcion_butaca`) | [x] |
| RF-09 Venta de entradas | `entrada` (FKs a `venta`, `funcion`, `butaca`, `tipoentrada`) | [x] |
| RF-10 Tipos de entrada | `tipoentrada` (`chk_tipoentrada_descuento`) | [x] |
| RF-11 Métodos de pago | `venta.fk_venta_metodopago` | [x] |
| RF-12 Control de estados | `chk_venta_estado`, `chk_entrada_estado` | [x] |
| RF-13 Programa de fidelidad | `historial_puntos` (`chk_historial_tipo`, `chk_historial_cantidad_puntos`) | [x] |
| RF-14 Historial de puntos | `historial_puntos` (`id_cliente` y `id_venta` `NOT NULL`) | [x] |
| RF-15 Venta taquilla/en línea | `venta.chk_venta_canal_empleado` | [x] |
| RF-16 Consultas | No requiere restricción propia (solo `SELECT`) | [x] |
| RF-17 Control de acceso por rol | `usuario.chk_usuario_rol` | [x] DB parcial / [ ] permisos en Java |

---

# Estado del documento

- [x] Requerimientos funcionales definidos.
- [x] Requerimientos no funcionales definidos.
- [x] Checklist de trazabilidad requerimiento → restricción física definido.
- [x] Consistente con el modelo conceptual.
- [x] Consistente con el modelo lógico.
- [x] Documento listo para la etapa de implementación.

---

# Conclusión

Los requerimientos definidos establecen las funcionalidades y restricciones que deberá cumplir el Sistema de Gestión de Cine. Constituyen la base para el diseño de la base de datos, el desarrollo de la aplicación y las validaciones necesarias para garantizar el correcto funcionamiento del sistema.

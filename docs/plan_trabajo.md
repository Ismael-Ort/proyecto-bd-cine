# Plan de Trabajo - Sistema de Gestión de Cine

## Objetivo del plan

Este documento organiza el proceso de trabajo del proyecto final. La finalidad es avanzar de forma ordenada, iniciando con el análisis de la propuesta base y la definición del alcance, antes de pasar al diseño de modelos, implementación de la base de datos y desarrollo del sistema.

El plan podrá ajustarse cuando el profesor indique los criterios oficiales de evaluación o cuando durante el desarrollo se identifique alguna mejora necesaria en el diseño.

---

## Fase 1: Análisis inicial

En esta fase se revisó la propuesta base del sistema de cine para comprender el problema, sus causas y el resultado esperado.

Actividades principales:

- [x] Revisar la propuesta inicial.
- [x] Identificar los problemas principales del sistema actual.
- [x] Entender el escenario esperado.
- [x] Definir el objetivo general del sistema.
- [x] Determinar qué elementos de la propuesta son obligatorios.
- [x] Proponer posibles mejoras o valor agregado del equipo.
- [x] Definir qué aspectos todavía quedan pendientes de decisión.
- [x] Completar el documento de análisis inicial.

Estado: Hecho

---

## Fase 2: Definición del alcance

En esta fase se decidió qué partes del sistema serán trabajadas dentro del tiempo disponible y qué elementos quedarán como posibles extensiones.

Después de la revisión del enfoque inicial, el sistema fue ajustado para contemplar dos canales de venta: venta presencial en taquilla y venta en línea.

Actividades principales:

- [x] Definir el alcance principal del proyecto.
- [x] Definir qué funcionalidades son necesarias para cumplir la propuesta base.
- [x] Definir qué mejoras propias del equipo serán incluidas.
- [x] Definir qué elementos quedarán como opcionales.
- [x] Revisar si el alcance es posible de completar en el tiempo disponible.
- [x] Ajustar el enfoque del sistema para contemplar ventas por taquilla y ventas en línea.
- [x] Definir que la entidad Usuario será general para clientes y empleados.
- [x] Definir que el cliente podrá comprar como cliente registrado, cliente con usuario o consumidor final.
- [ ] Ajustar el alcance según los criterios oficiales del profesor.

Estado: En proceso

---

## Fase 3: Requerimientos y reglas de negocio

En esta fase se definieron las funciones que debe cumplir el sistema y las reglas que deben respetarse para que funcione correctamente.

Los requerimientos y reglas de negocio deberán reflejar el nuevo enfoque del sistema, tomando en cuenta que las ventas podrán realizarse por taquilla o en línea.

Actividades principales:

- [x] Definir requerimientos funcionales.
- [x] Definir requerimientos no funcionales.
- [x] Definir reglas de negocio.
- [x] Revisar reglas relacionadas con disponibilidad.
- [x] Revisar reglas relacionadas con ventas.
- [x] Revisar reglas relacionadas con fidelidad.
- [x] Revisar reglas relacionadas con estados o cambios operativos.
- [x] Verificar que los requerimientos estén alineados con el alcance definido.
- [x] Actualizar la lógica de usuarios para permitir acceso de empleados y clientes.
- [x] Actualizar la lógica de ventas para permitir ventas por taquilla y ventas en línea.
- [x] Definir la lógica de cliente registrado, cliente con usuario y consumidor final.
- [ ] Ajustar requerimientos y reglas según criterios oficiales del profesor.

Estado: En proceso

---

## Fase 4: Modelo conceptual

En esta fase se identificarán las entidades principales del sistema y las relaciones entre ellas, considerando el enfoque actualizado del proyecto: ventas presenciales en taquilla y ventas en línea realizadas por clientes con usuario.

El modelo conceptual no incluirá todavía tipos de datos, llaves primarias técnicas, llaves foráneas ni sentencias SQL. Su propósito será representar las entidades, sus atributos principales, las relaciones y las cardinalidades.

Actividades principales:

- [x] Identificar entidades candidatas a partir del análisis.
- [x] Seleccionar las entidades principales iniciales.
- [x] Ajustar el enfoque para permitir ventas por taquilla y ventas en línea.
- [x] Definir Usuario como entidad general de acceso para clientes y empleados.
- [x] Definir que el cliente puede ser registrado, consumidor final o cliente con usuario.
- [x] Definir que una venta puede tener canal TAQUILLA o EN_LINEA.
- [ ] Definir relaciones entre entidades.
- [ ] Establecer cardinalidades.
- [ ] Revisar si el modelo representa correctamente la propuesta.
- [ ] Corregir entidades o relaciones innecesarias.
- [ ] Guardar el modelo conceptual en la carpeta correspondiente.

Estado: En proceso

---

## Fase 5: Modelo lógico

En esta fase se transformará el modelo conceptual en una estructura relacional más formal.

Aquí las entidades del modelo conceptual pasarán a convertirse en tablas, y las relaciones se representarán mediante llaves primarias y llaves foráneas.

Actividades principales:

- [ ] Convertir entidades en relaciones o tablas.
- [ ] Definir atributos principales.
- [ ] Definir llaves primarias.
- [ ] Definir llaves foráneas.
- [ ] Resolver relaciones de uno a muchos.
- [ ] Resolver relaciones de muchos a muchos si existen.
- [ ] Definir cómo se representará la relación Usuario-Cliente.
- [ ] Definir cómo se representará la relación Usuario-Empleado.
- [ ] Definir cómo se manejará el canal de venta en la tabla Venta.
- [ ] Definir cómo se manejará el cliente consumidor final.
- [ ] Revisar normalización básica.
- [ ] Validar que el modelo lógico responda a las reglas de negocio.

Estado: Pendiente

---

## Fase 6: Modelo físico

En esta fase se preparará el diseño final de la base de datos para su implementación en el sistema gestor seleccionado.

El modelo físico incluirá nombres finales de tablas, columnas, tipos de datos, restricciones, índices y reglas de integridad.

Actividades principales:

- [ ] Definir nombres finales de tablas y columnas.
- [ ] Definir tipos de datos.
- [ ] Definir restricciones.
- [ ] Definir índices si son necesarios.
- [ ] Definir reglas de integridad.
- [ ] Definir restricciones para evitar duplicidad de butacas por función.
- [ ] Definir restricciones relacionadas con usuarios, roles y canales de venta.
- [ ] Preparar el script SQL de creación de la base de datos.
- [ ] Probar el script en el gestor de base de datos.
- [ ] Corregir errores del modelo físico.

Estado: Pendiente

---

## Fase 7: Implementación de la base de datos

En esta fase se implementará la base de datos de acuerdo con el modelo físico aprobado.

Actividades principales:

- [ ] Crear la base de datos.
- [ ] Ejecutar el script de creación de tablas.
- [ ] Insertar datos de prueba.
- [ ] Crear usuarios de prueba para empleados.
- [ ] Crear usuarios de prueba para clientes.
- [ ] Crear cliente registrado.
- [ ] Crear cliente consumidor final.
- [ ] Crear funciones, salas y butacas de prueba.
- [ ] Crear consultas de prueba.
- [ ] Validar restricciones principales.
- [ ] Probar ventas por taquilla.
- [ ] Probar ventas en línea.
- [ ] Probar los escenarios definidos en el análisis.
- [ ] Documentar resultados de pruebas.

Estado: Pendiente

---

## Fase 8: Desarrollo de la aplicación

En esta fase se desarrollará la aplicación conectada a la base de datos.

La aplicación deberá contemplar un acceso general de usuarios y mostrar opciones según el rol del usuario autenticado.

Actividades principales:

- [ ] Definir la estructura del proyecto de programación.
- [ ] Crear la conexión con la base de datos.
- [ ] Diseñar el inicio de sesión general.
- [ ] Validar el rol del usuario al iniciar sesión.
- [ ] Diseñar las pantallas principales según el rol.
- [ ] Programar módulos internos para empleados.
- [ ] Programar módulos de compra para clientes.
- [ ] Programar consulta de funciones disponibles.
- [ ] Programar selección de butacas.
- [ ] Programar ventas por taquilla.
- [ ] Programar ventas en línea.
- [ ] Validar que la aplicación respete las reglas de negocio.
- [ ] Probar la comunicación entre la aplicación y la base de datos.

Estado: Pendiente

---

## Fase 9: Pruebas y correcciones

En esta fase se verificará que el sistema funcione correctamente y que cumpla con el diseño definido.

Las pruebas deberán contemplar los dos canales de venta y los diferentes tipos de usuarios.

Actividades principales:

- [ ] Probar inicio de sesión de empleado.
- [ ] Probar inicio de sesión de cliente.
- [ ] Probar permisos según rol.
- [ ] Probar venta por taquilla.
- [ ] Probar venta en línea.
- [ ] Probar cliente registrado.
- [ ] Probar cliente consumidor final.
- [ ] Probar acumulación de puntos.
- [ ] Probar canje de puntos.
- [ ] Probar entrada gratuita.
- [ ] Probar selección de butaca disponible.
- [ ] Probar intento de vender una butaca ya ocupada.
- [ ] Probar escenarios normales.
- [ ] Probar escenarios con errores.
- [ ] Probar restricciones de la base de datos.
- [ ] Corregir errores encontrados.
- [ ] Revisar consistencia de datos.
- [ ] Confirmar que el sistema cumple con la propuesta base.

Estado: Pendiente

---

## Fase 10: Documentación final y entrega

En esta fase se preparará la documentación final del proyecto y se organizará la entrega.

Actividades principales:

- [ ] Revisar análisis inicial.
- [ ] Revisar requerimientos.
- [ ] Revisar reglas de negocio.
- [ ] Revisar modelo conceptual.
- [ ] Revisar modelo lógico.
- [ ] Revisar modelo físico.
- [ ] Revisar scripts SQL.
- [ ] Actualizar README.
- [ ] Documentar cómo ejecutar el proyecto.
- [ ] Documentar cómo crear la base de datos.
- [ ] Documentar usuarios de prueba.
- [ ] Documentar casos de prueba.
- [ ] Verificar que todo esté subido a GitHub.
- [ ] Preparar explicación del diseño.
- [ ] Preparar entrega final.

Estado: Pendiente

---

## Entidades principales definidas para iniciar el modelo conceptual

A partir del análisis inicial, los requerimientos, las reglas de negocio y la revisión del enfoque del sistema, el equipo trabajará con las siguientes entidades principales:

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

Estas entidades responden al enfoque actualizado del sistema, el cual permite ventas presenciales en taquilla y ventas en línea.

La entidad Usuario será utilizada como acceso general al sistema, tanto para empleados como para clientes, diferenciando los permisos mediante roles como ADMIN, CAJERO, SUPERVISOR y CLIENTE.

La entidad Cliente permitirá representar diferentes situaciones:

- Cliente registrado por taquilla.
- Cliente registrado en línea.
- Cliente con usuario para compra en línea.
- Cliente genérico o consumidor final para compras sin registro.

Estas entidades podrán ajustarse si durante el modelado conceptual se identifica alguna mejora necesaria.

---

## Relaciones principales consideradas para el modelo conceptual

Las relaciones principales que se tomarán como base para el modelo conceptual son:

- Un cliente puede realizar muchas ventas.
- Un cliente puede tener un usuario, pero no es obligatorio.
- Un empleado puede tener un usuario, pero no es obligatorio.
- Un empleado puede registrar muchas ventas por taquilla.
- Una venta puede ser realizada por taquilla o en línea.
- Una venta por taquilla debe estar asociada a un empleado.
- Una venta en línea puede no tener empleado asociado.
- Toda venta debe estar asociada a un cliente.
- Un género puede clasificar muchas películas.
- Una película puede tener muchas funciones.
- Una sala puede tener muchas funciones.
- Una sala puede tener muchas butacas.
- Una venta puede contener una o varias entradas.
- Una función puede tener muchas entradas.
- Una butaca puede aparecer en muchas entradas, siempre que sean de funciones diferentes.
- Un tipo de entrada puede aplicarse a muchas entradas.
- Un método de pago puede usarse en muchas ventas.
- Un cliente puede tener muchos movimientos en el historial de puntos.
- Una venta puede generar cero, uno o varios movimientos de puntos.

---

## Reglas clave a considerar en los modelos

Durante el diseño conceptual, lógico y físico se deberán mantener las siguientes reglas principales:

- Una misma butaca no puede estar asociada a más de una entrada activa dentro de la misma función.
- El sistema debe permitir ventas por TAQUILLA y ventas EN_LINEA.
- El canal de venta debe quedar registrado en cada venta.
- Si la venta es por TAQUILLA, debe estar asociada al empleado que la registró.
- Si la venta es EN_LINEA, puede no tener empleado asociado.
- Toda venta debe estar asociada a un cliente.
- Solo los clientes registrados podrán acumular y canjear puntos.
- El consumidor final no acumula puntos.
- Un cliente puede estar registrado sin tener usuario.
- El usuario solo será obligatorio para comprar en línea.
- Las entradas gratuitas por fidelidad no generan nuevos puntos.
- Las ventas y entradas anuladas o devueltas no deben eliminarse físicamente, sino cambiar de estado.

---

## Nota

Este plan de trabajo es una guía inicial actualizada. El proyecto ya cuenta con análisis inicial, requerimientos, reglas de negocio y alcance base.

Luego de la revisión del enfoque, el sistema fue ajustado para contemplar dos canales de venta: venta presencial en taquilla y venta en línea. Además, se definió que Usuario será una entidad general de acceso para clientes y empleados.

El siguiente paso es desarrollar el modelo conceptual del sistema, representando las entidades, relaciones y cardinalidades correspondientes.

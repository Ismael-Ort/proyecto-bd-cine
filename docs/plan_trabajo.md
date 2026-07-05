# Plan de Trabajo - Sistema de Gestión de Cine

## Objetivo del plan

Este documento organiza el proceso de trabajo del proyecto final. La finalidad es avanzar de forma ordenada, iniciando con el análisis de la propuesta base y la definición del alcance, antes de pasar al diseño de modelos, implementación de la base de datos y desarrollo del sistema.

Este plan podrá ajustarse cuando el profesor indique los criterios oficiales de evaluación.

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

Actividades principales:

- [x] Definir el alcance principal del proyecto.
- [x] Definir qué funcionalidades son necesarias para cumplir la propuesta base.
- [x] Definir qué mejoras propias del equipo serán incluidas.
- [x] Definir qué elementos quedarán como opcionales.
- [x] Revisar si el alcance es posible de completar en el tiempo disponible.
- [ ] Ajustar el alcance según los criterios oficiales del profesor.

Estado: En proceso

---

## Fase 3: Requerimientos y reglas de negocio

En esta fase se definieron las funciones que debe cumplir el sistema y las reglas que deben respetarse para que funcione correctamente.

Actividades principales:

- [x] Definir requerimientos funcionales.
- [x] Definir requerimientos no funcionales.
- [x] Definir reglas de negocio.
- [x] Revisar reglas relacionadas con disponibilidad.
- [x] Revisar reglas relacionadas con ventas.
- [x] Revisar reglas relacionadas con fidelidad.
- [x] Revisar reglas relacionadas con estados o cambios operativos.
- [x] Verificar que los requerimientos estén alineados con el alcance definido.
- [ ] Ajustar requerimientos y reglas según criterios oficiales del profesor.

Estado: En proceso

---

## Fase 4: Modelo conceptual

En esta fase se identificarán las entidades principales del sistema y las relaciones entre ellas, sin entrar todavía en detalles técnicos de tablas o tipos de datos.

Actividades principales:

- [x] Identificar entidades candidatas a partir del análisis.
- [x] Seleccionar las entidades principales iniciales.
- [ ] Definir relaciones entre entidades.
- [ ] Establecer cardinalidades.
- [ ] Revisar si el modelo representa correctamente la propuesta.
- [ ] Corregir entidades o relaciones innecesarias.
- [ ] Guardar el modelo conceptual en la carpeta correspondiente.

Estado: Pendiente / Próximo paso

---

## Fase 5: Modelo lógico

En esta fase se transformará el modelo conceptual en una estructura relacional más formal.

Actividades principales:

- [ ] Convertir entidades en relaciones.
- [ ] Definir atributos principales.
- [ ] Definir llaves primarias.
- [ ] Definir llaves foráneas.
- [ ] Resolver relaciones de uno a muchos.
- [ ] Resolver relaciones de muchos a muchos si existen.
- [ ] Revisar normalización básica.
- [ ] Validar que el modelo lógico responda a las reglas de negocio.

Estado: Pendiente

---

## Fase 6: Modelo físico

En esta fase se preparará el diseño final de la base de datos para su implementación en el sistema gestor seleccionado.

Actividades principales:

- [ ] Definir nombres finales de tablas y columnas.
- [ ] Definir tipos de datos.
- [ ] Definir restricciones.
- [ ] Definir índices si son necesarios.
- [ ] Definir reglas de integridad.
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
- [ ] Crear consultas de prueba.
- [ ] Validar restricciones principales.
- [ ] Probar los escenarios definidos en el análisis.
- [ ] Documentar resultados de pruebas.

Estado: Pendiente

---

## Fase 8: Desarrollo de la aplicación

En esta fase se desarrollará la aplicación conectada a la base de datos.

Actividades principales:

- [ ] Definir la estructura del proyecto de programación.
- [ ] Crear la conexión con la base de datos.
- [ ] Diseñar las pantallas principales según el alcance.
- [ ] Programar los módulos principales.
- [ ] Validar que la aplicación respete las reglas de negocio.
- [ ] Probar la comunicación entre la aplicación y la base de datos.

Estado: Pendiente

---

## Fase 9: Pruebas y correcciones

En esta fase se verificará que el sistema funcione correctamente y que cumpla con el diseño definido.

Actividades principales:

- [ ] Probar los casos principales del sistema.
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
- [ ] Revisar modelos.
- [ ] Revisar scripts SQL.
- [ ] Actualizar README.
- [ ] Documentar cómo ejecutar el proyecto.
- [ ] Verificar que todo esté subido a GitHub.
- [ ] Preparar explicación del diseño.
- [ ] Preparar entrega final.

Estado: Pendiente

---

## Entidades principales definidas para iniciar el modelo conceptual

A partir del análisis inicial, los requerimientos y las reglas de negocio, el equipo trabajará inicialmente con las siguientes entidades:

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

Estas entidades podrán ajustarse si durante el modelado conceptual se identifica alguna mejora necesaria.

---

## Nota

Este plan de trabajo es una guía inicial actualizada. El proyecto ya cuenta con análisis inicial, requerimientos, reglas de negocio y alcance base. El siguiente paso es desarrollar el modelo conceptual del sistema.

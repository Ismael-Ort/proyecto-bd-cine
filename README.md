# Sistema de Gestión de Cine - Proyecto Final de Base de Datos

Este repositorio corresponde al proyecto final de la asignatura de Base de Datos.

El proyecto consiste en el análisis, diseño e implementación de un sistema para la gestión de un cine, utilizando Java como lenguaje de programación y una base de datos relacional en MariaDB/MySQL.

---

# Estado del proyecto

El proyecto se encuentra actualmente en la fase de implementación de la base de datos.

Hasta el momento se han completado:

- Análisis inicial.
- Definición del alcance.
- Requerimientos funcionales y no funcionales.
- Reglas de negocio (incluye checklist de correspondencia con `database/schema.sql`).
- Plan de trabajo.
- Modelo conceptual.
- Modelo lógico.
- Modelo físico y script de creación (`database/schema.sql`), con la entidad `Persona` como base de `Cliente`, `Empleado` y `Usuario`.
- Procedimientos almacenados, funciones y triggers (`database/procedimientos_triggers.sql`), documentados en `docs/procedimientos_bd.md`.

El siguiente paso consiste en ejecutar y probar `database/schema.sql` y `database/procedimientos_triggers.sql` en el gestor de base de datos, cargar datos de prueba y comenzar el desarrollo de la aplicación en Java.

---

# Objetivo general

Diseñar e implementar un sistema que permita gestionar las operaciones principales de un cine, incluyendo la administración de películas, géneros, salas, funciones, clientes, empleados, usuarios, ventas de entradas y programa de fidelidad, utilizando una base de datos relacional que garantice integridad, consistencia y trazabilidad de la información.

---

# Alcance del sistema

El sistema estará orientado a la gestión interna del cine y permitirá realizar ventas tanto en taquilla como en línea.

Los empleados accederán mediante un usuario y contraseña. Los clientes podrán estar registrados en el sistema y, opcionalmente, disponer de un usuario para realizar compras en línea. Toda venta deberá estar asociada a un cliente registrado.

El alcance incluye:

- Gestión de clientes.
- Gestión de empleados.
- Gestión de usuarios.
- Gestión de películas.
- Gestión de géneros.
- Gestión de salas.
- Gestión de butacas.
- Programación de funciones.
- Venta de entradas por taquilla.
- Venta de entradas en línea.
- Consulta de disponibilidad de butacas.
- Gestión de métodos de pago.
- Gestión de tipos de entrada.
- Programa de fidelidad.
- Historial de puntos.

Como posibles extensiones futuras, el sistema podrá incorporar módulos como cafetería, inventario, productos, combos y reservas.

---

# Funcionalidades principales

- Administración de películas y géneros.
- Administración de salas y butacas.
- Programación de funciones.
- Registro de clientes.
- Administración de empleados y usuarios.
- Venta de entradas por taquilla.
- Venta de entradas en línea.
- Selección de butacas disponibles.
- Gestión de métodos de pago.
- Programa de fidelidad basado en acumulación y canje de puntos.
- Historial de movimientos de puntos.
- Reglas críticas resueltas con procedimientos almacenados, funciones y triggers (registro de venta, disponibilidad/reventa de butacas, acumulación automática de puntos, canje de puntos).

---

# Tecnologías utilizadas

- Java
- MariaDB / MySQL
- Git
- GitHub
- IntelliJ IDEA

---

# Estructura del repositorio

```
docs/
modelos/
database/
src/
lib/
```

Descripción:

- **docs/**: análisis inicial, requerimientos, reglas de negocio, plan de trabajo y guía de procedimientos/triggers.
- **modelos/**: modelos conceptual, lógico y físico.
- **database/**: scripts SQL (`schema.sql`, `procedimientos_triggers.sql`), datos de prueba y consultas.
- **src/**: código fuente de la aplicación.
- **lib/**: librerías externas del proyecto.

---

# Avance del proyecto

- [x] Creación del repositorio.
- [x] Estructura inicial de carpetas.
- [x] README.
- [x] Análisis inicial.
- [x] Definición del alcance.
- [x] Requerimientos funcionales y no funcionales.
- [x] Reglas de negocio.
- [x] Plan de trabajo.
- [x] Modelo conceptual.
- [x] Modelo lógico.
- [x] Modelo físico.
- [x] Script SQL de creación de la base de datos (`database/schema.sql`).
- [x] Procedimientos almacenados, funciones y triggers (`database/procedimientos_triggers.sql`).
- [ ] Datos de prueba.
- [ ] Consultas de prueba.
- [ ] Ejecutar y validar `database/schema.sql` y `database/procedimientos_triggers.sql` en el gestor de base de datos.
- [ ] Desarrollo de la aplicación en Java.
- [ ] Pruebas del sistema.
- [ ] Documentación final.

---

# Próximo paso

El siguiente paso del proyecto consiste en:

- Ejecutar `database/schema.sql` y luego `database/procedimientos_triggers.sql` en el gestor de base de datos y corregir errores si surgen.
- Cargar datos de prueba (personas, clientes, empleados, usuarios, salas, butacas, funciones).
- Verificar, uno a uno, el checklist de constraints por tabla definido en `docs/reglas_negocio.md`.
- Ejecutar el guion de pruebas de `docs/procedimientos_bd.md` (venta simple, venta con varias entradas, butaca duplicada, cancelación/reventa, canje de puntos, función cancelada).

Posteriormente se iniciará el desarrollo de la aplicación en Java, apoyándose en `docs/validaciones_en_java.md` para las reglas que no se expresan como restricciones de la base de datos y en `docs/procedimientos_bd.md` para invocar los procedimientos desde `CallableStatement`.

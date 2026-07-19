# Sistema de Gestión de Cine - Proyecto Final de Base de Datos

Este repositorio corresponde al proyecto final de la asignatura de Base de Datos.

El proyecto consiste en el análisis, diseño e implementación de un sistema para la gestión de un cine, utilizando Java como lenguaje de programación y una base de datos relacional en MariaDB/MySQL.

---

# Estado del proyecto

El proyecto se encuentra actualmente en la fase de diseño e implementación de la base de datos.

Hasta el momento se han completado:

- Análisis inicial.
- Definición del alcance.
- Requerimientos funcionales y no funcionales.
- Reglas de negocio.
- Plan de trabajo.
- Modelo conceptual.
- Modelo lógico.

El siguiente paso consiste en desarrollar el modelo físico, implementar la base de datos y comenzar el desarrollo de la aplicación.

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

- **docs/**: análisis inicial, requerimientos, reglas de negocio y plan de trabajo.
- **modelos/**: modelos conceptual, lógico y físico.
- **database/**: scripts SQL, datos de prueba y consultas.
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
- [ ] Modelo físico.
- [ ] Script SQL de creación de la base de datos.
- [ ] Datos de prueba.
- [ ] Consultas de prueba.
- [ ] Desarrollo de la aplicación en Java.
- [ ] Pruebas del sistema.
- [ ] Documentación final.

---

# Próximo paso

El siguiente paso del proyecto consiste en desarrollar el modelo físico de la base de datos, definiendo:

- Tipos de datos.
- Restricciones.
- Claves primarias y foráneas.
- Índices.
- Reglas de integridad.
- Script SQL de creación de la base de datos.

Posteriormente se implementará la base de datos y se iniciará el desarrollo de la aplicación en Java.

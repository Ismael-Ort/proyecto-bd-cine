# Modelo Físico - Sistema de Gestión de Cine

Este documento define el diseño final de la base de datos: nombres de tablas y columnas, tipos de datos, llaves primarias, llaves foráneas, restricciones `NOT NULL`, `UNIQUE`, `CHECK`, e índices. El motor de referencia es **PostgreSQL** (se usa por su soporte nativo de índices únicos parciales, necesarios para BR-17).

Cada restricción incluye entre paréntesis la regla de negocio (BR-XX) o requerimiento no funcional (RNF-XX) que justifica su existencia, para que quede trazable ante el profesor.

---

## 1. Decisiones tomadas para pasar de lógico a físico

Antes del script, se documentan las decisiones nuevas de esta fase (más allá de las ya resueltas en el modelo lógico):

1. **`empleado.nombres`**: se agrega este atributo (ausente en el conceptual/lógico) porque sin él no es posible mostrar el nombre completo de un empleado en pantallas o reportes. Se marca como **suposición del equipo** — confirmar con el profesor si prefiere mantenerlo fuera.
2. **`cliente.documento`**: se deja opcional (nullable) para permitir consumidor final, pero se agrega una restricción `CHECK` que obliga a que todo cliente `REGISTRADO` sí tenga documento (BR-10), y un índice único parcial que solo aplica cuando el documento no es nulo (para no bloquear múltiples consumidores finales sin documento).
3. **`precio_final` y `total_pagado`** se mantienen como columnas almacenadas (no calculadas) con un `CHECK` que fuerza la consistencia aritmética, en lugar de usar columnas generadas, para mantener compatibilidad con la aplicación que hará el cálculo antes de insertar.
4. **Exclusividad de butaca por función (BR-17)** se implementa con un **índice único parcial** sobre `(id_funcion, id_butaca)` filtrado por `estado = 'Activa'` en la tabla `entrada`, ya que una restricción `UNIQUE` normal impediría reutilizar la butaca en funciones distintas o en entradas anuladas.
5. **Contraseña**: se almacena como `VARCHAR(255)` para admitir un hash (bcrypt/argon2), nunca texto plano — la aplicación es responsable de hashear antes de insertar.

---

## 2. Script SQL de creación

```sql
-- =========================================================
-- MODELO FÍSICO - SISTEMA DE GESTIÓN DE CINE
-- Motor: PostgreSQL
-- =========================================================

-- ---------------------------------------------------------
-- TABLA: genero
-- ---------------------------------------------------------
CREATE TABLE genero (
    id_genero       SERIAL PRIMARY KEY,
    nombre_genero   VARCHAR(50)  NOT NULL UNIQUE,
    descripcion     VARCHAR(255),
    estado          VARCHAR(20)  NOT NULL DEFAULT 'Activo'
                     CHECK (estado IN ('Activo', 'Inactivo'))
);

-- ---------------------------------------------------------
-- TABLA: sala
-- ---------------------------------------------------------
CREATE TABLE sala (
    id_sala         SERIAL PRIMARY KEY,
    nombre_sala     VARCHAR(50)  NOT NULL UNIQUE,
    tipo_sala       VARCHAR(30),
    capacidad       INT          NOT NULL CHECK (capacidad > 0),
    estado          VARCHAR(20)  NOT NULL DEFAULT 'Activa'
                     CHECK (estado IN ('Activa', 'Inactiva', 'Mantenimiento'))
);

-- ---------------------------------------------------------
-- TABLA: empleado
-- ---------------------------------------------------------
CREATE TABLE empleado (
    id_empleado         SERIAL PRIMARY KEY,
    documento           VARCHAR(20)  NOT NULL UNIQUE,
    nombres             VARCHAR(80)  NOT NULL,
    apellidos           VARCHAR(80)  NOT NULL,
    correo              VARCHAR(100) UNIQUE,
    telefono            VARCHAR(20),
    cargo               VARCHAR(50),
    fecha_contratacion  DATE         NOT NULL DEFAULT CURRENT_DATE,
    estado              VARCHAR(20)  NOT NULL DEFAULT 'Activo'
                         CHECK (estado IN ('Activo', 'Inactivo'))
);

-- ---------------------------------------------------------
-- TABLA: cliente
-- ---------------------------------------------------------
CREATE TABLE cliente (
    id_cliente      SERIAL PRIMARY KEY,
    documento       VARCHAR(20),
    nombres         VARCHAR(80)  NOT NULL,
    apellidos       VARCHAR(80)  NOT NULL,
    correo          VARCHAR(100),
    telefono        VARCHAR(20),
    fecha_registro  DATE         NOT NULL DEFAULT CURRENT_DATE,
    tipo_cliente    VARCHAR(20)  NOT NULL DEFAULT 'REGISTRADO'
                     CHECK (tipo_cliente IN ('REGISTRADO', 'CONSUMIDOR_FINAL')),
    estado          VARCHAR(20)  NOT NULL DEFAULT 'Activo'
                     CHECK (estado IN ('Activo', 'Inactivo')),
    -- BR-10: todo cliente REGISTRADO debe tener documento
    CONSTRAINT chk_cliente_documento_registrado
        CHECK (tipo_cliente <> 'REGISTRADO' OR documento IS NOT NULL)
);

-- Restricción de integridad (no de rendimiento): el documento solo debe
-- ser único cuando existe, para permitir múltiples consumidores finales
-- sin documento. En PostgreSQL esto solo puede implementarse como
-- índice único parcial, no como CONSTRAINT UNIQUE normal.
CREATE UNIQUE INDEX ux_cliente_documento
    ON cliente (documento)
    WHERE documento IS NOT NULL;

-- ---------------------------------------------------------
-- TABLA: metodopago
-- ---------------------------------------------------------
CREATE TABLE metodopago (
    id_metodopago   SERIAL PRIMARY KEY,
    nombre_metodo   VARCHAR(50)  NOT NULL UNIQUE,
    descripcion     VARCHAR(255),
    estado          VARCHAR(20)  NOT NULL DEFAULT 'Activo'
                     CHECK (estado IN ('Activo', 'Inactivo'))
);

-- ---------------------------------------------------------
-- TABLA: tipoentrada
-- ---------------------------------------------------------
CREATE TABLE tipoentrada (
    id_tipoentrada       SERIAL PRIMARY KEY,
    nombre_tipo          VARCHAR(50)  NOT NULL UNIQUE,
    descuento_porcentaje NUMERIC(5,2) NOT NULL DEFAULT 0
                          CHECK (descuento_porcentaje BETWEEN 0 AND 100),
    descripcion          VARCHAR(255),
    estado               VARCHAR(20)  NOT NULL DEFAULT 'Activo'
                          CHECK (estado IN ('Activo', 'Inactivo'))
);

-- ---------------------------------------------------------
-- TABLA: pelicula  (depende de genero)
-- ---------------------------------------------------------
CREATE TABLE pelicula (
    id_pelicula       SERIAL PRIMARY KEY,
    titulo            VARCHAR(150) NOT NULL,
    sinopsis          TEXT,
    clasificacion     VARCHAR(10),
    duracion_minutos  INT          NOT NULL CHECK (duracion_minutos > 0),
    estado            VARCHAR(20)  NOT NULL DEFAULT 'Activa'
                       CHECK (estado IN ('Activa', 'Inactiva')),
    id_genero         INT          NOT NULL,
    CONSTRAINT fk_pelicula_genero
        FOREIGN KEY (id_genero) REFERENCES genero (id_genero)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------
-- TABLA: butaca  (depende de sala)
-- ---------------------------------------------------------
CREATE TABLE butaca (
    id_butaca     SERIAL PRIMARY KEY,
    fila          VARCHAR(5)   NOT NULL,
    numero        INT          NOT NULL CHECK (numero > 0),
    tipo_butaca   VARCHAR(20)  NOT NULL DEFAULT 'Regular'
                   CHECK (tipo_butaca IN ('Regular', 'VIP')),
    estado        VARCHAR(20)  NOT NULL DEFAULT 'Activa'
                   CHECK (estado IN ('Activa', 'Inactiva', 'Mantenimiento')),
    id_sala       INT          NOT NULL,
    CONSTRAINT fk_butaca_sala
        FOREIGN KEY (id_sala) REFERENCES sala (id_sala)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    -- No pueden existir dos butacas con la misma fila y número en la misma sala
    CONSTRAINT ux_butaca_sala_fila_numero UNIQUE (id_sala, fila, numero)
);

-- ---------------------------------------------------------
-- TABLA: funcion  (depende de pelicula, sala)
-- ---------------------------------------------------------
CREATE TABLE funcion (
    id_funcion      SERIAL PRIMARY KEY,
    fecha_funcion   DATE          NOT NULL,
    hora_inicio     TIME          NOT NULL,
    hora_fin        TIME          NOT NULL,
    tarifa_base     NUMERIC(10,2) NOT NULL CHECK (tarifa_base >= 0),
    estado          VARCHAR(20)   NOT NULL DEFAULT 'Programada'
                     CHECK (estado IN ('Programada', 'En_Curso', 'Finalizada', 'Cancelada')),
    id_pelicula     INT           NOT NULL,
    id_sala         INT           NOT NULL,
    CONSTRAINT fk_funcion_pelicula
        FOREIGN KEY (id_pelicula) REFERENCES pelicula (id_pelicula)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_funcion_sala
        FOREIGN KEY (id_sala) REFERENCES sala (id_sala)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    -- BR-02: hora_fin debe ser posterior a hora_inicio
    CONSTRAINT chk_funcion_horario CHECK (hora_fin > hora_inicio)
);

-- BR-03: no permitir dos funciones en la misma sala con horarios solapados
-- (requiere la extensión btree_gist; alternativa a validar en Fase 7/8 vía aplicación
-- si el motor destino no soporta EXCLUDE)
-- CREATE EXTENSION IF NOT EXISTS btree_gist;
-- ALTER TABLE funcion ADD CONSTRAINT no_solape_horario
--     EXCLUDE USING gist (
--         id_sala WITH =,
--         daterange(fecha_funcion, fecha_funcion, '[]') WITH &&,
--         tsrange(fecha_funcion + hora_inicio, fecha_funcion + hora_fin) WITH &&
--     );

-- ---------------------------------------------------------
-- TABLA: usuario  (depende de empleado, cliente)
-- ---------------------------------------------------------
CREATE TABLE usuario (
    id_usuario      SERIAL PRIMARY KEY,
    nombre_usuario  VARCHAR(50)  NOT NULL UNIQUE,
    contrasena      VARCHAR(255) NOT NULL,  -- almacena el HASH, nunca texto plano
    rol             VARCHAR(20)  NOT NULL
                     CHECK (rol IN ('ADMIN', 'CAJERO', 'SUPERVISOR', 'CLIENTE')),
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado          VARCHAR(20)  NOT NULL DEFAULT 'Activo'
                     CHECK (estado IN ('Activo', 'Inactivo', 'Bloqueado')),
    id_empleado     INT UNIQUE,  -- BR-08: un empleado tiene como máximo un usuario
    id_cliente      INT UNIQUE,  -- BR-09: un cliente tiene como máximo un usuario
    CONSTRAINT fk_usuario_empleado
        FOREIGN KEY (id_empleado) REFERENCES empleado (id_empleado)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_usuario_cliente
        FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    -- BR-08, BR-09: exclusividad según el rol
    CONSTRAINT chk_usuario_rol_referencia CHECK (
        (rol IN ('ADMIN', 'CAJERO', 'SUPERVISOR') AND id_empleado IS NOT NULL AND id_cliente IS NULL)
        OR
        (rol = 'CLIENTE' AND id_cliente IS NOT NULL AND id_empleado IS NULL)
    )
);

-- ---------------------------------------------------------
-- TABLA: venta  (depende de cliente, empleado, metodopago)
-- ---------------------------------------------------------
CREATE TABLE venta (
    id_venta         SERIAL PRIMARY KEY,
    fecha_venta      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal         NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0),
    descuento_total  NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (descuento_total >= 0),
    total_pagado     NUMERIC(10,2) NOT NULL CHECK (total_pagado >= 0),
    canal_venta      VARCHAR(20)   NOT NULL
                      CHECK (canal_venta IN ('TAQUILLA', 'EN_LINEA')),
    observacion      VARCHAR(255),
    estado           VARCHAR(20)   NOT NULL DEFAULT 'Activa'
                      CHECK (estado IN ('Activa', 'Anulada', 'Devuelta')),
    id_cliente       INT           NOT NULL,
    id_empleado      INT,
    id_metodopago    INT           NOT NULL,
    CONSTRAINT fk_venta_cliente
        FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_venta_empleado
        FOREIGN KEY (id_empleado) REFERENCES empleado (id_empleado)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_venta_metodopago
        FOREIGN KEY (id_metodopago) REFERENCES metodopago (id_metodopago)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    -- BR-15, BR-36: venta TAQUILLA exige empleado
    CONSTRAINT chk_venta_canal_empleado CHECK (
        (canal_venta = 'TAQUILLA' AND id_empleado IS NOT NULL)
        OR
        (canal_venta = 'EN_LINEA')
    ),
    -- Consistencia aritmética de montos
    CONSTRAINT chk_venta_totales CHECK (total_pagado = subtotal - descuento_total)
);

-- ---------------------------------------------------------
-- TABLA: entrada  (depende de venta, funcion, butaca, tipoentrada)
-- ---------------------------------------------------------
CREATE TABLE entrada (
    id_entrada      SERIAL PRIMARY KEY,
    precio_base     NUMERIC(10,2) NOT NULL CHECK (precio_base >= 0),
    cargo_butaca    NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (cargo_butaca >= 0),
    descuento       NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (descuento >= 0),
    precio_final    NUMERIC(10,2) NOT NULL CHECK (precio_final >= 0),
    es_gratis       BOOLEAN       NOT NULL DEFAULT FALSE,
    estado          VARCHAR(20)   NOT NULL DEFAULT 'Activa'
                     CHECK (estado IN ('Activa', 'Anulada', 'Devuelta')),
    id_venta        INT           NOT NULL,
    id_funcion      INT           NOT NULL,
    id_butaca       INT           NOT NULL,
    id_tipoentrada  INT           NOT NULL,
    CONSTRAINT fk_entrada_venta
        FOREIGN KEY (id_venta) REFERENCES venta (id_venta)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_entrada_funcion
        FOREIGN KEY (id_funcion) REFERENCES funcion (id_funcion)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_entrada_butaca
        FOREIGN KEY (id_butaca) REFERENCES butaca (id_butaca)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_entrada_tipoentrada
        FOREIGN KEY (id_tipoentrada) REFERENCES tipoentrada (id_tipoentrada)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    -- BR-25: consistencia del cálculo de precio
    CONSTRAINT chk_entrada_precio_final
        CHECK (precio_final = precio_base + cargo_butaca - descuento),
    -- BR-27: si es gratis, el precio final debe ser 0
    CONSTRAINT chk_entrada_gratis
        CHECK ((es_gratis = TRUE AND precio_final = 0) OR (es_gratis = FALSE))
);

-- BR-17, BR-18, RNF-02: una butaca no puede estar en más de una entrada
-- ACTIVA para la misma función. Este índice único PARCIAL no es de
-- rendimiento: es la única forma de implementar esta restricción de
-- integridad en PostgreSQL, por eso se conserva.
CREATE UNIQUE INDEX ux_entrada_funcion_butaca_activa
    ON entrada (id_funcion, id_butaca)
    WHERE estado = 'Activa';

-- ---------------------------------------------------------
-- TABLA: historial_puntos  (depende de cliente, venta, entrada)
-- ---------------------------------------------------------
CREATE TABLE historial_puntos (
    id_historial       SERIAL PRIMARY KEY,
    tipo_movimiento    VARCHAR(20) NOT NULL
                        CHECK (tipo_movimiento IN ('GANADO', 'CANJEADO')),
    puntos_ganados      INT NOT NULL DEFAULT 0 CHECK (puntos_ganados >= 0),
    puntos_canjeados    INT NOT NULL DEFAULT 0 CHECK (puntos_canjeados >= 0),
    fecha_movimiento    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion         VARCHAR(255),
    id_cliente          INT         NOT NULL,
    id_venta            INT,
    id_entrada          INT,
    CONSTRAINT fk_historial_cliente
        FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_historial_venta
        FOREIGN KEY (id_venta) REFERENCES venta (id_venta)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_historial_entrada
        FOREIGN KEY (id_entrada) REFERENCES entrada (id_entrada)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    -- BR-26, BR-29, BR-30: un movimiento GANADO viene de una entrada específica;
    -- un movimiento CANJEADO viene de una venta específica
    CONSTRAINT chk_historial_tipo_referencia CHECK (
        (tipo_movimiento = 'GANADO'   AND puntos_ganados > 0  AND puntos_canjeados = 0 AND id_entrada IS NOT NULL)
        OR
        (tipo_movimiento = 'CANJEADO' AND puntos_canjeados > 0 AND puntos_ganados = 0  AND id_venta IS NOT NULL)
    ),
    -- Una misma entrada no puede generar más de un movimiento de puntos
    CONSTRAINT ux_historial_entrada UNIQUE (id_entrada)
);

```

---

## 3. Resumen de restricciones por tipo

| Tipo de restricción | Dónde se aplica | Regla que cubre |
|---|---|---|
| `PRIMARY KEY` | Todas las tablas | Integridad de entidad (RNF-01) |
| `FOREIGN KEY` (todas con `ON DELETE RESTRICT`) | Todas las relaciones | Integridad referencial (RNF-01). Se usa `RESTRICT` en vez de `CASCADE` para no borrar historial financiero por accidente (RNF-04) |
| `UNIQUE` simple | `genero.nombre_genero`, `sala.nombre_sala`, `empleado.documento`, `empleado.correo`, `usuario.nombre_usuario`, `metodopago.nombre_metodo`, `tipoentrada.nombre_tipo` | Evitar duplicados de catálogo |
| `UNIQUE` compuesta | `butaca (id_sala, fila, numero)` | Evitar butacas duplicadas dentro de una sala |
| `UNIQUE` sobre FK | `usuario.id_empleado`, `usuario.id_cliente`, `historial_puntos.id_entrada` | Cardinalidad 1 a 0..1 del modelo conceptual |
| Índice único **parcial** (integridad, no rendimiento) | `entrada (id_funcion, id_butaca) WHERE estado='Activa'` | BR-17, BR-18, RNF-02 — el corazón de la exclusividad de butacas |
| Índice único **parcial** (integridad, no rendimiento) | `cliente (documento) WHERE documento IS NOT NULL` | Permite múltiples consumidores finales sin documento |
| `CHECK` de dominio (listas de valores) | `estado`, `rol`, `canal_venta`, `tipo_cliente`, `tipo_movimiento`, `tipo_butaca` en varias tablas | Controlar valores válidos sin crear tablas de catálogo adicionales |
| `CHECK` numérico (`>= 0`, `> 0`) | Precios, capacidad, duración, puntos | Evitar valores negativos sin sentido de negocio |
| `CHECK` de consistencia entre columnas | `funcion` (horario), `venta` (canal-empleado, totales), `entrada` (precio final, gratuidad), `usuario` (rol-referencia), `historial_puntos` (tipo-referencia), `cliente` (documento-tipo) | Reglas de negocio compuestas que involucran más de una columna |

---

## 4. Notas para la Fase 7 (implementación)

- La restricción de **no solapamiento de horarios** (BR-03) quedó comentada en el script porque requiere la extensión `btree_gist` de PostgreSQL. Si el motor final no la soporta, esta regla deberá validarse en la capa de aplicación antes del `INSERT` en `funcion`.
- La **atomicidad transaccional** (BR-35, RNF-05) no se resuelve con constraints de tabla — se implementará envolviendo el flujo completo de venta (validar butaca → insertar venta → insertar entradas → insertar historial de puntos) en una única transacción (`BEGIN` / `COMMIT` / `ROLLBACK`) en la Fase 7-8.
- La regla de **9 puntos → descuento 100%** (BR-29) es lógica de aplicación, no de constraint — se validará antes de insertar la entrada gratuita, y luego el `CHECK chk_entrada_gratis` garantiza que, una vez decidido `es_gratis = TRUE`, el precio quede correctamente en 0.

---

## Estado del documento

- [x] Nombres finales de tablas y columnas definidos.
- [x] Tipos de datos definidos.
- [x] Restricciones `NOT NULL`, `UNIQUE` y `CHECK` definidas.
- [x] Índices definidos (incluyendo únicos parciales).
- [x] Reglas de integridad referencial definidas (`ON DELETE RESTRICT`).
- [x] Restricción de exclusividad de butacas por función resuelta.
- [x] Restricciones de usuarios, roles y canales de venta resueltas.
- [ ] Script probado en el gestor de base de datos (Fase 7).
- [ ] Ajustes finales según criterios oficiales del profesor.

-- =========================================================
-- SISTEMA DE GESTIÓN DE CINE
-- CREACIÓN COMPLETA DE LA BASE DE DATOS
-- MySQL / MariaDB
-- =========================================================


-- =========================================================
-- CREACIÓN DE LA BASE DE DATOS
-- =========================================================

DROP DATABASE IF EXISTS cine;

CREATE DATABASE cine
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE cine;


-- =========================================================
-- TABLA: PERSONA
-- Centraliza los datos personales de clientes, empleados
-- y usuarios.
-- =========================================================

CREATE TABLE persona (
                         id_persona INT AUTO_INCREMENT,
                         nombres VARCHAR(80) NOT NULL,
                         apellidos VARCHAR(80) NOT NULL,
                         fecha_nacimiento DATE NOT NULL,
                         sexo VARCHAR(1) NOT NULL,
                         documento VARCHAR(40) NOT NULL,
                         telefono VARCHAR(20) NOT NULL,
                         correo VARCHAR(100) NOT NULL,

                         CONSTRAINT pk_persona
                             PRIMARY KEY (id_persona),

                         CONSTRAINT uq_persona_documento
                             UNIQUE (documento),

                         CONSTRAINT uq_persona_correo
                             UNIQUE (correo),

                         CONSTRAINT chk_persona_sexo
                             CHECK (sexo IN ('M', 'F'))
);


-- =========================================================
-- TABLA: GENERO
-- =========================================================

CREATE TABLE genero (
                        id_genero INT AUTO_INCREMENT,
                        nombre_genero VARCHAR(50) NOT NULL,
                        descripcion VARCHAR(255) NULL,
                        estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',

                        CONSTRAINT pk_genero
                            PRIMARY KEY (id_genero),

                        CONSTRAINT uq_genero_nombre
                            UNIQUE (nombre_genero),

                        CONSTRAINT chk_genero_estado
                            CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);


-- =========================================================
-- TABLA: SALA
-- =========================================================

CREATE TABLE sala (
                      id_sala INT AUTO_INCREMENT,
                      nombre_sala VARCHAR(50) NOT NULL,
                      capacidad INT NOT NULL,
                      estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',

                      CONSTRAINT pk_sala
                          PRIMARY KEY (id_sala),

                      CONSTRAINT uq_sala_nombre
                          UNIQUE (nombre_sala),

                      CONSTRAINT chk_sala_capacidad
                          CHECK (capacidad > 0),

                      CONSTRAINT chk_sala_estado
                          CHECK (estado IN ('ACTIVA', 'INACTIVA'))
);


-- =========================================================
-- TABLA: METODO DE PAGO
-- =========================================================

CREATE TABLE metodopago (
                            id_metodopago INT AUTO_INCREMENT,
                            nombre_metodo VARCHAR(50) NOT NULL,
                            descripcion VARCHAR(255) NULL,
                            estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',

                            CONSTRAINT pk_metodopago
                                PRIMARY KEY (id_metodopago),

                            CONSTRAINT uq_metodopago_nombre
                                UNIQUE (nombre_metodo),

                            CONSTRAINT chk_metodopago_estado
                                CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);


-- =========================================================
-- TABLA: TIPO DE ENTRADA
-- =========================================================

CREATE TABLE tipoentrada (
                             id_tipoentrada INT AUTO_INCREMENT,
                             nombre_tipo VARCHAR(50) NOT NULL,
                             descuento_porcentaje DECIMAL(5,2) NOT NULL DEFAULT 0,
                             descripcion VARCHAR(255) NULL,
                             estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',

                             CONSTRAINT pk_tipoentrada
                                 PRIMARY KEY (id_tipoentrada),

                             CONSTRAINT uq_tipoentrada_nombre
                                 UNIQUE (nombre_tipo),

                             CONSTRAINT chk_tipoentrada_descuento
                                 CHECK (descuento_porcentaje BETWEEN 0 AND 100),

                             CONSTRAINT chk_tipoentrada_estado
                                 CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);


-- =========================================================
-- TABLA: PELICULA
-- =========================================================

CREATE TABLE pelicula (
                          id_pelicula INT AUTO_INCREMENT,
                          titulo VARCHAR(150) NOT NULL,
                          duracion_minutos INT NOT NULL,
                          clasificacion VARCHAR(10) NOT NULL,
                          sinopsis TEXT NULL,
                          estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
                          imagen_portada LONGBLOB NOT NULL,

                          CONSTRAINT pk_pelicula
                              PRIMARY KEY (id_pelicula),

                          CONSTRAINT uq_pelicula_titulo
                              UNIQUE (titulo),

                          CONSTRAINT chk_pelicula_duracion
                              CHECK (duracion_minutos > 0),

                          CONSTRAINT chk_pelicula_estado
                              CHECK (estado IN ('ACTIVA', 'INACTIVA'))
);


-- =========================================================
-- TABLA ASOCIATIVA: PELICULA_GENERO
-- Resuelve la relación muchos a muchos.
-- =========================================================

CREATE TABLE pelicula_genero (
                                 id_pelicula INT NOT NULL,
                                 id_genero INT NOT NULL,

                                 CONSTRAINT pk_pelicula_genero
                                     PRIMARY KEY (id_pelicula, id_genero),

                                 CONSTRAINT fk_peligenero_pelicula
                                     FOREIGN KEY (id_pelicula)
                                         REFERENCES pelicula(id_pelicula),

                                 CONSTRAINT fk_peligenero_genero
                                     FOREIGN KEY (id_genero)
                                         REFERENCES genero(id_genero)
);


-- =========================================================
-- TABLA: BUTACA
-- =========================================================

CREATE TABLE butaca (
                        id_butaca INT AUTO_INCREMENT,
                        fila VARCHAR(5) NOT NULL,
                        numero INT NOT NULL,
                        estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
                        id_sala INT NOT NULL,

                        CONSTRAINT pk_butaca
                            PRIMARY KEY (id_butaca),

                        CONSTRAINT fk_butaca_sala
                            FOREIGN KEY (id_sala)
                                REFERENCES sala(id_sala),

                        CONSTRAINT uq_butaca_sala_fila_numero
                            UNIQUE (id_sala, fila, numero),

                        CONSTRAINT chk_butaca_numero
                            CHECK (numero > 0),

                        CONSTRAINT chk_butaca_estado
                            CHECK (estado IN ('ACTIVA', 'INACTIVA'))
);


-- =========================================================
-- TABLA: FUNCION
-- hora_fin permite incluir el tiempo de película, anuncios,
-- limpieza y preparación de la sala.
-- =========================================================

CREATE TABLE funcion (
                         id_funcion INT AUTO_INCREMENT,
                         fecha_funcion DATE NOT NULL,
                         hora_inicio TIME NOT NULL,
                         hora_fin TIME NOT NULL,
                         tarifa_base DECIMAL(10,2) NOT NULL,
                         estado VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
                         id_pelicula INT NOT NULL,
                         id_sala INT NOT NULL,
                         idioma_audio VARCHAR(30) NULL,
                         idioma_subtitulos VARCHAR(30) NULL,

                         CONSTRAINT pk_funcion
                             PRIMARY KEY (id_funcion),

                         CONSTRAINT fk_funcion_pelicula
                             FOREIGN KEY (id_pelicula)
                                 REFERENCES pelicula(id_pelicula),

                         CONSTRAINT fk_funcion_sala
                             FOREIGN KEY (id_sala)
                                 REFERENCES sala(id_sala),

                         CONSTRAINT chk_funcion_horario
                             CHECK (hora_fin > hora_inicio),

                         CONSTRAINT chk_funcion_tarifa
                             CHECK (tarifa_base > 0),

                         CONSTRAINT chk_funcion_estado
                             CHECK (
                                 estado IN (
                                            'PROGRAMADA',
                                            'EN_CURSO',
                                            'FINALIZADA',
                                            'CANCELADA'
                                     )
                                 )
);


-- =========================================================
-- TABLA: EMPLEADO
-- Cada empleado se registra a partir de una Persona.
-- =========================================================

CREATE TABLE empleado (
                          id_empleado INT AUTO_INCREMENT,
                          cargo VARCHAR(50) NOT NULL,
                          fecha_contratacion DATE NOT NULL DEFAULT (CURRENT_DATE),
                          estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
                          id_persona INT NOT NULL,

                          CONSTRAINT pk_empleado
                              PRIMARY KEY (id_empleado),

                          CONSTRAINT uq_empleado_persona
                              UNIQUE (id_persona),

                          CONSTRAINT fk_empleado_persona
                              FOREIGN KEY (id_persona)
                                  REFERENCES persona(id_persona),

                          CONSTRAINT chk_empleado_estado
                              CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);


-- =========================================================
-- TABLA: CLIENTE
-- Cada cliente se registra a partir de una Persona.
-- =========================================================

CREATE TABLE cliente (
                         id_cliente INT AUTO_INCREMENT,
                         fecha_registro DATE NOT NULL DEFAULT (CURRENT_DATE),
                         estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
                         id_persona INT NOT NULL,

                         CONSTRAINT pk_cliente
                             PRIMARY KEY (id_cliente),

                         CONSTRAINT uq_cliente_persona
                             UNIQUE (id_persona),

                         CONSTRAINT fk_cliente_persona
                             FOREIGN KEY (id_persona)
                                 REFERENCES persona(id_persona),

                         CONSTRAINT chk_cliente_estado
                             CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);


-- =========================================================
-- TABLA: USUARIO
-- Una persona puede tener una o varias cuentas de usuario.
-- =========================================================

CREATE TABLE usuario (
                         id_usuario INT AUTO_INCREMENT,
                         nombre_usuario VARCHAR(50) NOT NULL,
                         hash_contrasena VARCHAR(255) NOT NULL,
                         rol VARCHAR(20) NOT NULL,
                         estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
                         id_persona INT NOT NULL,

                         CONSTRAINT pk_usuario
                             PRIMARY KEY (id_usuario),

                         CONSTRAINT uq_usuario_nombre
                             UNIQUE (nombre_usuario),

                         CONSTRAINT fk_usuario_persona
                             FOREIGN KEY (id_persona)
                                 REFERENCES persona(id_persona),

                         CONSTRAINT chk_usuario_rol
                             CHECK (
                                 rol IN (
                                         'ADMINISTRADOR',
                                         'CAJERO',
                                         'CLIENTE'
                                     )
                                 ),

                         CONSTRAINT chk_usuario_estado
                             CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);


-- =========================================================
-- TABLA: VENTA
-- Toda venta pertenece a un cliente.
-- Las ventas de TAQUILLA requieren empleado.
-- Las ventas EN_LINEA pueden no tener empleado.
-- =========================================================

CREATE TABLE venta (
                       id_venta INT AUTO_INCREMENT,
                       fecha_venta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       total_pagado DECIMAL(10,2) NOT NULL,
                       canal_venta VARCHAR(20) NOT NULL,
                       observacion VARCHAR(255) NULL,
                       estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
                       id_cliente INT NOT NULL,
                       id_empleado INT NULL,
                       id_metodopago INT NOT NULL,

                       CONSTRAINT pk_venta
                           PRIMARY KEY (id_venta),

                       CONSTRAINT fk_venta_cliente
                           FOREIGN KEY (id_cliente)
                               REFERENCES cliente(id_cliente),

                       CONSTRAINT fk_venta_empleado
                           FOREIGN KEY (id_empleado)
                               REFERENCES empleado(id_empleado),

                       CONSTRAINT fk_venta_metodopago
                           FOREIGN KEY (id_metodopago)
                               REFERENCES metodopago(id_metodopago),

                       CONSTRAINT chk_venta_total
                           CHECK (total_pagado >= 0),

                       CONSTRAINT chk_venta_canal
                           CHECK (canal_venta IN ('TAQUILLA', 'EN_LINEA')),

                       CONSTRAINT chk_venta_estado
                           CHECK (
                               estado IN (
                                          'PENDIENTE',
                                          'COMPLETADA',
                                          'CANCELADA'
                                   )
                               ),

                       CONSTRAINT chk_venta_canal_empleado
                           CHECK (
                               (
                                   canal_venta = 'TAQUILLA'
                                       AND id_empleado IS NOT NULL
                                   )
                                   OR
                               (
                                   canal_venta = 'EN_LINEA'
                                   )
                               )
);


-- =========================================================
-- TABLA: ENTRADA
-- Cada entrada pertenece a una venta, función, butaca
-- y tipo de entrada.
-- =========================================================

CREATE TABLE entrada (
                         id_entrada INT AUTO_INCREMENT,
                         precio_base DECIMAL(10,2) NOT NULL,
                         descuento DECIMAL(10,2) NOT NULL DEFAULT 0,
                         precio_final DECIMAL(10,2) NOT NULL,
                         estado VARCHAR(20) NOT NULL DEFAULT 'RESERVADA',
                         id_venta INT NOT NULL,
                         id_funcion INT NOT NULL,
                         id_butaca INT NOT NULL,
                         id_tipoentrada INT NOT NULL,

                         CONSTRAINT pk_entrada
                             PRIMARY KEY (id_entrada),

                         CONSTRAINT fk_entrada_venta
                             FOREIGN KEY (id_venta)
                                 REFERENCES venta(id_venta),

                         CONSTRAINT fk_entrada_funcion
                             FOREIGN KEY (id_funcion)
                                 REFERENCES funcion(id_funcion),

                         CONSTRAINT fk_entrada_butaca
                             FOREIGN KEY (id_butaca)
                                 REFERENCES butaca(id_butaca),

                         CONSTRAINT fk_entrada_tipoentrada
                             FOREIGN KEY (id_tipoentrada)
                                 REFERENCES tipoentrada(id_tipoentrada),

                         CONSTRAINT uq_entrada_funcion_butaca
                             UNIQUE (id_funcion, id_butaca),

                         CONSTRAINT chk_entrada_precio_base
                             CHECK (precio_base >= 0),

                         CONSTRAINT chk_entrada_descuento
                             CHECK (descuento >= 0),

                         CONSTRAINT chk_entrada_precio_final_valor
                             CHECK (precio_final >= 0),

                         CONSTRAINT chk_entrada_estado
                             CHECK (
                                 estado IN (
                                            'RESERVADA',
                                            'PAGADA',
                                            'CANCELADA',
                                            'UTILIZADA'
                                     )
                                 ),

                         CONSTRAINT chk_entrada_precio_final
                             CHECK (precio_final = precio_base - descuento)
);


-- =========================================================
-- TABLA: HISTORIAL_PUNTOS
-- Registra acumulaciones y canjes del programa de fidelidad.
-- =========================================================

CREATE TABLE historial_puntos (
                                  id_historial INT AUTO_INCREMENT,
                                  tipo_movimiento VARCHAR(20) NOT NULL,
                                  cantidad_puntos INT NOT NULL,
                                  fecha_movimiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  descripcion VARCHAR(255) NULL,
                                  id_cliente INT NOT NULL,
                                  id_venta INT NOT NULL,

                                  CONSTRAINT pk_historial_puntos
                                      PRIMARY KEY (id_historial),

                                  CONSTRAINT fk_historial_cliente
                                      FOREIGN KEY (id_cliente)
                                          REFERENCES cliente(id_cliente),

                                  CONSTRAINT fk_historial_venta
                                      FOREIGN KEY (id_venta)
                                          REFERENCES venta(id_venta),

                                  CONSTRAINT chk_historial_tipo
                                      CHECK (
                                          tipo_movimiento IN (
                                                              'ACUMULACIÓN',
                                                              'CANJE'
                                              )
                                          ),

                                  CONSTRAINT chk_historial_cantidad_puntos
                                      CHECK (cantidad_puntos > 0)
);


-- =========================================================
-- COMPROBACIÓN DE LAS TABLAS CREADAS
-- =========================================================

SHOW TABLES;
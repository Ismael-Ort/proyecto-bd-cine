CREATE TABLE genero (
                        id_genero INT AUTO_INCREMENT,
                        nombre_genero VARCHAR(50) NOT NULL,
                        descripcion VARCHAR(255) NULL,
                        estado VARCHAR(20) NOT NULL DEFAULT 'Activo',

                        CONSTRAINT pk_genero PRIMARY KEY (id_genero),
                        CONSTRAINT uq_genero_nombre UNIQUE (nombre_genero),
                        CONSTRAINT chk_genero_estado CHECK (estado IN ('Activo', 'Inactivo'))
);


CREATE TABLE sala (
                      id_sala INT AUTO_INCREMENT,
                      nombre_sala VARCHAR(50) NOT NULL,
                      tipo_sala VARCHAR(30) NULL,
                      capacidad INT NOT NULL,
                      estado VARCHAR(20) NOT NULL DEFAULT 'Activa',

                      CONSTRAINT pk_sala PRIMARY KEY (id_sala),
                      CONSTRAINT uq_sala_nombre UNIQUE (nombre_sala),
                      CONSTRAINT chk_sala_capacidad CHECK (capacidad > 0),
                      CONSTRAINT chk_sala_estado CHECK (estado IN ('Activa', 'Inactiva', 'Mantenimiento'))
);



CREATE TABLE empleado (
                          id_empleado INT AUTO_INCREMENT,
                          documento VARCHAR(20) NOT NULL,
                          nombres VARCHAR(80) NOT NULL,
                          apellidos VARCHAR(80) NOT NULL,
                          correo VARCHAR(100) NULL,
                          telefono VARCHAR(20) NULL,
                          cargo VARCHAR(50) NULL,
                          fecha_contratacion DATE NOT NULL DEFAULT (CURRENT_DATE),
                          estado VARCHAR(20) NOT NULL DEFAULT 'Activo',

                          CONSTRAINT pk_empleado PRIMARY KEY (id_empleado),
                          CONSTRAINT uq_empleado_documento UNIQUE (documento),
                          CONSTRAINT uq_empleado_correo UNIQUE (correo),
                          CONSTRAINT chk_empleado_estado CHECK (estado IN ('Activo', 'Inactivo'))
);



CREATE TABLE cliente (
                         id_cliente INT AUTO_INCREMENT,
                         documento VARCHAR(20) NULL,
                         nombres VARCHAR(80) NOT NULL,
                         apellidos VARCHAR(80) NOT NULL,
                         correo VARCHAR(100) NULL,
                         telefono VARCHAR(20) NULL,
                         fecha_registro DATE NOT NULL DEFAULT (CURRENT_DATE),
                         tipo_cliente VARCHAR(20) NOT NULL DEFAULT 'REGISTRADO',
                         estado VARCHAR(20) NOT NULL DEFAULT 'Activo',

                         CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
                         CONSTRAINT uq_cliente_documento UNIQUE (documento),
                         CONSTRAINT chk_cliente_tipo CHECK (tipo_cliente IN ('REGISTRADO', 'CONSUMIDOR_FINAL')),
                         CONSTRAINT chk_cliente_estado CHECK (estado IN ('Activo', 'Inactivo')),
                         CONSTRAINT chk_cliente_documento_registrado CHECK (
                             tipo_cliente != 'REGISTRADO' OR documento IS NOT NULL
)
    );



CREATE TABLE metodopago (
                            id_metodopago INT AUTO_INCREMENT,
                            nombre_metodo VARCHAR(50) NOT NULL,
                            descripcion VARCHAR(255) NULL,
                            estado VARCHAR(20) NOT NULL DEFAULT 'Activo',

                            CONSTRAINT pk_metodopago PRIMARY KEY (id_metodopago),
                            CONSTRAINT uq_metodopago_nombre UNIQUE (nombre_metodo),
                            CONSTRAINT chk_metodopago_estado CHECK (estado IN ('Activo', 'Inactivo'))
);


CREATE TABLE tipoentrada (
                             id_tipoentrada INT AUTO_INCREMENT,
                             nombre_tipo VARCHAR(50) NOT NULL,
                             descuento_porcentaje DECIMAL(5,2) NOT NULL DEFAULT 0,
                             descripcion VARCHAR(255) NULL,
                             estado VARCHAR(20) NOT NULL DEFAULT 'Activo',

                             CONSTRAINT pk_tipoentrada PRIMARY KEY (id_tipoentrada),
                             CONSTRAINT uq_tipoentrada_nombre UNIQUE (nombre_tipo),
                             CONSTRAINT chk_tipoentrada_descuento CHECK (descuento_porcentaje BETWEEN 0 AND 100),
                             CONSTRAINT chk_tipoentrada_estado CHECK (estado IN ('Activo', 'Inactivo'))
);


CREATE TABLE pelicula (
                          id_pelicula INT AUTO_INCREMENT,
                          titulo VARCHAR(150) NOT NULL,
                          sinopsis TEXT NULL,
                          clasificacion VARCHAR(10) NULL,
                          duracion_minutos INT NOT NULL,
                          estado VARCHAR(20) NOT NULL DEFAULT 'Activa',
                          id_genero INT NOT NULL,

                          CONSTRAINT pk_pelicula PRIMARY KEY (id_pelicula),
                          CONSTRAINT fk_pelicula_genero FOREIGN KEY (id_genero)
                              REFERENCES genero(id_genero),
                          CONSTRAINT chk_pelicula_duracion CHECK (duracion_minutos > 0),
                          CONSTRAINT chk_pelicula_estado CHECK (estado IN ('Activa', 'Inactiva'))
);



CREATE TABLE butaca (
                        id_butaca INT AUTO_INCREMENT,
                        fila VARCHAR(5) NOT NULL,
                        numero INT NOT NULL,
                        tipo_butaca VARCHAR(20) NOT NULL DEFAULT 'Regular',
                        estado VARCHAR(20) NOT NULL DEFAULT 'Activa',
                        id_sala INT NOT NULL,

                        CONSTRAINT pk_butaca PRIMARY KEY (id_butaca),
                        CONSTRAINT fk_butaca_sala FOREIGN KEY (id_sala)
                            REFERENCES sala(id_sala),
                        CONSTRAINT uq_butaca_sala_fila_numero UNIQUE (id_sala, fila, numero),
                        CONSTRAINT chk_butaca_numero CHECK (numero > 0),
                        CONSTRAINT chk_butaca_tipo CHECK (tipo_butaca IN ('Regular', 'VIP')),
                        CONSTRAINT chk_butaca_estado CHECK (estado IN ('Activa', 'Inactiva', 'Mantenimiento'))
);


CREATE TABLE funcion (
                         id_funcion INT AUTO_INCREMENT,
                         fecha_funcion DATE NOT NULL,
                         hora_inicio TIME NOT NULL,
                         hora_fin TIME NOT NULL,
                         tarifa_base DECIMAL(10,2) NOT NULL,
                         estado VARCHAR(20) NOT NULL DEFAULT 'Programada',
                         id_pelicula INT NOT NULL,
                         id_sala INT NOT NULL,

                         CONSTRAINT pk_funcion PRIMARY KEY (id_funcion),
                         CONSTRAINT fk_funcion_pelicula FOREIGN KEY (id_pelicula)
                             REFERENCES pelicula(id_pelicula),
                         CONSTRAINT fk_funcion_sala FOREIGN KEY (id_sala)
                             REFERENCES sala(id_sala),
                         CONSTRAINT chk_funcion_tarifa CHECK (tarifa_base >= 0),
                         CONSTRAINT chk_funcion_estado CHECK (
                             estado IN ('Programada', 'En_Curso', 'Finalizada', 'Cancelada')
                             ),
                         CONSTRAINT chk_funcion_horario CHECK (hora_fin > hora_inicio)
);



CREATE TABLE usuario (
                         id_usuario INT AUTO_INCREMENT,
                         nombre_usuario VARCHAR(50) NOT NULL,
                         contrasena_hash VARCHAR(255) NOT NULL,
                         rol VARCHAR(20) NOT NULL,
                         fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
                         id_empleado INT NULL,
                         id_cliente INT NULL,

                         CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
                         CONSTRAINT uq_usuario_nombre UNIQUE (nombre_usuario),
                         CONSTRAINT uq_usuario_empleado UNIQUE (id_empleado),
                         CONSTRAINT uq_usuario_cliente UNIQUE (id_cliente),
                         CONSTRAINT fk_usuario_empleado FOREIGN KEY (id_empleado)
                             REFERENCES empleado(id_empleado),
                         CONSTRAINT fk_usuario_cliente FOREIGN KEY (id_cliente)
                             REFERENCES cliente(id_cliente),
                         CONSTRAINT chk_usuario_rol CHECK (
                             rol IN ('ADMIN', 'CAJERO', 'SUPERVISOR', 'CLIENTE')
                             ),
                         CONSTRAINT chk_usuario_estado CHECK (
                             estado IN ('Activo', 'Inactivo', 'Bloqueado')
                             ),
                         CONSTRAINT chk_usuario_rol_referencia CHECK (
                             (
                                 rol IN ('ADMIN', 'CAJERO', 'SUPERVISOR')
                                     AND id_empleado IS NOT NULL
                                     AND id_cliente IS NULL
                                 )
                                 OR
                             (
                                 rol = 'CLIENTE'
                                     AND id_cliente IS NOT NULL
                                     AND id_empleado IS NULL
                                 )
                             )
);



CREATE TABLE venta (
                       id_venta INT AUTO_INCREMENT,
                       fecha_venta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       subtotal DECIMAL(10,2) NOT NULL,
                       descuento_total DECIMAL(10,2) NOT NULL DEFAULT 0,
                       total_pagado DECIMAL(10,2) NOT NULL,
                       canal_venta VARCHAR(20) NOT NULL,
                       observacion VARCHAR(255) NULL,
                       estado VARCHAR(20) NOT NULL DEFAULT 'Activa',
                       id_cliente INT NOT NULL,
                       id_empleado INT NULL,
                       id_metodopago INT NOT NULL,

                       CONSTRAINT pk_venta PRIMARY KEY (id_venta),
                       CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente)
                           REFERENCES cliente(id_cliente),
                       CONSTRAINT fk_venta_empleado FOREIGN KEY (id_empleado)
                           REFERENCES empleado(id_empleado),
                       CONSTRAINT fk_venta_metodopago FOREIGN KEY (id_metodopago)
                           REFERENCES metodopago(id_metodopago),
                       CONSTRAINT chk_venta_subtotal CHECK (subtotal >= 0),
                       CONSTRAINT chk_venta_descuento CHECK (descuento_total >= 0),
                       CONSTRAINT chk_venta_total CHECK (total_pagado >= 0),
                       CONSTRAINT chk_venta_canal CHECK (canal_venta IN ('TAQUILLA', 'EN_LINEA')),
                       CONSTRAINT chk_venta_estado CHECK (
                           estado IN ('Activa', 'Anulada', 'Devuelta')
                           ),
                       CONSTRAINT chk_venta_canal_empleado CHECK (
                           (canal_venta = 'TAQUILLA' AND id_empleado IS NOT NULL)
                               OR
                           (canal_venta = 'EN_LINEA')
                           ),
                       CONSTRAINT chk_venta_totales CHECK (
                           total_pagado = subtotal - descuento_total
                           )
);




CREATE TABLE entrada (
                         id_entrada INT AUTO_INCREMENT,
                         precio_base DECIMAL(10,2) NOT NULL,
                         cargo_butaca DECIMAL(10,2) NOT NULL DEFAULT 0,
                         descuento DECIMAL(10,2) NOT NULL DEFAULT 0,
                         precio_final DECIMAL(10,2) NOT NULL,
                         es_gratis BOOLEAN NOT NULL DEFAULT FALSE,
                         estado VARCHAR(20) NOT NULL DEFAULT 'Activa',
                         id_venta INT NOT NULL,
                         id_funcion INT NOT NULL,
                         id_butaca INT NOT NULL,
                         id_tipoentrada INT NOT NULL,

                         CONSTRAINT pk_entrada PRIMARY KEY (id_entrada),
                         CONSTRAINT fk_entrada_venta FOREIGN KEY (id_venta)
                             REFERENCES venta(id_venta),
                         CONSTRAINT fk_entrada_funcion FOREIGN KEY (id_funcion)
                             REFERENCES funcion(id_funcion),
                         CONSTRAINT fk_entrada_butaca FOREIGN KEY (id_butaca)
                             REFERENCES butaca(id_butaca),
                         CONSTRAINT fk_entrada_tipoentrada FOREIGN KEY (id_tipoentrada)
                             REFERENCES tipoentrada(id_tipoentrada),
                         CONSTRAINT chk_entrada_precio_base CHECK (precio_base >= 0),
                         CONSTRAINT chk_entrada_cargo CHECK (cargo_butaca >= 0),
                         CONSTRAINT chk_entrada_descuento CHECK (descuento >= 0),
                         CONSTRAINT chk_entrada_precio_final_valor CHECK (precio_final >= 0),
                         CONSTRAINT chk_entrada_estado CHECK (
                             estado IN ('Activa', 'Anulada', 'Devuelta')
                             ),
                         CONSTRAINT chk_entrada_precio_final CHECK (
                             precio_final = precio_base + cargo_butaca - descuento
                             ),
                         CONSTRAINT chk_entrada_gratis CHECK (
                             (es_gratis = TRUE AND precio_final = 0)
                                 OR
                             (es_gratis = FALSE)
                             )
);



CREATE TABLE historial_puntos (
                                  id_historial INT AUTO_INCREMENT,
                                  tipo_movimiento VARCHAR(20) NOT NULL,
                                  puntos_ganados INT NOT NULL DEFAULT 0,
                                  puntos_canjeados INT NOT NULL DEFAULT 0,
                                  fecha_movimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  descripcion VARCHAR(255) NULL,
                                  id_cliente INT NOT NULL,
                                  id_venta INT NULL,
                                  id_entrada INT NULL,

                                  CONSTRAINT pk_historial_puntos PRIMARY KEY (id_historial),
                                  CONSTRAINT uq_historial_entrada UNIQUE (id_entrada),
                                  CONSTRAINT fk_historial_cliente FOREIGN KEY (id_cliente)
                                      REFERENCES cliente(id_cliente),
                                  CONSTRAINT fk_historial_venta FOREIGN KEY (id_venta)
                                      REFERENCES venta(id_venta),
                                  CONSTRAINT fk_historial_entrada FOREIGN KEY (id_entrada)
                                      REFERENCES entrada(id_entrada),
                                  CONSTRAINT chk_historial_tipo CHECK (
                                      tipo_movimiento IN ('GANADO', 'CANJEADO')
                                      ),
                                  CONSTRAINT chk_historial_puntos_ganados CHECK (puntos_ganados >= 0),
                                  CONSTRAINT chk_historial_puntos_canjeados CHECK (puntos_canjeados >= 0),
                                  CONSTRAINT chk_historial_tipo_referencia CHECK (
                                      (
                                          tipo_movimiento = 'GANADO'
                                              AND puntos_ganados > 0
                                              AND puntos_canjeados = 0
                                              AND id_entrada IS NOT NULL
                                              AND id_venta IS NULL
                                          )
                                          OR
                                      (
                                          tipo_movimiento = 'CANJEADO'
                                              AND puntos_canjeados > 0
                                              AND puntos_ganados = 0
                                              AND id_venta IS NOT NULL
                                              AND id_entrada IS NULL
                                          )
                                      )
);

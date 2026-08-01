-- =========================================================
-- DATOS INICIALES (CATALOGOS FIJOS)
-- SISTEMA DE GESTION DE CINE
--
-- Requiere haber ejecutado antes database/schema.sql
--
-- `tipoentrada` y `metodopago` NO tienen pantalla de alta/edicion en la
-- aplicacion: son catalogos fijos definidos por las reglas del negocio
-- (un tipo de entrada trae asociado un porcentaje de descuento definido
-- de antemano; un metodo de pago es una forma de cobro que el cine ya
-- decidio aceptar). Un cajero o un cliente solo deben poder ELEGIR uno
-- de estos valores al momento de vender/comprar (ComboBox en la pantalla
-- de Ventas), nunca inventar uno nuevo desde ahi.
--
-- Si en el futuro el negocio agrega un metodo de pago o un tipo de
-- entrada nuevo, se hace con un INSERT como estos (por un administrador
-- de base de datos), no desde la aplicacion.
--
-- Este script se puede ejecutar varias veces sin duplicar filas: las
-- restricciones UNIQUE (uq_tipoentrada_nombre, uq_metodopago_nombre)
-- hacen que un segundo INSERT con el mismo nombre falle; por eso se usa
-- INSERT IGNORE.
-- =========================================================

USE cine;


-- =========================================================
-- TIPOS DE ENTRADA
-- descuento_porcentaje se aplica sobre funcion.tarifa_base dentro de
-- sp_registrar_venta (ver docs/procedimientos_bd.md).
-- =========================================================

INSERT IGNORE INTO tipoentrada (nombre_tipo, descuento_porcentaje, descripcion, estado) VALUES
    ('General',                0.00,  'Entrada estandar, sin descuento.',                              'ACTIVO'),
    ('Niño',                   30.00, 'Aplica a menores de 12 años, presentando documento.',           'ACTIVO'),
    ('Estudiante',             20.00, 'Aplica presentando carnet estudiantil vigente.',                 'ACTIVO'),
    ('Tercera edad',           25.00, 'Aplica a personas mayores de 60 años, presentando documento.',  'ACTIVO'),
    ('Canje de puntos',       100.00, 'Entrada gratuita canjeada por 9 puntos de fidelidad (BR-28/29).', 'ACTIVO');


-- =========================================================
-- METODOS DE PAGO
-- =========================================================

INSERT IGNORE INTO metodopago (nombre_metodo, descripcion, estado) VALUES
    ('Efectivo',                 'Pago en efectivo en taquilla.',                         'ACTIVO'),
    ('Tarjeta de credito/debito','Pago con tarjeta mediante datafono.',                   'ACTIVO'),
    ('Transferencia/QR',         'Pago mediante transferencia bancaria o codigo QR.',     'ACTIVO');


-- =========================================================
-- SALAS
-- Un par de salas de ejemplo para poder programar Funciones desde el
-- primer momento. Igual que tipoentrada/metodopago, se puede correr
-- varias veces sin duplicar filas (uq_sala_nombre + INSERT IGNORE).
-- La capacidad respeta chk_sala_capacidad (maximo 48 butacas por sala).
-- =========================================================

INSERT IGNORE INTO sala (nombre_sala, capacidad, estado) VALUES
    ('Sala 1', 48, 'ACTIVA'),
    ('Sala 2', 40, 'ACTIVA');


-- =========================================================
-- COMPROBACIÓN
-- =========================================================

SELECT * FROM tipoentrada;
SELECT * FROM metodopago;
SELECT * FROM sala;

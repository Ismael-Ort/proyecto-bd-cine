-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS Y TRIGGER
-- SISTEMA DE GESTION DE CINE
-- Motor: MySQL / MariaDB
--
-- Requiere haber ejecutado antes database/schema.sql.
--
-- IMPORTANTE: este archivo NO se ejecuta al crear la base de
-- datos. Es intencionalmente independiente de schema.sql.
-- Se ejecuta mas adelante, cuando en el programa Java se vaya
-- a implementar la parte que registra ventas y confirma pagos,
-- momento en el que conviene leer con calma cada bloque y
-- docs/procedimientos_bd.md antes de correrlo.
--
-- El proyecto exige, como minimo, un procedimiento almacenado,
-- un trigger y una transaccion. Aqui hay lo minimo necesario
-- para cubrir un flujo real (registrar una venta, confirmar su
-- pago, y mantener al dia el estado de las funciones), sin usar
-- funciones almacenadas (no se vieron en clase):
--
--   2 triggers   -> trg_acumula_puntos
--                -> trg_actualizar_estado_funcion
--   2 procedimientos, ambos con su propia transaccion:
--                -> sp_registrar_venta
--                -> sp_confirmar_pago
-- =========================================================

USE cine;


-- =========================================================
-- TRIGGER: trg_acumula_puntos
-- AFTER UPDATE ON entrada
--
-- Cuando una entrada pasa de RESERVADA a PAGADA (y no es una
-- entrada gratuita, es decir precio_final > 0), se registra
-- automaticamente 1 punto de fidelidad (ACUMULACION) para el
-- cliente de esa venta. Asi, sp_confirmar_pago no necesita
-- insertar el punto "a mano": el trigger lo hace solo.
-- =========================================================

DELIMITER $$

CREATE TRIGGER trg_acumula_puntos
AFTER UPDATE ON entrada
FOR EACH ROW
BEGIN
    DECLARE v_id_cliente INT;

    IF NEW.estado = 'PAGADA' AND OLD.estado <> 'PAGADA' AND NEW.precio_final > 0 THEN
        SELECT id_cliente INTO v_id_cliente FROM venta WHERE id_venta = NEW.id_venta;

        INSERT INTO historial_puntos (tipo_movimiento, cantidad_puntos, descripcion, id_cliente, id_venta)
        VALUES ('ACUMULACIÓN', 1, CONCAT('Punto generado por la entrada #', NEW.id_entrada), v_id_cliente, NEW.id_venta);
    END IF;
END$$

DELIMITER ;


-- =========================================================
-- TRIGGER: trg_actualizar_estado_funcion
-- BEFORE UPDATE ON funcion
--
-- Recalcula el estado de una funcion (PROGRAMADA/EN_CURSO/
-- FINALIZADA) comparando fecha_funcion/hora_inicio/hora_fin
-- contra la hora del propio SERVIDOR de MySQL (CURDATE(),
-- CURTIME()), no contra la hora de la computadora que corre el
-- programa Java. Asi, aunque dos computadoras tengan el reloj
-- desincronizado, el estado que queda guardado es siempre el
-- mismo para todos: el trigger no confia en ningun reloj externo.
--
-- No se dispara solo cada cierto tiempo (un trigger no puede
-- hacer eso): se dispara cuando Java ejecuta un UPDATE sobre
-- `funcion`, ya sea porque se edito una funcion desde el
-- formulario, o porque FuncionBD.actualizarEstadosAutomaticos()
-- hizo un UPDATE "de repaso" (SET estado = estado) al abrir la
-- pantalla de Funciones o el Panel de control.
--
-- Si NEW.estado ya viene como 'CANCELADA' (porque asi la dejo el
-- repaso automatico, o porque el usuario la cancelo a mano desde
-- el formulario), el trigger no la toca: una funcion cancelada
-- nunca cambia de estado sola.
-- =========================================================

DELIMITER $$

CREATE TRIGGER trg_actualizar_estado_funcion
BEFORE UPDATE ON funcion
FOR EACH ROW
BEGIN
    IF NEW.estado <> 'CANCELADA' THEN
        IF NEW.fecha_funcion < CURDATE()
           OR (NEW.fecha_funcion = CURDATE() AND CURTIME() >= NEW.hora_fin) THEN
            SET NEW.estado = 'FINALIZADA';
        ELSEIF NEW.fecha_funcion = CURDATE()
               AND CURTIME() >= NEW.hora_inicio
               AND CURTIME() < NEW.hora_fin THEN
            SET NEW.estado = 'EN_CURSO';
        ELSE
            SET NEW.estado = 'PROGRAMADA';
        END IF;
    END IF;
END$$

DELIMITER ;


-- =========================================================
-- PROCEDIMIENTO: sp_registrar_venta
--
-- Registra una venta con su entrada, calculando el precio a
-- partir de la tarifa base de la funcion y el descuento del
-- tipo de entrada (no hace falta que Java calcule el precio,
-- se lo pasa el procedimiento en las salidas si lo necesita
-- consultar despues con un SELECT).
--
-- Para vender una entrada "gratis" (canje de puntos), no hace
-- falta otro procedimiento: basta con llamar a este mismo
-- procedimiento usando un tipo_entrada con
-- descuento_porcentaje = 100 (precio_final queda en 0). Luego,
-- desde Java, se inserta el movimiento CANJE correspondiente
-- en historial_puntos (ver docs/procedimientos_bd.md).
--
-- Toda la operacion ocurre dentro de una unica transaccion:
-- si algo falla a mitad de camino, se revierte por completo.
-- =========================================================

DELIMITER $$

CREATE PROCEDURE sp_registrar_venta(
    IN  p_id_cliente     INT,
    IN  p_id_empleado    INT,
    IN  p_canal_venta    VARCHAR(20),
    IN  p_id_metodopago  INT,
    IN  p_observacion    VARCHAR(255),
    IN  p_id_funcion     INT,
    IN  p_id_butaca      INT,
    IN  p_id_tipoentrada INT,
    OUT p_id_venta       INT,
    OUT p_id_entrada     INT
)
BEGIN
    DECLARE v_estado_funcion VARCHAR(20);
    DECLARE v_tarifa_base DECIMAL(10,2);
    DECLARE v_descuento_pct DECIMAL(5,2);
    DECLARE v_precio_base DECIMAL(10,2);
    DECLARE v_descuento DECIMAL(10,2);
    DECLARE v_precio_final DECIMAL(10,2);
    DECLARE v_id_entrada_existente INT DEFAULT NULL;
    DECLARE v_estado_existente VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- Validaciones que no necesitan bloquear filas todavia.
    IF p_canal_venta = 'TAQUILLA' AND p_id_empleado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Las ventas por TAQUILLA requieren un empleado.';
    END IF;

    SELECT estado, tarifa_base INTO v_estado_funcion, v_tarifa_base
        FROM funcion WHERE id_funcion = p_id_funcion;

    IF v_tarifa_base IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La funcion indicada no existe.';
    END IF;
    IF v_estado_funcion NOT IN ('PROGRAMADA', 'EN_CURSO') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La funcion esta cancelada o finalizada.';
    END IF;

    SELECT descuento_porcentaje INTO v_descuento_pct
        FROM tipoentrada WHERE id_tipoentrada = p_id_tipoentrada;

    IF v_descuento_pct IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El tipo de entrada indicado no existe.';
    END IF;

    SET v_precio_base  = v_tarifa_base;
    SET v_descuento    = ROUND(v_precio_base * v_descuento_pct / 100, 2);
    SET v_precio_final = v_precio_base - v_descuento;

    START TRANSACTION;

    INSERT INTO venta (canal_venta, observacion, id_cliente, id_empleado, id_metodopago, total_pagado)
    VALUES (p_canal_venta, p_observacion, p_id_cliente, p_id_empleado, p_id_metodopago, v_precio_final);

    SET p_id_venta = LAST_INSERT_ID();

    -- La tabla entrada tiene uq_entrada_funcion_butaca: solo puede
    -- existir UNA fila para cada (id_funcion, id_butaca). Si esa
    -- butaca ya tuvo una entrada CANCELADA para esta funcion, se
    -- reutiliza esa fila con UPDATE en vez de intentar un INSERT
    -- que chocaria contra la restriccion UNIQUE.
    SELECT id_entrada, estado INTO v_id_entrada_existente, v_estado_existente
        FROM entrada
        WHERE id_funcion = p_id_funcion AND id_butaca = p_id_butaca
        FOR UPDATE;

    IF v_id_entrada_existente IS NOT NULL AND v_estado_existente <> 'CANCELADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La butaca ya esta ocupada para esta funcion.';
    END IF;

    IF v_id_entrada_existente IS NULL THEN
        INSERT INTO entrada (precio_base, descuento, precio_final, id_venta, id_funcion, id_butaca, id_tipoentrada)
        VALUES (v_precio_base, v_descuento, v_precio_final, p_id_venta, p_id_funcion, p_id_butaca, p_id_tipoentrada);

        SET p_id_entrada = LAST_INSERT_ID();
    ELSE
        UPDATE entrada
            SET precio_base    = v_precio_base,
                descuento      = v_descuento,
                precio_final   = v_precio_final,
                estado         = 'RESERVADA',
                id_venta       = p_id_venta,
                id_tipoentrada = p_id_tipoentrada
            WHERE id_entrada = v_id_entrada_existente;

        SET p_id_entrada = v_id_entrada_existente;
    END IF;

    COMMIT;
END$$

DELIMITER ;


-- =========================================================
-- PROCEDIMIENTO: sp_confirmar_pago
--
-- Confirma el pago de una entrada RESERVADA: la marca como
-- PAGADA (lo que dispara trg_acumula_puntos) y marca la venta
-- como COMPLETADA. Tambien es una transaccion completa.
-- =========================================================

DELIMITER $$

CREATE PROCEDURE sp_confirmar_pago(
    IN p_id_entrada INT
)
BEGIN
    DECLARE v_estado VARCHAR(20);
    DECLARE v_id_venta INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT estado, id_venta INTO v_estado, v_id_venta
        FROM entrada WHERE id_entrada = p_id_entrada FOR UPDATE;

    IF v_estado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La entrada indicada no existe.';
    END IF;
    IF v_estado <> 'RESERVADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede confirmar el pago de una entrada en estado RESERVADA.';
    END IF;

    UPDATE entrada SET estado = 'PAGADA' WHERE id_entrada = p_id_entrada;

    UPDATE venta SET estado = 'COMPLETADA' WHERE id_venta = v_id_venta;

    COMMIT;
END$$

DELIMITER ;

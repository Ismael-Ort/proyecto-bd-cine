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
-- un trigger y una transaccion. Aqui hay lo necesario para
-- cubrir un flujo real (registrar una venta, confirmar su pago,
-- mantener al dia el estado de las funciones, y cancelar una
-- funcion en cascada), sin usar funciones almacenadas (no se
-- vieron en clase):
--
--   3 triggers   -> trg_acumula_puntos
--                -> trg_actualizar_estado_funcion
--                -> trg_cancelar_entradas_por_funcion
--   3 procedimientos, todos con su propia transaccion:
--                -> sp_registrar_venta
--                -> sp_confirmar_pago
--                -> sp_cancelar_funcion
--
-- AUDITORIA (confirmado que los 6 objetos de este archivo se usan desde
-- el programa Java, ninguno quedo huerfano):
--   sp_registrar_venta  <- VentaBD.registrarVenta()
--   sp_confirmar_pago   <- VentaBD.confirmarPago()
--   sp_cancelar_funcion <- FuncionBD.cancelarFuncion()
--   trg_acumula_puntos, trg_actualizar_estado_funcion y
--   trg_cancelar_entradas_por_funcion no se llaman directo (un trigger no
--   se invoca, se dispara solo): se activan como efecto de los UPDATE que
--   hacen los 3 procedimientos de arriba y FuncionBD.actualizarEstadosAutomaticos()
--   (ver el comentario de cada trigger mas abajo para el detalle).
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
-- TRIGGER: trg_cancelar_entradas_por_funcion
-- AFTER UPDATE ON funcion
--
-- Cuando una funcion pasa a CANCELADA (por sp_cancelar_funcion,
-- ver mas abajo), cancela en cascada todo lo que dependia de
-- ella, "devolviendo" tanto la venta como los puntos ganados:
--
--   1. Por cada entrada de esa funcion que ya estaba PAGADA (ya
--      habia generado un punto via trg_acumula_puntos), inserta
--      un movimiento CANJE que resta ese punto.
--   2. Cancela todas las entradas de la funcion (menos las que
--      ya estaban CANCELADA).
--   3. Cancela las ventas que tenian entradas en esa funcion.
--
-- Todo esto corre dentro de la misma transaccion que abre
-- sp_cancelar_funcion: si algo aqui falla, el UPDATE de funcion
-- que disparo este trigger tambien se revierte.
-- =========================================================

DELIMITER $$

CREATE TRIGGER trg_cancelar_entradas_por_funcion
AFTER UPDATE ON funcion
FOR EACH ROW
BEGIN
    IF NEW.estado = 'CANCELADA' AND OLD.estado <> 'CANCELADA' THEN

        INSERT INTO historial_puntos (tipo_movimiento, cantidad_puntos, descripcion, id_cliente, id_venta)
        SELECT 'CANJE', 1,
               CONCAT('Reverso de punto por cancelacion de la funcion #', NEW.id_funcion, ' (entrada #', e.id_entrada, ')'),
               v.id_cliente, e.id_venta
        FROM entrada e
        JOIN venta v ON v.id_venta = e.id_venta
        WHERE e.id_funcion = NEW.id_funcion AND e.estado = 'PAGADA' AND e.precio_final > 0;

        UPDATE entrada SET estado = 'CANCELADA'
            WHERE id_funcion = NEW.id_funcion AND estado <> 'CANCELADA';

        UPDATE venta SET estado = 'CANCELADA'
            WHERE estado <> 'CANCELADA'
              AND id_venta IN (SELECT id_venta FROM entrada WHERE id_funcion = NEW.id_funcion);

    END IF;
END$$

DELIMITER ;


-- =========================================================
-- PROCEDIMIENTO: sp_registrar_venta
--
-- Registra una entrada, calculando el precio a partir de la
-- tarifa base de la funcion y el descuento del tipo de entrada
-- (no hace falta que Java calcule el precio, se lo pasa el
-- procedimiento en las salidas si lo necesita consultar despues
-- con un SELECT).
--
-- BR-19: una venta puede tener varias entradas (varias butacas
-- en una sola compra). p_id_venta_existente controla eso:
--   - NULL       -> crea una venta nueva (primera butaca de la compra).
--   - un id_venta -> reutiliza esa venta (butacas siguientes de la
--                    misma compra) y le suma el precio de esta
--                    entrada a su total_pagado, en vez de crear
--                    una venta aparte. Java debe llamar a este
--                    procedimiento una vez por butaca, pasando en
--                    la segunda llamada en adelante el p_id_venta
--                    que devolvio la primera (ver docs/procedimientos_bd.md).
--
-- Para vender una entrada "gratis" (canje de puntos), no hace
-- falta otro procedimiento: basta con llamar a este mismo
-- procedimiento usando un tipo_entrada con
-- descuento_porcentaje = 100 (precio_final queda en 0). Luego,
-- desde Java, se inserta el movimiento CANJE correspondiente
-- en historial_puntos (ver docs/procedimientos_bd.md).
--
-- Cada llamada ocurre dentro de su propia transaccion: si algo
-- falla, esa llamada se revierte completa (las butacas de la
-- misma compra que ya se hubieran registrado en llamadas
-- anteriores quedan como estaban, no se deshacen).
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
    IN  p_id_venta_existente INT,
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

    IF p_id_venta_existente IS NULL THEN
        INSERT INTO venta (canal_venta, observacion, id_cliente, id_empleado, id_metodopago, total_pagado)
        VALUES (p_canal_venta, p_observacion, p_id_cliente, p_id_empleado, p_id_metodopago, v_precio_final);

        SET p_id_venta = LAST_INSERT_ID();
    ELSE
        SET p_id_venta = p_id_venta_existente;

        UPDATE venta SET total_pagado = total_pagado + v_precio_final WHERE id_venta = p_id_venta;
    END IF;

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


-- =========================================================
-- PROCEDIMIENTO: sp_cancelar_funcion
--
-- Cancela una funcion. El propio UPDATE dispara
-- trg_cancelar_entradas_por_funcion (ver arriba), que en la
-- misma transaccion cancela las entradas y ventas asociadas y
-- devuelve los puntos de fidelidad ya ganados. Por eso este
-- procedimiento no necesita tocar entrada/venta/historial_puntos
-- el mismo: le basta con cambiar el estado de la funcion.
-- =========================================================

DELIMITER $$

CREATE PROCEDURE sp_cancelar_funcion(
    IN p_id_funcion INT
)
BEGIN
    DECLARE v_estado VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT estado INTO v_estado FROM funcion WHERE id_funcion = p_id_funcion FOR UPDATE;

    IF v_estado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La funcion indicada no existe.';
    END IF;
    IF v_estado = 'CANCELADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Esa funcion ya estaba cancelada.';
    END IF;

    UPDATE funcion SET estado = 'CANCELADA' WHERE id_funcion = p_id_funcion;

    COMMIT;
END$$

DELIMITER ;

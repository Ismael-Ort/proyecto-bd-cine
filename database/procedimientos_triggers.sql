-- =========================================================
-- PROCEDIMIENTOS ALMACENADOS, FUNCIONES Y TRIGGERS
-- SISTEMA DE GESTION DE CINE
-- Motor: MySQL / MariaDB
--
-- Requiere haber ejecutado antes database/schema.sql
-- Explicado en detalle en docs/procedimientos_bd.md
-- =========================================================


-- =========================================================
-- FUNCION: fn_funcion_disponible
-- Indica si una funcion todavia admite venta de entradas
-- (no esta CANCELADA ni FINALIZADA).
-- =========================================================

DELIMITER $$

CREATE FUNCTION fn_funcion_disponible(p_id_funcion INT)
RETURNS BOOLEAN
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_estado VARCHAR(20);

    SELECT estado INTO v_estado FROM funcion WHERE id_funcion = p_id_funcion;

    RETURN v_estado IN ('PROGRAMADA', 'EN_CURSO');
END$$

DELIMITER ;


-- =========================================================
-- FUNCION: fn_puntos_disponibles
-- Calcula el saldo de puntos de fidelidad de un cliente:
-- suma de ACUMULACION menos suma de CANJE.
-- =========================================================

DELIMITER $$

CREATE FUNCTION fn_puntos_disponibles(p_id_cliente INT)
RETURNS INT
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_saldo INT;

    SELECT COALESCE(SUM(
               CASE WHEN tipo_movimiento = 'ACUMULACIÓN' THEN cantidad_puntos
                    ELSE -cantidad_puntos
               END
           ), 0)
        INTO v_saldo
        FROM historial_puntos
        WHERE id_cliente = p_id_cliente;

    RETURN v_saldo;
END$$

DELIMITER ;


-- =========================================================
-- TRIGGER: trg_entrada_bi_valida_funcion
-- BEFORE INSERT ON entrada
-- Evita crear una entrada para una funcion cancelada/finalizada
-- o para una butaca que no este ACTIVA.
-- =========================================================

DELIMITER $$

CREATE TRIGGER trg_entrada_bi_valida_funcion
BEFORE INSERT ON entrada
FOR EACH ROW
BEGIN
    IF NOT fn_funcion_disponible(NEW.id_funcion) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'No se puede vender una entrada para una funcion cancelada o finalizada.';
    END IF;

    IF (SELECT estado FROM butaca WHERE id_butaca = NEW.id_butaca) <> 'ACTIVA' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La butaca seleccionada no esta activa.';
    END IF;
END$$

DELIMITER ;


-- =========================================================
-- TRIGGER: trg_entrada_bu_valida_reventa
-- BEFORE UPDATE ON entrada
-- Cuando una entrada CANCELADA se reutiliza para vender de
-- nuevo la misma butaca (cambia a otro estado), se repiten
-- las mismas validaciones que en el INSERT.
-- =========================================================

DELIMITER $$

CREATE TRIGGER trg_entrada_bu_valida_reventa
BEFORE UPDATE ON entrada
FOR EACH ROW
BEGIN
    IF OLD.estado = 'CANCELADA' AND NEW.estado <> 'CANCELADA' THEN
        IF NOT fn_funcion_disponible(NEW.id_funcion) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'No se puede revender una butaca de una funcion cancelada o finalizada.';
        END IF;

        IF (SELECT estado FROM butaca WHERE id_butaca = NEW.id_butaca) <> 'ACTIVA' THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'La butaca seleccionada no esta activa.';
        END IF;
    END IF;
END$$

DELIMITER ;


-- =========================================================
-- TRIGGER: trg_entrada_au_acumula_puntos
-- AFTER UPDATE ON entrada
-- 1) Cuando una entrada pasa a PAGADA y su precio_final es
--    mayor que cero, registra automaticamente un movimiento
--    ACUMULACION en historial_puntos (BR-28). Las entradas
--    gratuitas (precio_final = 0, canjeadas por puntos) NO
--    generan nuevos puntos (BR-29).
-- 2) Cuando todas las entradas de una venta quedan en un
--    estado final (PAGADA, UTILIZADA o CANCELADA), actualiza
--    el estado de la venta a COMPLETADA (si hubo al menos una
--    entrada pagada/utilizada) o CANCELADA (si todas fueron
--    canceladas).
-- =========================================================

DELIMITER $$

CREATE TRIGGER trg_entrada_au_acumula_puntos
AFTER UPDATE ON entrada
FOR EACH ROW
BEGIN
    DECLARE v_id_cliente INT;
    DECLARE v_total_entradas INT;
    DECLARE v_entradas_finalizadas INT;
    DECLARE v_entradas_honradas INT;

    IF NEW.estado = 'PAGADA' AND OLD.estado <> 'PAGADA' AND NEW.precio_final > 0 THEN
        SELECT id_cliente INTO v_id_cliente FROM venta WHERE id_venta = NEW.id_venta;

        INSERT INTO historial_puntos (tipo_movimiento, cantidad_puntos, descripcion, id_cliente, id_venta)
        VALUES ('ACUMULACIÓN', 1, CONCAT('Acumulacion automatica por entrada #', NEW.id_entrada), v_id_cliente, NEW.id_venta);
    END IF;

    IF NEW.estado IN ('PAGADA', 'UTILIZADA', 'CANCELADA') AND OLD.estado <> NEW.estado THEN
        SELECT COUNT(*) INTO v_total_entradas
            FROM entrada WHERE id_venta = NEW.id_venta;

        SELECT COUNT(*) INTO v_entradas_finalizadas
            FROM entrada
            WHERE id_venta = NEW.id_venta
              AND estado IN ('PAGADA', 'UTILIZADA', 'CANCELADA');

        IF v_total_entradas = v_entradas_finalizadas THEN
            SELECT COUNT(*) INTO v_entradas_honradas
                FROM entrada
                WHERE id_venta = NEW.id_venta
                  AND estado IN ('PAGADA', 'UTILIZADA');

            IF v_entradas_honradas > 0 THEN
                UPDATE venta SET estado = 'COMPLETADA'
                    WHERE id_venta = NEW.id_venta AND estado <> 'CANCELADA';
            ELSE
                UPDATE venta SET estado = 'CANCELADA'
                    WHERE id_venta = NEW.id_venta AND estado <> 'CANCELADA';
            END IF;
        END IF;
    END IF;
END$$

DELIMITER ;


-- =========================================================
-- PROCEDIMIENTO: sp_registrar_venta_simple
-- Registra una venta nueva con su primera entrada, calculando
-- el precio a partir de la tarifa base de la funcion y el
-- descuento del tipo de entrada. Toda la operacion se ejecuta
-- como una unica transaccion (BR-35): si algo falla, se
-- revierte por completo.
--
-- Si la butaca ya tuvo una entrada CANCELADA para la misma
-- funcion, esa fila se reutiliza (UPDATE) en lugar de crear
-- una nueva, porque uq_entrada_funcion_butaca no permite dos
-- filas para el mismo (id_funcion, id_butaca).
-- =========================================================

DELIMITER $$

CREATE PROCEDURE sp_registrar_venta_simple(
    IN  p_id_cliente     INT,
    IN  p_id_empleado    INT,
    IN  p_canal_venta    VARCHAR(20),
    IN  p_id_metodopago  INT,
    IN  p_observacion    VARCHAR(255),
    IN  p_id_funcion     INT,
    IN  p_id_butaca      INT,
    IN  p_id_tipoentrada INT,
    IN  p_motivo         VARCHAR(255),
    OUT p_id_venta       INT,
    OUT p_id_entrada     INT
)
BEGIN
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

    IF p_canal_venta = 'TAQUILLA' AND p_id_empleado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Las ventas por TAQUILLA requieren un empleado.';
    END IF;

    SELECT tarifa_base INTO v_tarifa_base FROM funcion WHERE id_funcion = p_id_funcion;
    IF v_tarifa_base IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La funcion indicada no existe.';
    END IF;

    SELECT descuento_porcentaje INTO v_descuento_pct FROM tipoentrada WHERE id_tipoentrada = p_id_tipoentrada;
    IF v_descuento_pct IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El tipo de entrada indicado no existe.';
    END IF;

    SET v_precio_base  = v_tarifa_base;
    SET v_descuento    = ROUND(v_precio_base * v_descuento_pct / 100, 2);
    SET v_precio_final = v_precio_base - v_descuento;

    START TRANSACTION;

    INSERT INTO venta (canal_venta, observacion, id_cliente, id_empleado, id_metodopago, total_pagado)
    VALUES (p_canal_venta, p_observacion, p_id_cliente, p_id_empleado, p_id_metodopago, 0);

    SET p_id_venta = LAST_INSERT_ID();

    SELECT id_entrada, estado INTO v_id_entrada_existente, v_estado_existente
        FROM entrada
        WHERE id_funcion = p_id_funcion AND id_butaca = p_id_butaca
        FOR UPDATE;

    IF v_id_entrada_existente IS NOT NULL AND v_estado_existente <> 'CANCELADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La butaca ya esta ocupada para esta funcion.';
    END IF;

    IF v_id_entrada_existente IS NULL THEN
        INSERT INTO entrada (precio_base, descuento, precio_final, motivo, id_venta, id_funcion, id_butaca, id_tipoentrada)
        VALUES (v_precio_base, v_descuento, v_precio_final, p_motivo, p_id_venta, p_id_funcion, p_id_butaca, p_id_tipoentrada);

        SET p_id_entrada = LAST_INSERT_ID();
    ELSE
        UPDATE entrada
            SET precio_base    = v_precio_base,
                descuento      = v_descuento,
                precio_final   = v_precio_final,
                motivo         = p_motivo,
                estado         = 'RESERVADA',
                id_venta       = p_id_venta,
                id_tipoentrada = p_id_tipoentrada
            WHERE id_entrada = v_id_entrada_existente;

        SET p_id_entrada = v_id_entrada_existente;
    END IF;

    UPDATE venta SET total_pagado = v_precio_final WHERE id_venta = p_id_venta;

    COMMIT;
END$$

DELIMITER ;


-- =========================================================
-- PROCEDIMIENTO: sp_agregar_entrada_a_venta
-- Agrega una entrada adicional a una venta que ya existe y
-- que todavia esta PENDIENTE (venta con multiples entradas,
-- BR-19). Recalcula total_pagado sumando todas las entradas
-- no canceladas de la venta. Tambien es una transaccion
-- completa con reversion ante cualquier error.
-- =========================================================

DELIMITER $$

CREATE PROCEDURE sp_agregar_entrada_a_venta(
    IN  p_id_venta       INT,
    IN  p_id_funcion     INT,
    IN  p_id_butaca      INT,
    IN  p_id_tipoentrada INT,
    IN  p_motivo         VARCHAR(255),
    OUT p_id_entrada     INT
)
BEGIN
    DECLARE v_estado_venta VARCHAR(20);
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

    SELECT estado INTO v_estado_venta FROM venta WHERE id_venta = p_id_venta;
    IF v_estado_venta IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La venta indicada no existe.';
    END IF;
    IF v_estado_venta <> 'PENDIENTE' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se pueden agregar entradas a una venta PENDIENTE.';
    END IF;

    SELECT tarifa_base INTO v_tarifa_base FROM funcion WHERE id_funcion = p_id_funcion;
    IF v_tarifa_base IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La funcion indicada no existe.';
    END IF;

    SELECT descuento_porcentaje INTO v_descuento_pct FROM tipoentrada WHERE id_tipoentrada = p_id_tipoentrada;
    IF v_descuento_pct IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El tipo de entrada indicado no existe.';
    END IF;

    SET v_precio_base  = v_tarifa_base;
    SET v_descuento    = ROUND(v_precio_base * v_descuento_pct / 100, 2);
    SET v_precio_final = v_precio_base - v_descuento;

    START TRANSACTION;

    SELECT id_entrada, estado INTO v_id_entrada_existente, v_estado_existente
        FROM entrada
        WHERE id_funcion = p_id_funcion AND id_butaca = p_id_butaca
        FOR UPDATE;

    IF v_id_entrada_existente IS NOT NULL AND v_estado_existente <> 'CANCELADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La butaca ya esta ocupada para esta funcion.';
    END IF;

    IF v_id_entrada_existente IS NULL THEN
        INSERT INTO entrada (precio_base, descuento, precio_final, motivo, id_venta, id_funcion, id_butaca, id_tipoentrada)
        VALUES (v_precio_base, v_descuento, v_precio_final, p_motivo, p_id_venta, p_id_funcion, p_id_butaca, p_id_tipoentrada);

        SET p_id_entrada = LAST_INSERT_ID();
    ELSE
        UPDATE entrada
            SET precio_base    = v_precio_base,
                descuento      = v_descuento,
                precio_final   = v_precio_final,
                motivo         = p_motivo,
                estado         = 'RESERVADA',
                id_venta       = p_id_venta,
                id_tipoentrada = p_id_tipoentrada
            WHERE id_entrada = v_id_entrada_existente;

        SET p_id_entrada = v_id_entrada_existente;
    END IF;

    UPDATE venta
        SET total_pagado = (
            SELECT COALESCE(SUM(precio_final), 0)
                FROM entrada
                WHERE id_venta = p_id_venta AND estado <> 'CANCELADA'
        )
        WHERE id_venta = p_id_venta;

    COMMIT;
END$$

DELIMITER ;


-- =========================================================
-- PROCEDIMIENTO: sp_marcar_entrada_pagada
-- Confirma el pago de una entrada RESERVADA. Al cambiar su
-- estado a PAGADA dispara trg_entrada_au_acumula_puntos, que
-- registra el punto de fidelidad y, si corresponde, marca la
-- venta como COMPLETADA.
-- =========================================================

DELIMITER $$

CREATE PROCEDURE sp_marcar_entrada_pagada(
    IN p_id_entrada INT
)
BEGIN
    DECLARE v_estado VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT estado INTO v_estado FROM entrada WHERE id_entrada = p_id_entrada FOR UPDATE;

    IF v_estado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La entrada indicada no existe.';
    END IF;
    IF v_estado <> 'RESERVADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Solo se puede marcar como pagada una entrada en estado RESERVADA.';
    END IF;

    UPDATE entrada SET estado = 'PAGADA' WHERE id_entrada = p_id_entrada;

    COMMIT;
END$$

DELIMITER ;


-- =========================================================
-- PROCEDIMIENTO: sp_cancelar_entrada
-- Cancela una entrada (libera la butaca para esa funcion) y
-- recalcula total_pagado de la venta excluyendo entradas
-- canceladas. Dispara trg_entrada_au_acumula_puntos, que
-- puede marcar la venta completa como CANCELADA si todas sus
-- entradas terminan canceladas.
-- =========================================================

DELIMITER $$

CREATE PROCEDURE sp_cancelar_entrada(
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
    IF v_estado = 'UTILIZADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede cancelar una entrada ya utilizada.';
    END IF;
    IF v_estado = 'CANCELADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La entrada ya estaba cancelada.';
    END IF;

    UPDATE entrada SET estado = 'CANCELADA' WHERE id_entrada = p_id_entrada;

    UPDATE venta
        SET total_pagado = (
            SELECT COALESCE(SUM(precio_final), 0)
                FROM entrada
                WHERE id_venta = v_id_venta AND estado <> 'CANCELADA'
        )
        WHERE id_venta = v_id_venta;

    COMMIT;
END$$

DELIMITER ;


-- =========================================================
-- PROCEDIMIENTO: sp_canjear_puntos
-- Implementa el canje de puntos de fidelidad (BR-29/BR-31):
-- si el cliente tiene al menos 9 puntos disponibles
-- (fn_puntos_disponibles), genera una venta y una entrada
-- gratuita (precio_final = 0) y registra el movimiento CANJE
-- en historial_puntos. Toda la operacion es una transaccion.
-- =========================================================

DELIMITER $$

CREATE PROCEDURE sp_canjear_puntos(
    IN  p_id_cliente     INT,
    IN  p_id_empleado    INT,
    IN  p_canal_venta    VARCHAR(20),
    IN  p_id_metodopago  INT,
    IN  p_id_funcion     INT,
    IN  p_id_butaca      INT,
    IN  p_id_tipoentrada INT,
    OUT p_id_venta       INT,
    OUT p_id_entrada     INT
)
BEGIN
    DECLARE v_puntos_requeridos INT DEFAULT 9;
    DECLARE v_tarifa_base DECIMAL(10,2);
    DECLARE v_id_entrada_existente INT DEFAULT NULL;
    DECLARE v_estado_existente VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_canal_venta = 'TAQUILLA' AND p_id_empleado IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Las ventas por TAQUILLA requieren un empleado.';
    END IF;

    IF fn_puntos_disponibles(p_id_cliente) < v_puntos_requeridos THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El cliente no tiene puntos suficientes para canjear una entrada gratuita.';
    END IF;

    SELECT tarifa_base INTO v_tarifa_base FROM funcion WHERE id_funcion = p_id_funcion;
    IF v_tarifa_base IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La funcion indicada no existe.';
    END IF;

    START TRANSACTION;

    INSERT INTO venta (canal_venta, observacion, id_cliente, id_empleado, id_metodopago, total_pagado)
    VALUES (p_canal_venta, 'Canje de puntos de fidelidad', p_id_cliente, p_id_empleado, p_id_metodopago, 0);

    SET p_id_venta = LAST_INSERT_ID();

    SELECT id_entrada, estado INTO v_id_entrada_existente, v_estado_existente
        FROM entrada
        WHERE id_funcion = p_id_funcion AND id_butaca = p_id_butaca
        FOR UPDATE;

    IF v_id_entrada_existente IS NOT NULL AND v_estado_existente <> 'CANCELADA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'La butaca ya esta ocupada para esta funcion.';
    END IF;

    IF v_id_entrada_existente IS NULL THEN
        INSERT INTO entrada (precio_base, descuento, precio_final, motivo, estado, id_venta, id_funcion, id_butaca, id_tipoentrada)
        VALUES (v_tarifa_base, v_tarifa_base, 0, 'Entrada gratuita por canje de puntos de fidelidad', 'PAGADA',
                p_id_venta, p_id_funcion, p_id_butaca, p_id_tipoentrada);

        SET p_id_entrada = LAST_INSERT_ID();
    ELSE
        UPDATE entrada
            SET precio_base    = v_tarifa_base,
                descuento      = v_tarifa_base,
                precio_final   = 0,
                motivo         = 'Entrada gratuita por canje de puntos de fidelidad',
                estado         = 'PAGADA',
                id_venta       = p_id_venta,
                id_tipoentrada = p_id_tipoentrada
            WHERE id_entrada = v_id_entrada_existente;

        SET p_id_entrada = v_id_entrada_existente;
    END IF;

    UPDATE venta SET estado = 'COMPLETADA' WHERE id_venta = p_id_venta;

    INSERT INTO historial_puntos (tipo_movimiento, cantidad_puntos, descripcion, id_cliente, id_venta)
    VALUES ('CANJE', v_puntos_requeridos, 'Canje de puntos por entrada gratuita', p_id_cliente, p_id_venta);

    COMMIT;
END$$

DELIMITER ;

-- ============================================================
-- MIBANCO PERÚ — SCRIPT COMPLETO DE BASE DE DATOS
-- Sistema Core Bancario — MySQL 8.0
-- Proyecto Universitario — SIN ENCRIPTACIÓN (modo prueba)
-- ============================================================

DROP DATABASE IF EXISTS banco;
CREATE DATABASE banco
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE banco;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. SUCURSAL
-- ============================================================
CREATE TABLE sucursal (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    ciudad      VARCHAR(100) NOT NULL,
    direccion   VARCHAR(300),
    telefono    VARCHAR(15),
    horario     VARCHAR(100),
    activa      BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_ciudad_sucursal CHECK (ciudad IN ('Lima','Huancayo','Arequipa','Trujillo','Cusco'))
) COMMENT 'Sucursales del banco';

-- ============================================================
-- 2. CLIENTE
-- ============================================================
CREATE TABLE cliente (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_persona        ENUM('NATURAL','EMPRESA') NOT NULL,
    dni                 CHAR(8)         UNIQUE,
    ruc                 CHAR(11)        UNIQUE,
    nombre_completo     VARCHAR(200),
    razon_social        VARCHAR(200),
    telefono            VARCHAR(15),
    email               VARCHAR(100),
    direccion           VARCHAR(300),
    ciudad              VARCHAR(100)    DEFAULT 'Lima',
    fecha_nacimiento    DATE,
    fecha_constitucion  DATE,
    estado              ENUM('ACTIVO','INACTIVO','BLOQUEADO') NOT NULL DEFAULT 'ACTIVO',
    fecha_registro      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dni_length      CHECK (dni IS NULL OR CHAR_LENGTH(dni) = 8),
    CONSTRAINT chk_ruc_length      CHECK (ruc IS NULL OR CHAR_LENGTH(ruc) = 11),
    CONSTRAINT chk_natural_dni     CHECK (tipo_persona = 'EMPRESA' OR dni IS NOT NULL),
    CONSTRAINT chk_empresa_ruc     CHECK (tipo_persona = 'NATURAL' OR ruc IS NOT NULL)
) COMMENT 'Clientes: personas naturales y empresas (MYPE)';

-- ============================================================
-- 3. ROL
-- ============================================================
CREATE TABLE rol (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200)
) COMMENT 'Roles del sistema: ADMIN, CAJERO, CLIENTE, ANALISTA';

-- ============================================================
-- 4. PERMISO
-- ============================================================
CREATE TABLE permiso (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(300)
) COMMENT 'Permisos granulares del sistema';

-- ============================================================
-- 5. ROL_PERMISO (N:M)
-- ============================================================
CREATE TABLE rol_permiso (
    rol_id      BIGINT NOT NULL,
    permiso_id  BIGINT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id)     REFERENCES rol(id)     ON DELETE CASCADE,
    FOREIGN KEY (permiso_id) REFERENCES permiso(id) ON DELETE CASCADE
) COMMENT 'Relación N:M entre roles y permisos';

-- ============================================================
-- 6. USUARIO
-- Nota: password_hash almacena texto plano en modo prueba
-- ============================================================
CREATE TABLE usuario (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL COMMENT 'Texto plano en modo prueba',
    cliente_id      BIGINT,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acceso   DATETIME,
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE SET NULL
) COMMENT 'Usuarios del sistema bancario';

-- ============================================================
-- 7. USUARIO_ROL (N:M)
-- ============================================================
CREATE TABLE usuario_rol (
    usuario_id  BIGINT NOT NULL,
    rol_id      BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (rol_id)     REFERENCES rol(id)     ON DELETE CASCADE
) COMMENT 'Relación N:M entre usuarios y roles';

-- ============================================================
-- 8. EMPLEADO
-- ============================================================
CREATE TABLE empleado (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  BIGINT UNIQUE,
    sucursal_id BIGINT,
    nombre      VARCHAR(200) NOT NULL,
    cargo       VARCHAR(100),
    dni         CHAR(8) NOT NULL UNIQUE,
    telefono    VARCHAR(15),
    email       VARCHAR(100),
    activo      BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_ingreso DATE,
    FOREIGN KEY (usuario_id)  REFERENCES usuario(id)  ON DELETE SET NULL,
    FOREIGN KEY (sucursal_id) REFERENCES sucursal(id) ON DELETE SET NULL
) COMMENT 'Empleados bancarios';

-- ============================================================
-- 9. CUENTA
-- ============================================================
CREATE TABLE cuenta (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_cuenta   VARCHAR(20) NOT NULL UNIQUE,
    producto        VARCHAR(100) NOT NULL,
    tipo            VARCHAR(50) NOT NULL,
    moneda          ENUM('PEN','USD') NOT NULL DEFAULT 'PEN',
    saldo           DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    interes_mensual DECIMAL(5,4),
    cliente_id      BIGINT NOT NULL,
    sucursal_id     BIGINT,
    fecha_apertura  DATE NOT NULL DEFAULT (CURRENT_DATE),
    estado          ENUM('ACTIVA','BLOQUEADA','CANCELADA') NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT chk_saldo_positivo CHECK (saldo >= 0),
    FOREIGN KEY (cliente_id)  REFERENCES cliente(id)  ON DELETE RESTRICT,
    FOREIGN KEY (sucursal_id) REFERENCES sucursal(id) ON DELETE SET NULL
) COMMENT 'Cuentas bancarias de los clientes';

-- ============================================================
-- 10. TARJETA
-- ============================================================
CREATE TABLE tarjeta (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_enmascarado  VARCHAR(19) NOT NULL,
    ultimo4             CHAR(4) NOT NULL,
    tipo                ENUM('DEBITO','CREDITO') NOT NULL,
    cuenta_id           BIGINT NOT NULL,
    cliente_id          BIGINT NOT NULL,
    fecha_vencimiento   DATE NOT NULL,
    estado              ENUM('ACTIVA','BLOQUEADA','CANCELADA') NOT NULL DEFAULT 'ACTIVA',
    FOREIGN KEY (cuenta_id)  REFERENCES cuenta(id)  ON DELETE CASCADE,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE
) COMMENT 'Tarjetas débito/crédito';

-- ============================================================
-- 11. TRANSACCION
-- ============================================================
CREATE TABLE transaccion (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo                ENUM('DEPOSITO','RETIRO','TRANSFERENCIA','PAGO_PRESTAMO',
                             'PAGO_SERVICIO','YAPA','CAMBIO_DIVISA') NOT NULL,
    monto               DECIMAL(15,2) NOT NULL,
    cuenta_origen_id    BIGINT,
    cuenta_destino_id   BIGINT,
    fecha               DATE NOT NULL DEFAULT (CURRENT_DATE),
    hora                TIME NOT NULL DEFAULT (CURRENT_TIME),
    sucursal_id         BIGINT,
    empleado_id         BIGINT,
    canal               ENUM('AGENCIA','APP','WEB','WHATSAPP','CAJERO') NOT NULL,
    descripcion         VARCHAR(300),
    referencia          VARCHAR(50) UNIQUE,
    CONSTRAINT chk_monto_positivo CHECK (monto > 0),
    FOREIGN KEY (cuenta_origen_id)  REFERENCES cuenta(id) ON DELETE SET NULL,
    FOREIGN KEY (cuenta_destino_id) REFERENCES cuenta(id) ON DELETE SET NULL,
    FOREIGN KEY (sucursal_id)       REFERENCES sucursal(id) ON DELETE SET NULL,
    FOREIGN KEY (empleado_id)       REFERENCES empleado(id) ON DELETE SET NULL
) COMMENT 'Todas las transacciones del banco';

-- ============================================================
-- 12. PRODUCTO_PRESTAMO
-- ============================================================
CREATE TABLE producto_prestamo (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL UNIQUE,
    tcea_maxima     DECIMAL(6,4) NOT NULL,
    plazo_minimo    INT NOT NULL,
    plazo_maximo    INT NOT NULL,
    gracia_maxima   INT NOT NULL DEFAULT 3,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_tcea        CHECK (tcea_maxima > 0 AND tcea_maxima <= 1),
    CONSTRAINT chk_plazos      CHECK (plazo_minimo > 0 AND plazo_maximo >= plazo_minimo),
    CONSTRAINT chk_gracia      CHECK (gracia_maxima >= 0 AND gracia_maxima <= 3)
) COMMENT 'Catálogo de productos crediticios';

-- ============================================================
-- 13. PRESTAMO
-- ============================================================
CREATE TABLE prestamo (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    monto_total             DECIMAL(15,2) NOT NULL,
    tasa_interes_mensual    DECIMAL(6,4)  NOT NULL,
    plazo_meses             INT NOT NULL,
    cuota_mensual           DECIMAL(15,2),
    cliente_id              BIGINT NOT NULL,
    sucursal_id             BIGINT,
    producto_id             BIGINT NOT NULL,
    fecha_solicitud         DATE NOT NULL DEFAULT (CURRENT_DATE),
    fecha_aprobacion        DATE,
    fecha_desembolso        DATE,
    periodo_gracia_meses    INT NOT NULL DEFAULT 0,
    estado                  ENUM('PENDIENTE','APROBADO','DESEMBOLSADO','MOROSO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
    tcea                    DECIMAL(6,4),
    CONSTRAINT chk_monto_prestamo  CHECK (monto_total > 0),
    CONSTRAINT chk_tasa            CHECK (tasa_interes_mensual > 0),
    CONSTRAINT chk_gracia_prestamo CHECK (periodo_gracia_meses >= 0 AND periodo_gracia_meses <= 3),
    FOREIGN KEY (cliente_id)  REFERENCES cliente(id)           ON DELETE RESTRICT,
    FOREIGN KEY (sucursal_id) REFERENCES sucursal(id)          ON DELETE SET NULL,
    FOREIGN KEY (producto_id) REFERENCES producto_prestamo(id) ON DELETE RESTRICT
) COMMENT 'Préstamos bancarios de clientes';

-- ============================================================
-- 14. CUOTA_PRESTAMO
-- ============================================================
CREATE TABLE cuota_prestamo (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    prestamo_id             BIGINT NOT NULL,
    numero_cuota            INT NOT NULL,
    fecha_pago_programada   DATE NOT NULL,
    capital                 DECIMAL(15,2) NOT NULL,
    interes                 DECIMAL(15,2) NOT NULL,
    seguro                  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    mora                    DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    monto_total             DECIMAL(15,2) NOT NULL,
    saldo_capital           DECIMAL(15,2) NOT NULL,
    estado                  ENUM('PENDIENTE','PAGADA','MOROSA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago_real         DATE,
    CONSTRAINT chk_numero_cuota CHECK (numero_cuota > 0),
    UNIQUE KEY uk_prestamo_cuota (prestamo_id, numero_cuota),
    FOREIGN KEY (prestamo_id) REFERENCES prestamo(id) ON DELETE CASCADE
) COMMENT 'Cronograma de pagos — sistema francés';

-- ============================================================
-- 15. HISTORIAL_PAGO
-- ============================================================
CREATE TABLE historial_pago (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuota_id        BIGINT NOT NULL,
    fecha_pago      DATE NOT NULL DEFAULT (CURRENT_DATE),
    monto_pagado    DECIMAL(15,2) NOT NULL,
    canal           VARCHAR(30),
    empleado_id     BIGINT,
    referencia      VARCHAR(50),
    FOREIGN KEY (cuota_id)    REFERENCES cuota_prestamo(id) ON DELETE CASCADE,
    FOREIGN KEY (empleado_id) REFERENCES empleado(id)       ON DELETE SET NULL
) COMMENT 'Historial de pagos realizados a cuotas';

-- ============================================================
-- 16. SEGURO
-- ============================================================
CREATE TABLE seguro (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id      BIGINT NOT NULL,
    prestamo_id     BIGINT,
    tipo            ENUM('VIDA','DESGRAVAMEN','MULTIRRIESGO','HOGAR','ACCIDENTES') NOT NULL,
    prima_mensual   DECIMAL(10,2),
    fecha_inicio    DATE NOT NULL,
    fecha_fin       DATE,
    estado          ENUM('ACTIVO','VENCIDO','CANCELADO') NOT NULL DEFAULT 'ACTIVO',
    FOREIGN KEY (cliente_id)  REFERENCES cliente(id)  ON DELETE CASCADE,
    FOREIGN KEY (prestamo_id) REFERENCES prestamo(id) ON DELETE SET NULL
) COMMENT 'Seguros asociados a clientes y préstamos';

-- ============================================================
-- 17. SERVICIO_PAGO
-- ============================================================
CREATE TABLE servicio_pago (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id          BIGINT NOT NULL,
    tipo_servicio       ENUM('LUZ','AGUA','TELEFONIA','CELULAR','INTERNET','EDUCACION') NOT NULL,
    empresa             VARCHAR(100),
    numero_suministro   VARCHAR(50),
    monto               DECIMAL(10,2) NOT NULL,
    fecha_pago          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    referencia          VARCHAR(50),
    CONSTRAINT chk_monto_servicio CHECK (monto > 0),
    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE
) COMMENT 'Pagos de servicios';

-- ============================================================
-- 18. TRANSFERENCIA_YAPA
-- ============================================================
CREATE TABLE transferencia_yapa (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    remitente_cliente_id    BIGINT NOT NULL,
    cliente_destino_id      BIGINT,
    numero_destino          VARCHAR(15) NOT NULL,
    monto                   DECIMAL(10,2) NOT NULL,
    fecha                   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado                  ENUM('PENDIENTE','COMPLETADO','RECHAZADO') NOT NULL DEFAULT 'COMPLETADO',
    referencia              VARCHAR(50) UNIQUE,
    CONSTRAINT chk_monto_yapa CHECK (monto > 0 AND monto <= 500),
    FOREIGN KEY (remitente_cliente_id) REFERENCES cliente(id) ON DELETE CASCADE,
    FOREIGN KEY (cliente_destino_id)   REFERENCES cliente(id) ON DELETE SET NULL
) COMMENT 'Transferencias Yapa';

-- ============================================================
-- 19. DEPOSITO_PLAZO
-- ============================================================
CREATE TABLE deposito_plazo (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id              BIGINT NOT NULL,
    monto                   DECIMAL(15,2) NOT NULL,
    tasa_interes_mensual    DECIMAL(6,4)  NOT NULL,
    plazo_meses             INT NOT NULL,
    fecha_inicio            DATE NOT NULL,
    fecha_fin               DATE NOT NULL,
    saldo_proyectado        DECIMAL(15,2),
    estado                  ENUM('ACTIVO','VENCIDO','CANCELADO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT chk_monto_deposito CHECK (monto > 0),
    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE
) COMMENT 'Depósitos a plazo fijo';

-- ============================================================
-- 20. BENEFICIARIO
-- ============================================================
CREATE TABLE beneficiario (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id      BIGINT NOT NULL,
    nombre          VARCHAR(200) NOT NULL,
    banco           VARCHAR(100) NOT NULL,
    numero_cuenta   VARCHAR(30) NOT NULL,
    alias           VARCHAR(100),
    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE CASCADE
) COMMENT 'Beneficiarios frecuentes para transferencias';

-- ============================================================
-- 21. MORA
-- ============================================================
CREATE TABLE mora (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuota_id            BIGINT NOT NULL,
    dias_atraso         INT NOT NULL,
    interes_moratorio   DECIMAL(10,2) NOT NULL,
    fecha_calculo       DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT chk_dias_atraso CHECK (dias_atraso > 0),
    FOREIGN KEY (cuota_id) REFERENCES cuota_prestamo(id) ON DELETE CASCADE
) COMMENT 'Registro de moras por atraso en cuotas';

-- ============================================================
-- 22. AUDITORIA
-- ============================================================
CREATE TABLE auditoria (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id      BIGINT,
    tabla_afectada  VARCHAR(100) NOT NULL,
    accion          ENUM('INSERT','UPDATE','DELETE') NOT NULL,
    registro_id     BIGINT,
    datos_antes     JSON,
    datos_despues   JSON,
    fecha           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_origen       VARCHAR(45),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE SET NULL
) COMMENT 'Log de auditoría';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- ÍNDICES
-- ============================================================
CREATE INDEX idx_cliente_dni         ON cliente(dni);
CREATE INDEX idx_cliente_ruc         ON cliente(ruc);
CREATE INDEX idx_cliente_estado      ON cliente(estado);
CREATE INDEX idx_cuenta_cliente      ON cuenta(cliente_id);
CREATE INDEX idx_cuenta_numero       ON cuenta(numero_cuenta);
CREATE INDEX idx_cuenta_estado       ON cuenta(estado);
CREATE INDEX idx_transaccion_origen  ON transaccion(cuenta_origen_id, fecha);
CREATE INDEX idx_transaccion_destino ON transaccion(cuenta_destino_id, fecha);
CREATE INDEX idx_transaccion_fecha   ON transaccion(fecha);
CREATE INDEX idx_prestamo_cliente    ON prestamo(cliente_id);
CREATE INDEX idx_prestamo_estado     ON prestamo(estado);
CREATE INDEX idx_cuota_prestamo_est  ON cuota_prestamo(prestamo_id, estado);
CREATE INDEX idx_cuota_fecha         ON cuota_prestamo(fecha_pago_programada);
CREATE INDEX idx_tarjeta_cliente     ON tarjeta(cliente_id);
CREATE INDEX idx_auditoria_fecha     ON auditoria(fecha);
CREATE INDEX idx_yapa_remitente      ON transferencia_yapa(remitente_cliente_id);

-- ============================================================
-- VIEWS
-- ============================================================

CREATE VIEW saldo_clientes AS
SELECT
    c.id               AS cliente_id,
    COALESCE(c.nombre_completo, c.razon_social) AS nombre,
    c.tipo_persona,
    cu.numero_cuenta,
    cu.producto,
    cu.moneda,
    cu.saldo,
    cu.estado          AS estado_cuenta
FROM cliente c
JOIN cuenta cu ON cu.cliente_id = c.id
WHERE cu.estado = 'ACTIVA'
ORDER BY c.id, cu.moneda;

CREATE VIEW prestamos_activos AS
SELECT
    p.id               AS prestamo_id,
    p.cliente_id,
    COALESCE(cl.nombre_completo, cl.razon_social) AS cliente,
    pp.nombre          AS producto,
    p.monto_total,
    p.cuota_mensual,
    p.plazo_meses,
    p.tasa_interes_mensual,
    p.tcea,
    p.fecha_desembolso,
    p.estado,
    s.nombre           AS sucursal
FROM prestamo p
JOIN cliente cl           ON cl.id = p.cliente_id
JOIN producto_prestamo pp ON pp.id = p.producto_id
LEFT JOIN sucursal s      ON s.id = p.sucursal_id
WHERE p.estado IN ('APROBADO','DESEMBOLSADO','MOROSO');

CREATE VIEW morosos AS
SELECT DISTINCT
    COALESCE(cl.nombre_completo, cl.razon_social) AS cliente,
    cl.dni, cl.ruc, cl.telefono,
    p.id               AS prestamo_id,
    pp.nombre          AS producto,
    COUNT(cp.id)       AS cuotas_vencidas,
    SUM(cp.monto_total + cp.mora) AS deuda_total
FROM cliente cl
JOIN prestamo p           ON p.cliente_id = cl.id
JOIN producto_prestamo pp ON pp.id = p.producto_id
JOIN cuota_prestamo cp    ON cp.prestamo_id = p.id
WHERE cp.estado = 'MOROSA'
  AND cp.fecha_pago_programada < CURRENT_DATE
GROUP BY cl.id, p.id, pp.nombre;

CREATE VIEW transacciones_mensuales AS
SELECT
    YEAR(t.fecha)  AS anio,
    MONTH(t.fecha) AS mes,
    t.tipo,
    t.canal,
    COUNT(*)       AS cantidad,
    SUM(t.monto)   AS monto_total
FROM transaccion t
GROUP BY YEAR(t.fecha), MONTH(t.fecha), t.tipo, t.canal
ORDER BY anio DESC, mes DESC;

CREATE VIEW simulador_cuota AS
SELECT
    pp.nombre                  AS producto,
    pp.tcea_maxima * 100       AS tcea_maxima_pct,
    pp.plazo_minimo,
    pp.plazo_maximo,
    pp.gracia_maxima,
    ROUND(10000 * (pp.tcea_maxima/12) /
          (1 - POW(1 + (pp.tcea_maxima/12), -12)), 2) AS cuota_ejemplo_10k_12m
FROM producto_prestamo pp
WHERE pp.activo = TRUE;

-- ============================================================
-- STORED PROCEDURES
-- ============================================================

DELIMITER $$

CREATE PROCEDURE generar_cronograma_frances(
    IN p_prestamo_id        BIGINT,
    IN p_monto              DECIMAL(15,2),
    IN p_tasa_mensual       DECIMAL(6,4),
    IN p_plazo_meses        INT,
    IN p_gracia_meses       INT,
    IN p_seguro_mensual     DECIMAL(10,2),
    IN p_fecha_inicio       DATE
)
BEGIN
    DECLARE v_cuota         DECIMAL(15,2);
    DECLARE v_saldo         DECIMAL(15,2);
    DECLARE v_interes       DECIMAL(15,2);
    DECLARE v_capital       DECIMAL(15,2);
    DECLARE v_n_efectivo    INT;
    DECLARE v_i             INT DEFAULT 1;
    DECLARE v_fecha_cuota   DATE;

    SET v_saldo = p_monto;
    SET v_n_efectivo = p_plazo_meses - p_gracia_meses;

    SET v_cuota = ROUND(
        p_monto * p_tasa_mensual /
        (1 - POW(1 + p_tasa_mensual, -v_n_efectivo)),
        2
    );

    UPDATE prestamo SET cuota_mensual = v_cuota + p_seguro_mensual
    WHERE id = p_prestamo_id;

    DELETE FROM cuota_prestamo WHERE prestamo_id = p_prestamo_id;

    WHILE v_i <= p_plazo_meses DO
        SET v_fecha_cuota = DATE_ADD(p_fecha_inicio, INTERVAL v_i MONTH);

        IF v_i <= p_gracia_meses THEN
            SET v_interes = ROUND(v_saldo * p_tasa_mensual, 2);
            SET v_capital = 0;
            INSERT INTO cuota_prestamo
                (prestamo_id, numero_cuota, fecha_pago_programada, capital, interes, seguro, monto_total, saldo_capital, estado)
            VALUES
                (p_prestamo_id, v_i, v_fecha_cuota, v_capital, v_interes, p_seguro_mensual, v_interes + p_seguro_mensual, v_saldo, 'PENDIENTE');
        ELSE
            SET v_interes = ROUND(v_saldo * p_tasa_mensual, 2);
            SET v_capital = ROUND(v_cuota - v_interes, 2);
            IF v_i = p_plazo_meses THEN
                SET v_capital = v_saldo;
                SET v_interes = ROUND(v_saldo * p_tasa_mensual, 2);
            END IF;
            SET v_saldo = GREATEST(0, ROUND(v_saldo - v_capital, 2));
            INSERT INTO cuota_prestamo
                (prestamo_id, numero_cuota, fecha_pago_programada, capital, interes, seguro, monto_total, saldo_capital, estado)
            VALUES
                (p_prestamo_id, v_i, v_fecha_cuota, v_capital, v_interes, p_seguro_mensual, v_capital + v_interes + p_seguro_mensual, v_saldo, 'PENDIENTE');
        END IF;

        SET v_i = v_i + 1;
    END WHILE;

    SELECT CONCAT('Cronograma generado: ', p_plazo_meses, ' cuotas. Cuota: S/ ', v_cuota + p_seguro_mensual) AS resultado;
END$$

CREATE PROCEDURE desembolsar_prestamo(
    IN p_prestamo_id    BIGINT,
    IN p_cuenta_id      BIGINT,
    IN p_empleado_id    BIGINT
)
BEGIN
    DECLARE v_monto         DECIMAL(15,2);
    DECLARE v_estado        VARCHAR(20);
    DECLARE v_referencia    VARCHAR(50);

    SELECT monto_total, estado INTO v_monto, v_estado
    FROM prestamo WHERE id = p_prestamo_id;

    IF v_estado != 'APROBADO' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'El préstamo debe estar APROBADO para desembolsar';
    END IF;

    SET v_referencia = CONCAT('DSB-', YEAR(NOW()), '-', LPAD(p_prestamo_id, 6, '0'));

    UPDATE cuenta SET saldo = saldo + v_monto WHERE id = p_cuenta_id;

    INSERT INTO transaccion (tipo, monto, cuenta_destino_id, canal, descripcion, referencia, empleado_id)
    VALUES ('DEPOSITO', v_monto, p_cuenta_id, 'AGENCIA', CONCAT('Desembolso préstamo #', p_prestamo_id), v_referencia, p_empleado_id);

    UPDATE prestamo SET estado = 'DESEMBOLSADO', fecha_desembolso = CURRENT_DATE
    WHERE id = p_prestamo_id;

    SELECT CONCAT('Desembolso exitoso. Ref: ', v_referencia, ' — Monto: S/ ', v_monto) AS resultado;
END$$

CREATE PROCEDURE calcular_mora()
BEGIN
    DECLARE v_tasa_mora DECIMAL(6,4) DEFAULT 0.0010;

    UPDATE cuota_prestamo SET estado = 'MOROSA'
    WHERE estado = 'PENDIENTE' AND fecha_pago_programada < CURRENT_DATE;

    INSERT INTO mora (cuota_id, dias_atraso, interes_moratorio, fecha_calculo)
    SELECT cp.id,
           DATEDIFF(CURRENT_DATE, cp.fecha_pago_programada),
           ROUND(cp.monto_total * v_tasa_mora * DATEDIFF(CURRENT_DATE, cp.fecha_pago_programada), 2),
           CURRENT_DATE
    FROM cuota_prestamo cp
    WHERE cp.estado = 'MOROSA'
      AND cp.id NOT IN (SELECT cuota_id FROM mora WHERE fecha_calculo = CURRENT_DATE);

    UPDATE prestamo p SET p.estado = 'MOROSO'
    WHERE EXISTS (SELECT 1 FROM cuota_prestamo cp WHERE cp.prestamo_id = p.id AND cp.estado = 'MOROSA')
      AND p.estado = 'DESEMBOLSADO';

    SELECT CONCAT('Proceso de mora completado: ', CURRENT_TIMESTAMP) AS resultado;
END$$

DELIMITER ;

-- ============================================================
-- TRIGGERS
-- ============================================================

DELIMITER $$

CREATE TRIGGER trg_validar_saldo_debito
BEFORE INSERT ON transaccion
FOR EACH ROW
BEGIN
    DECLARE v_saldo DECIMAL(15,2);
    IF NEW.tipo IN ('RETIRO','TRANSFERENCIA','PAGO_PRESTAMO','PAGO_SERVICIO','YAPA','CAMBIO_DIVISA')
       AND NEW.cuenta_origen_id IS NOT NULL THEN
        SELECT saldo INTO v_saldo FROM cuenta WHERE id = NEW.cuenta_origen_id;
        IF v_saldo < NEW.monto THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Saldo insuficiente para realizar la operación';
        END IF;
    END IF;
END$$

CREATE TRIGGER trg_actualizar_saldo
AFTER INSERT ON transaccion
FOR EACH ROW
BEGIN
    IF NEW.cuenta_origen_id IS NOT NULL THEN
        UPDATE cuenta SET saldo = saldo - NEW.monto WHERE id = NEW.cuenta_origen_id;
    END IF;
    IF NEW.cuenta_destino_id IS NOT NULL THEN
        UPDATE cuenta SET saldo = saldo + NEW.monto WHERE id = NEW.cuenta_destino_id;
    END IF;
END$$

CREATE TRIGGER trg_historial_pago
AFTER UPDATE ON cuota_prestamo
FOR EACH ROW
BEGIN
    IF NEW.estado = 'PAGADA' AND OLD.estado != 'PAGADA' THEN
        INSERT INTO historial_pago (cuota_id, fecha_pago, monto_pagado, canal, referencia)
        VALUES (NEW.id, COALESCE(NEW.fecha_pago_real, CURRENT_DATE), NEW.monto_total, 'APP',
                CONCAT('PAG-', YEAR(NOW()), '-', LPAD(NEW.id, 6, '0')));
    END IF;
END$$

CREATE TRIGGER trg_auditoria_cuenta
AFTER UPDATE ON cuenta
FOR EACH ROW
BEGIN
    INSERT INTO auditoria (tabla_afectada, accion, registro_id, datos_antes, datos_despues)
    VALUES ('cuenta', 'UPDATE', NEW.id,
            JSON_OBJECT('saldo', OLD.saldo, 'estado', OLD.estado),
            JSON_OBJECT('saldo', NEW.saldo, 'estado', NEW.estado));
END$$

CREATE TRIGGER trg_auditoria_prestamo
AFTER UPDATE ON prestamo
FOR EACH ROW
BEGIN
    INSERT INTO auditoria (tabla_afectada, accion, registro_id, datos_antes, datos_despues)
    VALUES ('prestamo', 'UPDATE', NEW.id,
            JSON_OBJECT('estado', OLD.estado),
            JSON_OBJECT('estado', NEW.estado));
END$$

DELIMITER ;

-- ============================================================
-- DATOS DE EJEMPLO
-- ============================================================

-- Sucursales
INSERT INTO sucursal (nombre, ciudad, direccion, telefono, horario) VALUES
('Mibanco Huancayo Centro',      'Huancayo', 'Jr. Real 567, El Tambo',                 '064-123456', 'Lun-Vie 9:00-18:00, Sáb 9:00-13:00'),
('Mibanco Lima San Isidro',      'Lima',     'Av. Javier Prado Este 1230, San Isidro', '01-4567890',  'Lun-Vie 9:00-18:00'),
('Mibanco Arequipa Selva Alegre','Arequipa', 'Av. Independencia 1200',                 '054-234567', 'Lun-Vie 9:00-18:00'),
('Mibanco Trujillo El Porvenir', 'Trujillo', 'Jr. Húsares de Junín 890',               '044-345678', 'Lun-Vie 9:00-18:00'),
('Mibanco Cusco Wanchaq',        'Cusco',    'Av. El Sol 220, Wanchaq',                '084-456789', 'Lun-Vie 9:00-18:00');

-- Clientes personas naturales
INSERT INTO cliente (tipo_persona, dni, nombre_completo, telefono, email, direccion, ciudad, fecha_nacimiento) VALUES
('NATURAL', '47823156', 'Juan Carlos Quispe Mamani',   '987654321', 'jquispe@email.com',  'Jr. Loreto 234, El Tambo',  'Huancayo', '1985-03-15'),
('NATURAL', '72341890', 'María Elena López Flores',    '945112337', 'mlopez@email.com',   'Av. Ferrocarril 567',       'Huancayo', '1992-07-22'),
('NATURAL', '61234567', 'Pedro Antonio Ramos Chávez',  '912445887', 'pramos@email.com',   'Jr. Puno 123, Breña',       'Lima',     '1978-11-30'),
('NATURAL', '53218974', 'Ana Lucía García Torres',     '966778432', 'agarcia@email.com',  'Calle Los Álamos 456',      'Arequipa', '1995-04-18'),
('NATURAL', '41567832', 'Luis Alberto Torres Mendoza', '931224567', 'ltorres@email.com',  'Av. Los Incas 890',         'Cusco',    '1980-09-05'),
('NATURAL', '69801234', 'Rosa Amelia Huanca Paucar',   '978334521', 'rhuanca@email.com',  'Jr. Cajamarca 678, Chilca', 'Huancayo', '1988-12-14'),
('NATURAL', '38120456', 'Carlos Eduardo Mendez Vega',  '956234781', 'cmendez@email.com',  'Av. Grau 1234, La Perla',   'Lima',     '1975-06-28');

-- Clientes amigos del equipo (IDs: 11, 12, 13, 14)
INSERT INTO cliente (tipo_persona, dni, nombre_completo, telefono, email, direccion, ciudad, fecha_nacimiento) VALUES
('NATURAL', '60933373', 'Diego Fredy Cucho Rivera',      '924606154', 'dcucho@email.com',    'Jr. Amazonas 123, El Tambo',  'Huancayo', '2000-05-10'),
('NATURAL', '60847193', 'Kianu William Retamozo Lavado', '918689085', 'kretamozo@email.com', 'Av. Ferrocarril 890',         'Huancayo', '2001-03-22'),
('NATURAL', '75137707', 'Anyelo Jheray Velita Lozano',   '976229873', 'avelita@email.com',   'Jr. Cajamarca 456, Chilca',   'Huancayo', '1999-11-14'),
('NATURAL', '74875059', 'Jhovani Salvador Jumpa Fierro', '968265326', 'jjumpa@email.com',    'Av. Los Andes 789, El Tambo', 'Huancayo', '2000-08-30');

-- Clientes empresa
INSERT INTO cliente (tipo_persona, ruc, razon_social, telefono, email, direccion, ciudad, fecha_constitucion) VALUES
('EMPRESA', '20601234567', 'Abarrotes San Martín S.A.C.',        '064-789012', 'abarrotes.sanmartin@empresa.pe', 'Jr. Ancash 234, El Tambo', 'Huancayo', '2015-08-10'),
('EMPRESA', '20712345678', 'Constructora Andes del Perú S.R.L.', '01-5678901',  'constructora.andes@empresa.pe',  'Av. Colonial 890, Callao', 'Lima',     '2010-03-22'),
('EMPRESA', '20823456789', 'Agropecuaria Valles Verdes E.I.R.L.','054-890123', 'agrovaverdes@empresa.pe',         'Urb. Cerro Colorado 456',  'Arequipa', '2018-06-15');

-- Roles
INSERT INTO rol (nombre, descripcion) VALUES
('ADMIN',    'Administrador del sistema con acceso total'),
('CAJERO',   'Empleado de caja con operaciones básicas'),
('ANALISTA', 'Analista de crédito con acceso a préstamos'),
('CLIENTE',  'Cliente con acceso a banca por internet');

-- Permisos
INSERT INTO permiso (nombre, descripcion) VALUES
('VER_CUENTAS',           'Consultar saldos y cuentas'),
('REALIZAR_TRANSACCIONES','Realizar depósitos, retiros y transferencias'),
('APROBAR_PRESTAMOS',     'Aprobar solicitudes de préstamo'),
('VER_AUDITORIA',         'Consultar logs de auditoría'),
('GESTIONAR_CLIENTES',    'Crear y editar clientes'),
('DESEMBOLSAR',           'Realizar desembolsos de préstamos');

-- Rol-Permisos
INSERT INTO rol_permiso (rol_id, permiso_id) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),
(2,1),(2,2),
(3,1),(3,3),(3,5),(3,6),
(4,1);

-- ============================================================
-- USUARIOS — CONTRASEÑA EN TEXTO PLANO (modo prueba)
-- ============================================================
-- Usuario    | Contraseña | Rol
-- -----------|------------|--------
-- 47823156   | 123456     | CLIENTE (Juan Quispe)       → cliente_id 1
-- 72341890   | 123456     | CLIENTE (María López)       → cliente_id 2
-- 61234567   | 123456     | CLIENTE (Pedro Ramos)       → cliente_id 3
-- 53218974   | 123456     | CLIENTE (Ana García)        → cliente_id 4
-- 41567832   | 123456     | CLIENTE (Luis Torres)       → cliente_id 5
-- 69801234   | 123456     | CLIENTE (Rosa Huanca)       → cliente_id 6
-- 38120456   | 123456     | CLIENTE (Carlos Mendez)     → cliente_id 7
-- 60933373   | 123456     | CLIENTE (Diego Cucho)       → cliente_id 8
-- 60847193   | 123456     | CLIENTE (Kianu Retamozo)    → cliente_id 9
-- 75137707   | 123456     | CLIENTE (Anyelo Velita)     → cliente_id 10
-- 74875059   | 123456     | CLIENTE (Jhovani Jumpa)     → cliente_id 11
-- admin      | 123456     | ADMIN
-- ============================================================
INSERT INTO usuario (username, password_hash, cliente_id) VALUES
('47823156', '123456',  1),
('72341890', '123456',  2),
('61234567', '123456',  3),
('53218974', '123456',  4),
('41567832', '123456',  5),
('69801234', '123456',  6),
('38120456', '123456',  7),
('60933373', '123456',  8),
('60847193', '123456',  9),
('75137707', '123456', 10),
('74875059', '123456', 11),
('admin',    '123456', NULL);

INSERT INTO usuario_rol (usuario_id, rol_id) VALUES
(1,4),(2,4),(3,4),(4,4),(5,4),(6,4),(7,4),(8,4),(9,4),(10,4),(11,4),(12,1);

-- Empleado
INSERT INTO empleado (usuario_id, sucursal_id, nombre, cargo, dni, telefono, fecha_ingreso) VALUES
(12, 1, 'Admin Sistema Mibanco', 'Administrador', '00000001', '064-000000', '2020-01-01');

-- Cuentas
INSERT INTO cuenta (numero_cuenta, producto, tipo, moneda, saldo, interes_mensual, cliente_id, sucursal_id, fecha_apertura) VALUES
('19000012847201', 'Miahorro',    'Ahorro', 'PEN', 4280.50,  0.0050, 1, 1, '2023-03-15'),
('19000056122301', 'Full Ahorro', 'Ahorro', 'USD', 1850.00,  0.0030, 1, 1, '2023-06-20'),
('19000033911504', 'CTS',         'CTS',   'PEN', 2150.00,  0.0250, 1, 1, '2023-05-01'),
('19000072341891', 'Miahorro',    'Ahorro', 'PEN', 8940.00,  0.0050, 2, 1, '2023-01-10'),
('19000061234560', 'Michequera',  'Ahorro', 'PEN', 12500.00, 0.0000, 3, 2, '2022-09-05'),
('19000053218975', 'Ahorro Mujer','Ahorro', 'PEN', 3200.00,  0.0060, 4, 3, '2024-02-14'),
('19000041567830', 'Full Ahorro', 'Ahorro', 'USD', 3450.00,  0.0035, 5, 5, '2023-07-01'),
('19000069801230', 'Miahorro',    'Ahorro', 'PEN', 550.00,   0.0050, 6, 1, '2024-08-20'),
('19000038120456', 'Mirenta',     'Ahorro', 'PEN', 18000.00, 0.0080, 7, 2, '2021-12-01'),
('19000020601230', 'Full Ahorro', 'Ahorro', 'PEN', 45000.00, 0.0050, 12, 1, '2022-04-12'),
('19000060933373', 'Miahorro',    'Ahorro', 'PEN',  2350.00, 0.0050, 8, 1, '2025-01-15'),
('19000060847193', 'Miahorro',    'Ahorro', 'PEN',  1800.00, 0.0050, 9, 1, '2025-02-20'),
('19000075137707', 'Miahorro',    'Ahorro', 'PEN',  3100.00, 0.0050, 10, 1, '2025-03-10'),
('19000074875059', 'Miahorro',    'Ahorro', 'PEN',  4750.00, 0.0050, 11, 1, '2025-04-05');

-- Tarjetas
INSERT INTO tarjeta (numero_enmascarado, ultimo4, tipo, cuenta_id, cliente_id, fecha_vencimiento) VALUES
('**** **** **** 2847', '2847', 'DEBITO', 1, 1, '2028-12-31'),
('**** **** **** 5612', '5612', 'DEBITO', 2, 1, '2027-09-30'),
('**** **** **** 1891', '1891', 'DEBITO', 4, 2, '2028-06-30'),
('**** **** **** 4560', '4560', 'DEBITO', 5, 3, '2027-12-31'),
('**** **** **** 8975', '8975', 'DEBITO', 6, 4, '2029-03-31');

-- Productos de préstamo
INSERT INTO producto_prestamo (nombre, tcea_maxima, plazo_minimo, plazo_maximo, gracia_maxima) VALUES
('Micapital',          0.9084, 3,  36,  3),
('Miequipo',           0.6000, 3,  24,  2),
('Milocal',            0.3500, 12, 120, 3),
('Mivivienda',         0.1500, 12, 240, 3),
('Micasa',             0.2400, 12, 180, 3),
('Mihipoteca',         0.1200, 24, 360, 3),
('Agropecuario Rural', 0.7200, 3,  18,  3),
('Mercado/Galerías',   0.5400, 6,  48,  2),
('Efectivo Altoque',   0.9084, 1,  12,  0);

-- Préstamos
INSERT INTO prestamo (monto_total, tasa_interes_mensual, plazo_meses, cuota_mensual,
    cliente_id, sucursal_id, producto_id, fecha_solicitud, fecha_aprobacion,
    fecha_desembolso, periodo_gracia_meses, estado, tcea) VALUES
(15000.00, 0.0350, 24, 680.00,   1, 1, 1, '2024-10-01', '2024-10-03', '2024-10-05', 0, 'DESEMBOLSADO', 0.5102),
( 3500.00, 0.0280, 12, 328.50,   1, 1, 2, '2025-03-05', '2025-03-07', '2025-03-12', 0, 'DESEMBOLSADO', 0.3960),
(50000.00, 0.0180, 60, 1268.00,  3, 2, 3, '2023-11-01', '2023-11-08', '2023-11-15', 0, 'DESEMBOLSADO', 0.2391),
( 8000.00, 0.0420, 18, 530.00,   2, 1, 1, '2025-09-10', '2025-09-12', '2025-09-15', 1, 'DESEMBOLSADO', 0.6486),
(   800.00, 0.0500, 6, 150.00,   6, 1, 9, '2026-01-10', '2026-01-10', '2026-01-10', 0, 'DESEMBOLSADO', 0.7959),
(25000.00, 0.0150, 36, 764.00,  12, 1, 8, '2026-03-01', '2026-03-05', '2026-03-10', 0, 'DESEMBOLSADO', 0.1956),
( 5000.00, 0.0380, 12, 450.00,   4, 3, 2, '2026-06-01', NULL,          NULL,          0, 'PENDIENTE',   NULL);

-- Cuotas préstamo 1
INSERT INTO cuota_prestamo (prestamo_id, numero_cuota, fecha_pago_programada, capital, interes, seguro, monto_total, saldo_capital, estado, fecha_pago_real) VALUES
(1,  1, '2024-11-05', 395.00, 270.00, 15.00, 680.00, 14605.00, 'PAGADA',   '2024-11-05'),
(1,  2, '2024-12-05', 409.00, 256.00, 15.00, 680.00, 14196.00, 'PAGADA',   '2024-12-04'),
(1,  3, '2025-01-05', 423.00, 242.00, 15.00, 680.00, 13773.00, 'PAGADA',   '2025-01-05'),
(1,  4, '2025-02-05', 438.00, 227.00, 15.00, 680.00, 13335.00, 'PAGADA',   '2025-02-05'),
(1,  5, '2025-03-05', 453.00, 212.00, 15.00, 680.00, 12882.00, 'PAGADA',   '2025-03-05'),
(1,  6, '2025-04-05', 469.00, 196.00, 15.00, 680.00, 12413.00, 'PAGADA',   '2025-04-04'),
(1,  7, '2025-05-05', 485.00, 180.00, 15.00, 680.00, 11928.00, 'PAGADA',   '2025-05-05'),
(1,  8, '2025-06-05', 501.00, 164.00, 15.00, 680.00, 11427.00, 'PAGADA',   '2025-06-05'),
(1,  9, '2026-07-05', 519.00, 146.00, 15.00, 680.00, 10908.00, 'PENDIENTE', NULL),
(1, 10, '2026-08-05', 537.00, 128.00, 15.00, 680.00, 10371.00, 'PENDIENTE', NULL),
(1, 11, '2026-09-05', 556.00, 109.00, 15.00, 680.00,  9815.00, 'PENDIENTE', NULL),
(1, 12, '2026-10-05', 575.00,  90.00, 15.00, 680.00,  9240.00, 'PENDIENTE', NULL);

-- Transacciones
INSERT INTO transaccion (tipo, monto, cuenta_origen_id, cuenta_destino_id, fecha, hora, sucursal_id, empleado_id, canal, descripcion, referencia) VALUES
('DEPOSITO',      1200.00, NULL, 1, '2026-06-11', '09:15:00', 1,    1,    'AGENCIA', 'Depósito en efectivo — Juan Quispe',              'MB-11847'),
('YAPA',            80.00, 1,    4, '2026-06-11', '08:02:00', NULL, NULL, 'APP',     'Yapa a María López — 987-654-321',                'YAP-11023'),
('PAGO_SERVICIO',  145.00, 1,    NULL,'2026-06-10','19:45:00', NULL, NULL, 'WEB',    'Pago Luz del Sur — Sumin: 121345678',             'SVC-10441'),
('TRANSFERENCIA',  500.00, 1,    5, '2026-06-10', '14:30:00', NULL, NULL, 'APP',     'Transferencia interbancaria BCP',                 'TRF-10289'),
('DEPOSITO',      3500.00, NULL, 1, '2026-06-05', '10:00:00', NULL, NULL, 'WEB',     'Abono CCI — Constructora Andes SAC',              'CCI-05712'),
('PAGO_PRESTAMO',  680.00, 1,    NULL,'2026-06-05','09:00:00', 1,    1,   'CAJERO',  'Cuota 8 — Préstamo Micapital MBC-2024-000821',    'PCU-05183'),
('PAGO_SERVICIO',   68.00, 1,    NULL,'2026-06-01','08:30:00', NULL, NULL, 'APP',    'Pago Sedapal — Sumin: 4521890',                   'SVC-01092'),
('DEPOSITO',      8940.00, NULL, 4, '2026-05-15', '11:00:00', 1,    1,   'AGENCIA', 'Apertura cuenta Miahorro — María López',           'APE-05001'),
('YAPA',            50.00, 1,    NULL,'2026-06-09','18:45:00', NULL, NULL, 'APP',    'Yapa a Carlos Ramos — 945-112-337',               'YAP-09345');

-- Seguros
INSERT INTO seguro (cliente_id, prestamo_id, tipo, prima_mensual, fecha_inicio, fecha_fin) VALUES
(1, 1, 'DESGRAVAMEN', 15.00, '2024-10-05', '2026-10-05'),
(1, 2, 'DESGRAVAMEN',  8.00, '2025-03-12', '2026-03-12'),
(3, 3, 'VIDA',         25.00, '2023-11-15', '2028-11-15'),
(2, 4, 'DESGRAVAMEN', 18.00, '2025-09-15', '2027-03-15');

-- Servicios de pago
INSERT INTO servicio_pago (cliente_id, tipo_servicio, empresa, numero_suministro, monto, referencia) VALUES
(1, 'LUZ',      'Luz del Sur',            '121345678',        145.00,   'SVC-10441'),
(1, 'AGUA',     'Sedapal',                '4521890',           68.00,   'SVC-01092'),
(1, 'INTERNET', 'Entel Fibra',            '7834512',          119.90,   'SVC-06781'),
(1, 'EDUCACION','Universidad Continental','UC-2026-889123',  1200.00,   'SVC-06112'),
(2, 'LUZ',      'Enel Distribución',      '98234567',          87.50,   'SVC-08910'),
(3, 'TELEFONIA','Movistar Hogar',          '456789012',         89.90,   'SVC-07654');

-- Transferencias Yapa
INSERT INTO transferencia_yapa (remitente_cliente_id, cliente_destino_id, numero_destino, monto, estado, referencia) VALUES
(1, 2,    '987-645-321',  80.00, 'COMPLETADO', 'YAP-11023'),
(1, NULL, '945-112-337',  50.00, 'COMPLETADO', 'YAP-09345'),
(2, 1,    '987-654-321', 100.00, 'COMPLETADO', 'YAP-08102'),
(1, NULL, '966-778-432', 200.00, 'COMPLETADO', 'YAP-06781'),
(1, NULL, '931-224-567', 100.00, 'COMPLETADO', 'YAP-01234');

-- Depósitos a plazo
INSERT INTO deposito_plazo (cliente_id, monto, tasa_interes_mensual, plazo_meses, fecha_inicio, fecha_fin, saldo_proyectado) VALUES
(1,  5000.00, 0.0054, 12, '2026-01-15', '2027-01-15',  5333.23),
(3, 20000.00, 0.0058, 24, '2025-06-01', '2027-06-01', 22935.60),
(5,  8000.00, 0.0050, 18, '2026-03-01', '2027-09-01',  8742.50);

-- Beneficiarios
INSERT INTO beneficiario (cliente_id, nombre, banco, numero_cuenta, alias) VALUES
(1, 'María López Quispe',  'BCP',        '194-0234100-1-14',    'María BCP'),
(1, 'Carlos Ramos Flores', 'Interbank',  '8982123456781234',    'Carlos Inter'),
(1, 'Ana García Torres',   'BBVA',       '0011-0175-0155904562','Ana BBVA'),
(2, 'Rosa Huanca Paucar',  'Mibanco',    '19000069801230',      'Rosa Ahorro'),
(3, 'Luis Torres Mendoza', 'Scotiabank', '056-XXXX-XXXX-4321',  'Luis Scotia');

-- Mora de ejemplo
INSERT INTO mora (cuota_id, dias_atraso, interes_moratorio, fecha_calculo) VALUES
(9, 5, 34.00, '2026-06-11');

-- ============================================================
-- FIN DEL SCRIPT — MIBANCO PERÚ
-- ============================================================
SELECT 'Base de datos MiBanco Perú (banco) creada exitosamente!' AS mensaje;
-- base de datos: tms_guide_db
-- motor: MySQL 8

CREATE DATABASE IF NOT EXISTS tms_guide_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE tms_guide_db;

-- tabla guia_remision
-- ciclo de vida: PENDIENTE → EN_PROCESO → ACEPTADA | RECHAZADA | ANULADA
CREATE TABLE guia_remision (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    uuid                CHAR(36)        NOT NULL,
    serie               VARCHAR(4)      NOT NULL COMMENT 'Ej: T001',
    correlativo         VARCHAR(8)      NOT NULL COMMENT 'Ej: 00000001',
    numero_completo     VARCHAR(15)     GENERATED ALWAYS AS (CONCAT(serie, '-', correlativo)) STORED,

    -- referencia a la orden de transporte
    orden_id            BIGINT          NOT NULL,

    -- motivo y modalidad
    motivo_traslado     VARCHAR(6)      NOT NULL COMMENT 'Código SUNAT: 01=venta, 02=compra...',
    modalidad           VARCHAR(2)      NOT NULL COMMENT '01=transporte público, 02=privado',
    fecha_inicio        DATE            NOT NULL,

    -- remitente
    remitente_ruc       VARCHAR(11)     NOT NULL,
    remitente_razon     VARCHAR(200)    NOT NULL,
    remitente_dir       VARCHAR(300)    NOT NULL,
    remitente_ubigeo    CHAR(6)         NOT NULL,

    -- destinatario
    destinatario_ruc    VARCHAR(11)     NOT NULL,
    destinatario_razon  VARCHAR(200)    NOT NULL,
    destinatario_dir    VARCHAR(300)    NOT NULL,
    destinatario_ubigeo CHAR(6)         NOT NULL,

    -- punto de llegada
    destino_dir         VARCHAR(300)    NOT NULL,
    destino_ubigeo      CHAR(6)         NOT NULL,

    -- vehículo y conductor
    vehiculo_placa      VARCHAR(8)      NOT NULL,
    conductor_dni       VARCHAR(8)      NOT NULL,
    conductor_nombre    VARCHAR(200)    NOT NULL,
    conductor_licencia  VARCHAR(12)     NOT NULL,

    -- estado del documento
    estado              ENUM('PENDIENTE','EN_PROCESO','ACEPTADA','RECHAZADA','ANULADA')
                        NOT NULL DEFAULT 'PENDIENTE',

    -- datos sunat / ose
    xml_firmado         LONGTEXT        NULL COMMENT 'XML firmado digitalmente',
    cdr_response        TEXT            NULL COMMENT 'cdr recibido del ose',
    cdr_codigo          VARCHAR(4)      NULL COMMENT 'codigo de respuesta
    sunat',
    cdr_descripcion     VARCHAR(500)    NULL,
    pdf_path            VARCHAR(500)    NULL COMMENT 'Ruta del PDF generado',

    -- cola de reenvio
    intentos_envio      TINYINT         NOT NULL DEFAULT 0,
    proximo_reenvio     DATETIME        NULL,

    -- auditoria
    creado_por          VARCHAR(100)    NOT NULL,
    creado_en           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modificado_en       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    anulado_en          DATETIME        NULL,
    anulado_por         VARCHAR(100)    NULL,
    motivo_anulacion    VARCHAR(300)    NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_guia_uuid          (uuid),
    UNIQUE KEY uq_guia_numero        (serie, correlativo),
    INDEX      idx_guia_orden        (orden_id),
    INDEX      idx_guia_estado       (estado),
    INDEX      idx_guia_fecha        (fecha_inicio),
    INDEX      idx_guia_reenvio      (proximo_reenvio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- tabla guia_detalle
-- bienes transportados
CREATE TABLE guia_detalle (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    guia_id         BIGINT          NOT NULL,
    item            TINYINT         NOT NULL COMMENT 'Línea dentro de la guía (1, 2, 3...)',
    descripcion     VARCHAR(500)    NOT NULL,
    unidad_medida   VARCHAR(3)      NOT NULL COMMENT 'Código UOM SUNAT: KGM, TNE, NIU...',
    cantidad        DECIMAL(12, 3)  NOT NULL,
    peso_bruto_kg   DECIMAL(12, 3)  NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_detalle_guia_item (guia_id, item),
    CONSTRAINT fk_detalle_guia
        FOREIGN KEY (guia_id) REFERENCES guia_remision(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- tabla auditoria_guia
-- registro inmutable de todas las operaciones sensibles
CREATE TABLE auditoria_guia (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    guia_id     BIGINT          NULL COMMENT 'NULL si el intento fue antes de crear la guía',
    accion      VARCHAR(50)     NOT NULL COMMENT 'GENERAR, ENVIAR, ANULAR, ACCESO_DENEGADO...',
    resultado   ENUM('OK','ERROR','DENEGADO') NOT NULL,
    detalle     VARCHAR(1000)   NULL,
    usuario     VARCHAR(100)    NOT NULL,
    ip_origen   VARCHAR(45)     NULL,
    registrado_en DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_auditoria_guia   (guia_id),
    INDEX idx_auditoria_fecha  (registrado_en)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Datos iniciales: series disponibles (una sola serie por ahora)
INSERT INTO guia_remision (uuid, serie, correlativo, orden_id,
    motivo_traslado, modalidad, fecha_inicio,
    remitente_ruc, remitente_razon, remitente_dir, remitente_ubigeo,
    destinatario_ruc, destinatario_razon, destinatario_dir, destinatario_ubigeo,
    destino_dir, destino_ubigeo,
    vehiculo_placa, conductor_dni, conductor_nombre, conductor_licencia,
    estado, creado_por)
SELECT NULL, NULL, NULL, NULL, NULL, NULL, NULL,
       NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
       NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL
WHERE 1 = 0; -- Sentencia vacía solo para validar estructura

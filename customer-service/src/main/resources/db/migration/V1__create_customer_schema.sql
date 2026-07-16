-- =====================================================================
-- Módulo 4 — Gestión de Clientes
-- V1: Esquema inicial
-- =====================================================================

CREATE TABLE IF NOT EXISTS cliente (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_cliente       VARCHAR(10)  NOT NULL UNIQUE,
    razon_social         VARCHAR(200) NOT NULL,
    ruc                  VARCHAR(11)  NOT NULL UNIQUE,
    direccion_fiscal     VARCHAR(300),
    telefono             VARCHAR(20),
    correo               VARCHAR(100),
    persona_contacto     VARCHAR(150),
    estado               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    motivo_inactivacion  VARCHAR(300),
    fecha_inactivacion   DATE,
    version              INTEGER      NOT NULL DEFAULT 0,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_estado CHECK (estado IN ('ACTIVO','INACTIVO')),
    CONSTRAINT chk_ruc_length CHECK (char_length(ruc) = 11)
);

CREATE TABLE IF NOT EXISTS condicion_pago (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id      UUID         NOT NULL REFERENCES cliente(id),
    plazo_dias      INTEGER      NOT NULL CHECK (plazo_dias >= 0),
    tipo_facturacion VARCHAR(20) NOT NULL DEFAULT 'CONTADO',
    linea_credito   NUMERIC(12,2),
    vigente_desde   DATE         NOT NULL,
    vigente_hasta   DATE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_tipo_fact CHECK (tipo_facturacion IN ('CONTADO','CREDITO'))
);

CREATE TABLE IF NOT EXISTS tarifa_cliente (
    id            UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id    UUID      NOT NULL REFERENCES cliente(id),
    tarifa_id     UUID      NOT NULL,
    vigente_desde DATE      NOT NULL,
    vigente_hasta DATE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS incidencia_cliente (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id       UUID         NOT NULL REFERENCES cliente(id),
    tipo_incidencia  VARCHAR(30)  NOT NULL,
    descripcion      TEXT         NOT NULL,
    orden_id         UUID,
    estado           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVA',
    registrado_por   VARCHAR(100),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_tipo_inc  CHECK (tipo_incidencia IN ('RECLAMO','DEVOLUCION','SINIESTRO','CAMBIO_CONDICIONES')),
    CONSTRAINT chk_estado_inc CHECK (estado IN ('ACTIVA','ANULADA'))
);

CREATE TABLE IF NOT EXISTS auditoria_cliente (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id       UUID         NOT NULL REFERENCES cliente(id),
    campo_modificado VARCHAR(100) NOT NULL,
    valor_anterior   TEXT,
    valor_nuevo      TEXT,
    modificado_por   VARCHAR(100),
    modified_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Índices para consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_cliente_ruc        ON cliente(ruc);
CREATE INDEX IF NOT EXISTS idx_cliente_estado      ON cliente(estado);
CREATE INDEX IF NOT EXISTS idx_cliente_razon       ON cliente(razon_social);
CREATE INDEX IF NOT EXISTS idx_condicion_cliente   ON condicion_pago(cliente_id);
CREATE INDEX IF NOT EXISTS idx_tarifa_cliente      ON tarifa_cliente(cliente_id);
CREATE INDEX IF NOT EXISTS idx_incidencia_cliente  ON incidencia_cliente(cliente_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_cliente   ON auditoria_cliente(cliente_id);

-- Secuencia para código de cliente (CLI-00001, CLI-00002, ...)
CREATE SEQUENCE IF NOT EXISTS seq_codigo_cliente START 1 INCREMENT 1;

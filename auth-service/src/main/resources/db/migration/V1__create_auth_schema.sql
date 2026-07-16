-- ============================================================
-- M8 Seguridad y Administracion - Schema inicial
-- ============================================================

CREATE TABLE rol (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(50)  NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE permiso (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo      VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    modulo      VARCHAR(50),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE rol_permiso (
    rol_id     UUID NOT NULL REFERENCES rol(id),
    permiso_id UUID NOT NULL REFERENCES permiso(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (rol_id, permiso_id)
);

CREATE TABLE usuario (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username         VARCHAR(50)  NOT NULL UNIQUE,
    correo           VARCHAR(150) NOT NULL UNIQUE,
    nombre_completo  VARCHAR(150) NOT NULL,
    password_hash    VARCHAR(255) NOT NULL,
    rol_id           UUID         NOT NULL REFERENCES rol(id),
    estado           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    intentos_fallidos INT         NOT NULL DEFAULT 0,
    bloqueado_hasta  TIMESTAMP,
    ultimo_acceso    TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE historial_password (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id    UUID         NOT NULL REFERENCES usuario(id),
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_token (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id  UUID         NOT NULL REFERENCES usuario(id),
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expira_en   TIMESTAMP    NOT NULL,
    revocado    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE evento_auditoria (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id  UUID         REFERENCES usuario(id),
    tipo_accion VARCHAR(50)  NOT NULL,
    modulo      VARCHAR(50),
    detalle     TEXT,
    ip_origen   VARCHAR(45),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indices
CREATE INDEX idx_usuario_username   ON usuario(username);
CREATE INDEX idx_usuario_correo     ON usuario(correo);
CREATE INDEX idx_usuario_estado     ON usuario(estado);
CREATE INDEX idx_refresh_token_hash ON refresh_token(token_hash);
CREATE INDEX idx_auditoria_usuario  ON evento_auditoria(usuario_id);
CREATE INDEX idx_auditoria_tipo     ON evento_auditoria(tipo_accion);
CREATE INDEX idx_auditoria_fecha    ON evento_auditoria(created_at);

-- Datos iniciales: roles
INSERT INTO rol (id, nombre, descripcion) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMINISTRADOR',       'Acceso total al sistema'),
    ('00000000-0000-0000-0000-000000000002', 'OPERADOR_LOGISTICO',  'Gestion de operaciones logisticas'),
    ('00000000-0000-0000-0000-000000000003', 'FINANZAS',            'Acceso a modulos financieros'),
    ('00000000-0000-0000-0000-000000000004', 'COMERCIAL',           'Gestion de clientes y tarifas');

-- Usuario admin inicial (password: Admin123!)
INSERT INTO usuario (username, correo, nombre_completo, password_hash, rol_id) VALUES
    ('admin', 'admin@tms-logistica.com', 'Administrador del Sistema',
     '$2a$10$N.zmdr9zkzoGtM.XGifF7.u5JCeijqiYPEiYGNcqnPj7sGDRp2HW..',
     '00000000-0000-0000-0000-000000000001');

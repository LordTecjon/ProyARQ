-- =====================================================================
-- Módulo 4 — Gestión de Clientes
-- V2: Patrón Cloud "Materialized View" (Tema Individual 0.8.4)
--
-- Objetivo del demo: contrastar una consulta de reporte "en caliente"
-- (JOIN + GROUP BY sobre miles de filas) contra una VISTA MATERIALIZADA
-- pre-calculada que responde en tiempo casi constante.
--
-- Nota: la tabla `orden_transporte` normalmente vive en el Módulo 1
-- (order-service). Aquí se incluye una versión mínima y LOCAL solo para
-- que el demo del patrón sea auto-contenido y ejecutable sin ese módulo.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Tabla de órdenes (simulada localmente para el demo)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orden_transporte (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id    UUID          NOT NULL REFERENCES cliente(id),
    estado        VARCHAR(20)   NOT NULL,
    monto         NUMERIC(12,2) NOT NULL DEFAULT 0,
    fecha_entrega TIMESTAMP,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_estado_orden CHECK (estado IN ('PENDIENTE','PROGRAMADA','EN_RUTA','ENTREGADA','ANULADA'))
);

CREATE INDEX IF NOT EXISTS idx_orden_cliente ON orden_transporte(cliente_id);
CREATE INDEX IF NOT EXISTS idx_orden_estado  ON orden_transporte(estado);

-- ---------------------------------------------------------------------
-- Vista materializada: reporte de clientes frecuentes (RF4.8)
-- Pre-calcula el conteo de órdenes ENTREGADAS y el monto total por cliente.
-- Se crea WITH DATA para que sea consultable de inmediato (vacía al inicio).
-- ---------------------------------------------------------------------
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_cliente_frecuente AS
SELECT c.id             AS cliente_id,
       c.codigo_cliente AS codigo_cliente,
       c.razon_social   AS razon_social,
       c.ruc            AS ruc,
       COUNT(o.id)      AS total_ordenes,
       COALESCE(SUM(o.monto), 0) AS monto_total,
       MAX(o.fecha_entrega)      AS ultima_orden
FROM cliente c
JOIN orden_transporte o ON o.cliente_id = c.id
WHERE o.estado = 'ENTREGADA'
GROUP BY c.id, c.codigo_cliente, c.razon_social, c.ruc
WITH DATA;

-- Índice ÚNICO: requerido para poder usar REFRESH MATERIALIZED VIEW CONCURRENTLY
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_cliente_frecuente_id
    ON mv_cliente_frecuente (cliente_id);

-- Índice de apoyo para el ORDER BY del reporte
CREATE INDEX IF NOT EXISTS idx_mv_cliente_frecuente_total
    ON mv_cliente_frecuente (total_ordenes DESC);

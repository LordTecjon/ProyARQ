package com.tms.logistica.customerservice.report;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Acceso a datos del reporte de clientes frecuentes mediante SQL nativo.
 * Contrasta dos caminos de lectura:
 *   - topRealtime():    JOIN + GROUP BY en caliente (costoso bajo volumen).
 *   - topMaterialized(): lectura directa de la vista materializada (rápida).
 */
@Repository
@RequiredArgsConstructor
public class FrequentReportRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<FrequentCustomerResponse> MAPPER = (rs, i) -> {
        Timestamp ts = rs.getTimestamp("ultima_orden");
        return FrequentCustomerResponse.builder()
                .clienteId(rs.getObject("cliente_id", UUID.class))
                .codigoCliente(rs.getString("codigo_cliente"))
                .razonSocial(rs.getString("razon_social"))
                .ruc(rs.getString("ruc"))
                .totalOrdenes(rs.getLong("total_ordenes"))
                .montoTotal(rs.getBigDecimal("monto_total"))
                .ultimaOrden(ts != null ? ts.toLocalDateTime() : null)
                .build();
    };

    /** Consulta en vivo: agrega sobre cliente + orden_transporte en cada llamada. */
    public List<FrequentCustomerResponse> topRealtime(int limit) {
        String sql = """
                SELECT c.id AS cliente_id, c.codigo_cliente, c.razon_social, c.ruc,
                       COUNT(o.id) AS total_ordenes,
                       COALESCE(SUM(o.monto), 0) AS monto_total,
                       MAX(o.fecha_entrega) AS ultima_orden
                FROM cliente c
                JOIN orden_transporte o ON o.cliente_id = c.id
                WHERE o.estado = 'ENTREGADA'
                GROUP BY c.id, c.codigo_cliente, c.razon_social, c.ruc
                ORDER BY total_ordenes DESC
                LIMIT ?
                """;
        return jdbc.query(sql, MAPPER, limit);
    }

    /** Lectura pre-calculada desde la vista materializada. */
    public List<FrequentCustomerResponse> topMaterialized(int limit) {
        String sql = """
                SELECT cliente_id, codigo_cliente, razon_social, ruc,
                       total_ordenes, monto_total, ultima_orden
                FROM mv_cliente_frecuente
                ORDER BY total_ordenes DESC
                LIMIT ?
                """;
        return jdbc.query(sql, MAPPER, limit);
    }

    /** Refresca la vista materializada (proceso de actualización del patrón). */
    public void refresh() {
        // Se usa REFRESH no concurrente por simplicidad. La vista tiene un índice
        // único, por lo que también admite: REFRESH MATERIALIZED VIEW CONCURRENTLY ...
        jdbc.execute("REFRESH MATERIALIZED VIEW mv_cliente_frecuente");
    }

    /**
     * Inserta datos de demostración: `clientes` clientes ficticios y
     * `ordenesPorCliente` órdenes por cada uno. Devuelve el total de órdenes en la tabla.
     *
     * Los códigos de demo usan la banda 'CLI-9xxxxx' (numérica) para no romper el
     * generador de códigos existente, que hace CAST(SUBSTRING(codigo,5) AS integer).
     * Los clientes de demo se identifican por su razón social ('Demo Cliente N').
     */
    public long seed(int clientes, int ordenesPorCliente) {
        jdbc.update("""
                INSERT INTO cliente (codigo_cliente, razon_social, ruc, estado)
                SELECT 'CLI-' || LPAD((900000 + g)::text, 6, '0'),
                       'Demo Cliente ' || g,
                       '20' || LPAD(g::text, 9, '0'),
                       'ACTIVO'
                FROM generate_series(1, ?) g
                ON CONFLICT DO NOTHING
                """, clientes);

        jdbc.update("""
                INSERT INTO orden_transporte (cliente_id, estado, monto, fecha_entrega)
                SELECT c.id,
                       CASE WHEN random() < 0.85 THEN 'ENTREGADA' ELSE 'PENDIENTE' END,
                       ROUND((random() * 900 + 100)::numeric, 2),
                       NOW() - (FLOOR(random() * 90)::text || ' days')::interval
                FROM cliente c
                CROSS JOIN generate_series(1, ?) s
                WHERE c.razon_social LIKE 'Demo Cliente %'
                """, ordenesPorCliente);

        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM orden_transporte", Long.class);
        return total != null ? total : 0L;
    }
}

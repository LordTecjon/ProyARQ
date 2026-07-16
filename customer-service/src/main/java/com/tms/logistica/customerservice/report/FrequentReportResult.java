package com.tms.logistica.customerservice.report;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Envoltura del resultado del reporte de clientes frecuentes.
 * Incluye la fuente (vista materializada vs. consulta en vivo) y el tiempo
 * transcurrido, para evidenciar en el demo la diferencia de latencia del
 * patrón Materialized View.
 */
@Data
@Builder
public class FrequentReportResult {
    /** MATERIALIZED_VIEW | REALTIME */
    private String source;
    /** Tiempo de ejecución de la consulta en milisegundos */
    private long elapsedMs;
    private int  count;
    private List<FrequentCustomerResponse> data;
}

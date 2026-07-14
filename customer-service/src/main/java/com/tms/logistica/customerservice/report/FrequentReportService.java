package com.tms.logistica.customerservice.report;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Lógica del reporte de clientes frecuentes (patrón Materialized View, 0.8.4).
 *
 * Cada consulta se cronometra y se publica como métrica Micrometer
 * `tms.frequent.report` (etiqueta source = materialized|realtime), que Prometheus
 * expone como `tms_frequent_report_seconds` — enlazando este demo con el Tema
 * de Observabilidad (0.7.4).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FrequentReportService {

    private final FrequentReportRepository repo;
    private final MeterRegistry meterRegistry;

    public FrequentReportResult realtime(int limit) {
        long startNs = System.nanoTime();
        List<FrequentCustomerResponse> data = repo.topRealtime(limit);
        long elapsedMs = record("realtime", startNs);
        log.info("Reporte clientes frecuentes REALTIME: {} filas en {} ms", data.size(), elapsedMs);
        return build("REALTIME", elapsedMs, data);
    }

    public FrequentReportResult materialized(int limit) {
        long startNs = System.nanoTime();
        List<FrequentCustomerResponse> data = repo.topMaterialized(limit);
        long elapsedMs = record("materialized", startNs);
        log.info("Reporte clientes frecuentes MATERIALIZED: {} filas en {} ms", data.size(), elapsedMs);
        return build("MATERIALIZED_VIEW", elapsedMs, data);
    }

    public void refresh() {
        long startNs = System.nanoTime();
        repo.refresh();
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        meterRegistry.timer("tms.frequent.report", "source", "refresh")
                .record(elapsedMs, TimeUnit.MILLISECONDS);
        log.info("Vista materializada refrescada en {} ms", elapsedMs);
    }

    public long seed(int clientes, int ordenesPorCliente) {
        return repo.seed(clientes, ordenesPorCliente);
    }

    private long record(String source, long startNs) {
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        meterRegistry.timer("tms.frequent.report", "source", source)
                .record(elapsedMs, TimeUnit.MILLISECONDS);
        return elapsedMs;
    }

    private FrequentReportResult build(String source, long elapsedMs, List<FrequentCustomerResponse> data) {
        return FrequentReportResult.builder()
                .source(source)
                .elapsedMs(elapsedMs)
                .count(data.size())
                .data(data)
                .build();
    }
}

package com.tms.logistica.customerservice.report;

import com.tms.logistica.customerservice.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints del demo del patrón Materialized View (Tema Individual 0.8.4).
 *
 * Flujo sugerido:
 *   1. POST /seed?clientes=200&ordenes=50   → genera datos (10.000 órdenes).
 *   2. GET  /realtime                        → JOIN en caliente (lento).
 *   3. POST /refresh                         → recalcula la vista materializada.
 *   4. GET  /materialized                    → lectura pre-calculada (rápida).
 * Comparar el campo `elapsedMs` de los pasos 2 y 4.
 */
@RestController
@RequestMapping("/api/v1/reports/frequent-customers")
@RequiredArgsConstructor
@Tag(name = "Demo — Materialized View (0.8.4)",
     description = "Reporte de clientes frecuentes: consulta en vivo vs. vista materializada")
public class FrequentReportController {

    private final FrequentReportService service;

    @GetMapping("/realtime")
    @Operation(summary = "Reporte con JOIN en caliente (sin patrón)")
    public ResponseEntity<FrequentReportResult> realtime(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.realtime(limit));
    }

    @GetMapping("/materialized")
    @Operation(summary = "Reporte desde la vista materializada (con patrón)")
    public ResponseEntity<FrequentReportResult> materialized(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.materialized(limit));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar la vista materializada (proceso de actualización)")
    public ResponseEntity<ApiResponse<Void>> refresh() {
        service.refresh();
        return ResponseEntity.ok(ApiResponse.ok("Vista materializada refrescada.", null));
    }

    @PostMapping("/seed")
    @Operation(summary = "Generar datos de demostración (clientes y órdenes)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seed(
            @RequestParam(defaultValue = "200") int clientes,
            @RequestParam(defaultValue = "50") int ordenes) {
        long totalOrdenes = service.seed(clientes, ordenes);
        return ResponseEntity.ok(ApiResponse.ok(
                "Datos de demo generados. Ejecute /refresh antes de consultar /materialized.",
                Map.of("clientesSolicitados", clientes,
                       "ordenesPorCliente", ordenes,
                       "totalOrdenesEnTabla", totalOrdenes)));
    }
}

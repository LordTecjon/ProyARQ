package com.tms.logistica.customerservice.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fila del reporte de clientes frecuentes (RF4.8).
 * Representa una agregación (conteo de órdenes y monto total) por cliente.
 */
@Data
@Builder
public class FrequentCustomerResponse {
    private UUID          clienteId;
    private String        codigoCliente;
    private String        razonSocial;
    private String        ruc;
    private long          totalOrdenes;
    private BigDecimal    montoTotal;
    private LocalDateTime ultimaOrden;
}

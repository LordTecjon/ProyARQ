package com.tms.logistica.billingservice.model.dto.response;

import com.tms.logistica.billingservice.model.enums.EstadoComprobante;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CuentaPorCobrarResponse {
    private Long comprobanteId;
    private String serie;
    private String correlativo;
    private Long clienteId;
    private String clienteNombre;
    private EstadoComprobante estado;
    private BigDecimal total;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private long diasVencido;
    private boolean vencido;
}

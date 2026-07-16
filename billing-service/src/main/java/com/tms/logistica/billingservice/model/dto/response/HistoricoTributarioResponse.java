package com.tms.logistica.billingservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class HistoricoTributarioResponse {
    private Long clienteId;
    private int totalComprobantes;
    private BigDecimal totalFacturado;
    private BigDecimal totalIgv;
    private BigDecimal totalPagado;
    private BigDecimal saldoPendiente;
    private List<ComprobanteResponse> comprobantes;
}

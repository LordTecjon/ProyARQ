package com.tms.logistica.billingservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LineaComprobanteResponse {
    private Long id;
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotalLinea;
    private Long otId;
}

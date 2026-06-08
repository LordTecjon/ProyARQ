package com.tms.logistica.costservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MargenResponse {
    private Long ordenId;
    private BigDecimal ingreso;
    private BigDecimal costoReal;
    private BigDecimal margen;
    private BigDecimal porcentajeMargen;
}

package com.tms.logistica.costservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ComparacionCostoResponse {
    private Long ordenId;
    private BigDecimal costoEstimado;
    private BigDecimal costoReal;
    private BigDecimal diferencia;
    private BigDecimal porcentajeDesviacion;
}

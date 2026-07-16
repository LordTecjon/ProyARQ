package com.tms.logistica.costservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ResumenCostoResponse {
    private Long ordenId;
    private BigDecimal costoEstimado;
    private BigDecimal costoReal;
    private BigDecimal combustible;
    private BigDecimal peajes;
    private BigDecimal viaticos;
    private BigDecimal otrosGastos;
    private BigDecimal diferencia;
    private BigDecimal ingreso;
    private BigDecimal margen;
    private List<GastoResponse> gastos;
}

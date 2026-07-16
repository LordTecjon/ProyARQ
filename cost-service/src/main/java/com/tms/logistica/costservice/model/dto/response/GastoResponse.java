package com.tms.logistica.costservice.model.dto.response;

import com.tms.logistica.costservice.model.enums.TipoGasto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GastoResponse {
    private Long id;
    private TipoGasto tipoGasto;
    private BigDecimal monto;
    private String concepto;
    private String observaciones;
    private String registradoPor;
    private LocalDateTime registradoEn;
}

package com.tms.logistica.orderservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrdenDetalleResponse {
    private Long id;
    private String descripcion;
    private Integer cantidad;
    private BigDecimal pesoKg;
    private BigDecimal volumenM3;
}

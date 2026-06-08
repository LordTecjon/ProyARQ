package com.tms.logistica.costservice.model.dto.response;

import com.tms.logistica.costservice.model.enums.EstadoCosto;
import com.tms.logistica.costservice.model.enums.TipoServicio;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CostoResponse {
    private Long id;
    private String uuid;
    private Long ordenId;
    private TipoServicio tipoServicio;
    private BigDecimal distanciaKm;
    private BigDecimal pesoKg;
    private BigDecimal tarifaBase;
    private BigDecimal costoDistancia;
    private BigDecimal costoPeso;
    private BigDecimal recargoServicio;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private EstadoCosto estado;
    private String observaciones;
    private LocalDateTime creadoEn;
    private LocalDateTime modificadoEn;
}

package com.tms.logistica.orderservice.model.dto.response;

import com.tms.logistica.orderservice.model.enums.EstadoOrden;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrazabilidadResponse {
    private Long id;
    private EstadoOrden estadoAnterior;
    private EstadoOrden estadoNuevo;
    private String accion;
    private String observaciones;
    private String usuario;
    private LocalDateTime fecha;
}

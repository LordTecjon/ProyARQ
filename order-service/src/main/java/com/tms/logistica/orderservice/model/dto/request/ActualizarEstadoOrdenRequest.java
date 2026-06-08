package com.tms.logistica.orderservice.model.dto.request;

import com.tms.logistica.orderservice.model.enums.EstadoOrden;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarEstadoOrdenRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoOrden estado;

    private String observaciones;
}

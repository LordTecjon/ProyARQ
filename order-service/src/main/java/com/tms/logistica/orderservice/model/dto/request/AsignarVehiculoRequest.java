package com.tms.logistica.orderservice.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarVehiculoRequest {

    @NotNull(message = "El vehiculo es obligatorio")
    private Long vehiculoId;

    private String observaciones;
}

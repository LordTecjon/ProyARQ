package com.tms.logistica.orderservice.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarConductorRequest {

    @NotNull(message = "El conductor es obligatorio")
    private Long conductorId;

    private String observaciones;
}

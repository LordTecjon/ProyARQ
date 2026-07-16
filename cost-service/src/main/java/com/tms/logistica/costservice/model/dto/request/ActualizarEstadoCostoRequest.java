package com.tms.logistica.costservice.model.dto.request;

import com.tms.logistica.costservice.model.enums.EstadoCosto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActualizarEstadoCostoRequest {

    @NotNull(message = "El estado es obligatorio")
    private EstadoCosto estado;

    private String observaciones;
}

package com.tms.logistica.customerservice.dto.request;

import com.tms.logistica.customerservice.domain.enums.IncidentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterIncidentRequest {

    @NotNull(message = "El tipo de incidencia es obligatorio")
    private IncidentType tipoIncidencia;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private UUID ordenId;

    private String registradoPor;
}

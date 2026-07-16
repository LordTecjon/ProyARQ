package com.tms.logistica.billingservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmitirNotaCreditoRequest {

    @NotBlank(message = "El motivo de la nota de credito es obligatorio")
    private String motivo;
}

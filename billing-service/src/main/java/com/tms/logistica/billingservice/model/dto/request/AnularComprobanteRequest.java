package com.tms.logistica.billingservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnularComprobanteRequest {

    @NotBlank(message = "El motivo de anulacion es obligatorio")
    private String motivo;
}

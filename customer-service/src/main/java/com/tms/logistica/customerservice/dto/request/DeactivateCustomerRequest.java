package com.tms.logistica.customerservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeactivateCustomerRequest {

    @NotBlank(message = "El motivo de inactivación es obligatorio")
    @Size(max = 300)
    private String motivoInactivacion;
}

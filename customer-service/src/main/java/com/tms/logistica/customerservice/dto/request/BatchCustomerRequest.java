package com.tms.logistica.customerservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BatchCustomerRequest {

    @NotEmpty(message = "La lista de IDs no puede estar vacía")
    @Size(max = 500, message = "Se permiten como máximo 500 IDs por solicitud")
    private List<UUID> ids;
}

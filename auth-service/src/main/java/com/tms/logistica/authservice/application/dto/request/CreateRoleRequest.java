package com.tms.logistica.authservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoleRequest {
    @NotBlank @Size(max = 50)
    private String nombre;
    @Size(max = 200)
    private String descripcion;
}

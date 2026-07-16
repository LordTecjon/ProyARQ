package com.tms.logistica.authservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String passwordActual;
    @NotBlank @Size(min = 8)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).*$",
             message = "La contrasena debe tener mayuscula, numero y caracter especial")
    private String passwordNuevo;
}

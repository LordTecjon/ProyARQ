package com.tms.logistica.authservice.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterUserRequest {
    @NotBlank @Size(min = 3, max = 50)
    private String username;
    @NotBlank @Email
    private String correo;
    @NotBlank @Size(max = 150)
    private String nombreCompleto;
    @NotBlank @Size(min = 8)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).*$",
             message = "La contrasena debe tener mayuscula, numero y caracter especial")
    private String password;
    private UUID rolId;
}

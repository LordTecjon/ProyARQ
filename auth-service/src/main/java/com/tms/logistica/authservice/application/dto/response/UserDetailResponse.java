package com.tms.logistica.authservice.application.dto.response;

import com.tms.logistica.authservice.domain.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserDetailResponse {
    private UUID id;
    private String username;
    private String correo;
    private String nombreCompleto;
    private RoleResponse rol;
    private UserStatus estado;
    private Integer intentosFallidos;
    private LocalDateTime bloqueadoHasta;
    private LocalDateTime ultimoAcceso;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

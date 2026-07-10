package com.tms.logistica.authservice.application.dto.response;

import com.tms.logistica.authservice.domain.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserSummaryResponse {
    private UUID id;
    private String username;
    private String correo;
    private String nombreCompleto;
    private String rol;
    private UserStatus estado;
}

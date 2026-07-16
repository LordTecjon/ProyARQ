package com.tms.logistica.authservice.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PermissionResponse {
    private UUID id;
    private String codigo;
    private String descripcion;
    private String modulo;
}

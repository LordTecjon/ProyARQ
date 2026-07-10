package com.tms.logistica.authservice.application.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RoleResponse {
    private UUID id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private List<PermissionResponse> permisos;
}

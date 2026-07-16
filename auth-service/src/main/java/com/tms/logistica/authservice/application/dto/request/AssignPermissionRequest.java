package com.tms.logistica.authservice.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignPermissionRequest {
    @NotNull
    private UUID permisoId;
}

package com.tms.logistica.authservice.infrastructure.controller;

import com.tms.logistica.authservice.application.dto.request.AssignPermissionRequest;
import com.tms.logistica.authservice.application.dto.request.CreateRoleRequest;
import com.tms.logistica.authservice.application.dto.response.ApiResponse;
import com.tms.logistica.authservice.application.dto.response.PermissionResponse;
import com.tms.logistica.authservice.application.dto.response.RoleResponse;
import com.tms.logistica.authservice.application.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Roles y Permisos", description = "Gestion de roles y permisos")
public class RoleController {

    private final RolePermissionService rolePermissionService;

    @GetMapping
    @Operation(summary = "Listar roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> listRoles() {
        return ResponseEntity.ok(ApiResponse.ok(rolePermissionService.listRoles()));
    }

    @PostMapping
    @Operation(summary = "Crear rol")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request,
            HttpServletRequest http,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Rol creado", rolePermissionService.createRole(request, username, getIp(http))));
    }

    @PostMapping("/{rolId}/permissions")
    @Operation(summary = "Asignar permiso a rol")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermission(
            @PathVariable UUID rolId,
            @Valid @RequestBody AssignPermissionRequest request,
            HttpServletRequest http,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(ApiResponse.ok("Permiso asignado",
                rolePermissionService.assignPermission(rolId, request, username, getIp(http))));
    }

    @DeleteMapping("/{rolId}/permissions/{permisoId}")
    @Operation(summary = "Remover permiso de rol")
    public ResponseEntity<ApiResponse<Void>> removePermission(
            @PathVariable UUID rolId,
            @PathVariable UUID permisoId,
            HttpServletRequest http,
            @AuthenticationPrincipal String username) {
        rolePermissionService.removePermission(rolId, permisoId, username, getIp(http));
        return ResponseEntity.ok(ApiResponse.ok("Permiso removido", null));
    }

    @GetMapping("/permissions")
    @Operation(summary = "Listar permisos disponibles")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> listPermissions() {
        return ResponseEntity.ok(ApiResponse.ok(rolePermissionService.listPermissions()));
    }

    private String getIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (StringUtils.hasText(xff)) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }
}

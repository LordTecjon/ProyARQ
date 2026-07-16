package com.tms.logistica.authservice.infrastructure.controller;

import com.tms.logistica.authservice.application.dto.request.AssignPermissionRequest;
import com.tms.logistica.authservice.application.dto.request.RegisterUserRequest;
import com.tms.logistica.authservice.application.dto.response.ApiResponse;
import com.tms.logistica.authservice.application.dto.response.UserDetailResponse;
import com.tms.logistica.authservice.application.dto.response.UserSummaryResponse;
import com.tms.logistica.authservice.application.service.UserService;
import com.tms.logistica.authservice.domain.enums.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestion de usuarios del sistema")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Crear usuario")
    public ResponseEntity<ApiResponse<UserDetailResponse>> createUser(
            @Valid @RequestBody RegisterUserRequest request,
            HttpServletRequest http,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario creado", userService.createUser(request, username, getIp(http))));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar usuarios")
    public ResponseEntity<ApiResponse<Page<UserSummaryResponse>>> listUsers(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) UserStatus estado,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(userService.listUsers(nombre, estado, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Detalle de usuario")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserById(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cambiar estado del usuario")
    public ResponseEntity<ApiResponse<UserDetailResponse>> changeStatus(
            @PathVariable UUID id,
            @RequestParam UserStatus estado,
            HttpServletRequest http,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado",
                userService.changeUserStatus(id, estado, username, getIp(http))));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Asignar rol al usuario")
    public ResponseEntity<ApiResponse<UserDetailResponse>> assignRole(
            @PathVariable UUID id,
            @Valid @RequestBody AssignPermissionRequest request,
            HttpServletRequest http,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(ApiResponse.ok("Rol asignado",
                userService.assignRole(id, request.getPermisoId(), username, getIp(http))));
    }

    private String getIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (StringUtils.hasText(xff)) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }
}

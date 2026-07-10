package com.tms.logistica.authservice.infrastructure.controller;

import com.tms.logistica.authservice.application.dto.request.ChangePasswordRequest;
import com.tms.logistica.authservice.application.dto.request.LoginRequest;
import com.tms.logistica.authservice.application.dto.request.RefreshTokenRequest;
import com.tms.logistica.authservice.application.dto.response.ApiResponse;
import com.tms.logistica.authservice.application.dto.response.AuthResponse;
import com.tms.logistica.authservice.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Login, logout y gestion de tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", authService.login(request, getIp(http))));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesion")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest http,
                                                     @AuthenticationPrincipal String username) {
        authService.logout(extractToken(http), getIp(http));
        return ResponseEntity.ok(ApiResponse.ok("Sesion cerrada", null));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contrasena")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                             HttpServletRequest http,
                                                             @AuthenticationPrincipal String username) {
        authService.changePassword(username, request, getIp(http));
        return ResponseEntity.ok(ApiResponse.ok("Contrasena actualizada", null));
    }

    private String extractToken(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        return (StringUtils.hasText(h) && h.startsWith("Bearer ")) ? h.substring(7) : null;
    }

    private String getIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (StringUtils.hasText(xff)) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }
}

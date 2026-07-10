package com.tms.logistica.authservice.application.service;

import com.tms.logistica.authservice.application.dto.request.ChangePasswordRequest;
import com.tms.logistica.authservice.application.dto.request.LoginRequest;
import com.tms.logistica.authservice.application.dto.request.RefreshTokenRequest;
import com.tms.logistica.authservice.application.dto.response.AuthResponse;
import com.tms.logistica.authservice.application.dto.response.UserSummaryResponse;
import com.tms.logistica.authservice.application.exception.AccountLockedException;
import com.tms.logistica.authservice.application.exception.InvalidCredentialsException;
import com.tms.logistica.authservice.application.exception.UserNotFoundException;
import com.tms.logistica.authservice.domain.entity.User;
import com.tms.logistica.authservice.domain.enums.AuditEventType;
import com.tms.logistica.authservice.domain.enums.UserStatus;
import com.tms.logistica.authservice.domain.repository.PasswordHistoryRepository;
import com.tms.logistica.authservice.domain.repository.UserRepository;
import com.tms.logistica.authservice.infrastructure.security.BruteForceProtectionService;
import com.tms.logistica.authservice.infrastructure.security.JwtProperties;
import com.tms.logistica.authservice.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final TokenService tokenService;
    private final BruteForceProtectionService bruteForce;
    private final SecurityAuditService auditService;

    @Transactional
    public AuthResponse login(LoginRequest request, String ip) {
        if (bruteForce.isBlocked(request.getUsername(), ip)) {
            auditService.log(AuditEventType.BLOQUEO_CUENTA, "M8",
                    "Bloqueado por intentos fallidos: " + request.getUsername(), ip);
            throw new AccountLockedException("Cuenta bloqueada temporalmente. Intente en 15 minutos.");
        }

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> {
            bruteForce.recordFailedAttempt(request.getUsername(), ip);
            return new InvalidCredentialsException("Credenciales invalidas");
        });

        if (user.isBloqueado()) {
            throw new AccountLockedException("Cuenta bloqueada hasta: " + user.getBloqueadoHasta());
        }
        if (user.getEstado() == UserStatus.INACTIVO) {
            throw new AccountLockedException("La cuenta esta inactiva");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            bruteForce.recordFailedAttempt(request.getUsername(), ip);
            auditService.log(user, AuditEventType.LOGIN_FALLIDO, "M8", "Contrasena incorrecta", ip);
            throw new InvalidCredentialsException("Credenciales invalidas");
        }

        bruteForce.resetAttempts(request.getUsername(), ip);
        user.setUltimoAcceso(LocalDateTime.now());
        userRepository.save(user);
        auditService.log(user, AuditEventType.LOGIN, "M8", "Login exitoso", ip);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        User user = tokenService.validateRefreshToken(request.getRefreshToken());
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String token, String ip) {
        if (token == null) return;
        jwtUtil.blacklistToken(jwtUtil.getJtiFromToken(token), jwtUtil.getExpirationFromToken(token));
        userRepository.findByUsername(jwtUtil.getUsernameFromToken(token)).ifPresent(user -> {
            tokenService.revokeAllUserTokens(user.getId());
            auditService.log(user, AuditEventType.LOGOUT, "M8", "Logout", ip);
        });
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request, String ip) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPasswordActual(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("La contrasena actual es incorrecta");
        }

        var recientes = passwordHistoryRepository.findRecentByUserId(user.getId(), PageRequest.of(0, 5));
        if (recientes.stream().anyMatch(ph -> passwordEncoder.matches(request.getPasswordNuevo(), ph.getPasswordHash()))) {
            throw new InvalidCredentialsException("No puede reutilizar una contrasena reciente");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPasswordNuevo()));
        userRepository.save(user);
        tokenService.revokeAllUserTokens(user.getId());
        auditService.log(user, AuditEventType.CAMBIO_PASSWORD, "M8", "Cambio de contrasena", ip);
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .accessToken(tokenService.generateAccessToken(user))
                .refreshToken(tokenService.generateRefreshToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationMs() / 1000)
                .usuario(UserSummaryResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .correo(user.getCorreo())
                        .nombreCompleto(user.getNombreCompleto())
                        .rol(user.getRol().getNombre())
                        .estado(user.getEstado())
                        .build())
                .build();
    }
}

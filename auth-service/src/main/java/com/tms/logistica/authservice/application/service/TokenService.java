package com.tms.logistica.authservice.application.service;

import com.tms.logistica.authservice.application.exception.TokenExpiredException;
import com.tms.logistica.authservice.domain.entity.RefreshToken;
import com.tms.logistica.authservice.domain.entity.User;
import com.tms.logistica.authservice.domain.repository.RefreshTokenRepository;
import com.tms.logistica.authservice.infrastructure.security.JwtProperties;
import com.tms.logistica.authservice.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public String generateAccessToken(User user) {
        return jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRol().getNombre());
    }

    @Transactional
    public String generateRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshTokenExpirationMs() / 1000);
        refreshTokenRepository.save(RefreshToken.builder()
                .usuario(user)
                .tokenHash(passwordEncoder.encode(rawToken))
                .expiraEn(expiry)
                .build());
        return rawToken;
    }

    @Transactional(readOnly = true)
    public User validateRefreshToken(String rawToken) {
        return refreshTokenRepository.findAll().stream()
                .filter(rt -> !rt.getRevocado() && !rt.isExpired()
                        && passwordEncoder.matches(rawToken, rt.getTokenHash()))
                .findFirst()
                .map(RefreshToken::getUsuario)
                .orElseThrow(() -> new TokenExpiredException("Refresh token invalido o expirado"));
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}

package com.tms.logistica.authservice.application.service;

import com.tms.logistica.authservice.application.dto.request.RegisterUserRequest;
import com.tms.logistica.authservice.application.dto.response.RoleResponse;
import com.tms.logistica.authservice.application.dto.response.UserDetailResponse;
import com.tms.logistica.authservice.application.dto.response.UserSummaryResponse;
import com.tms.logistica.authservice.application.exception.DuplicateResourceException;
import com.tms.logistica.authservice.application.exception.UserNotFoundException;
import com.tms.logistica.authservice.domain.entity.Role;
import com.tms.logistica.authservice.domain.entity.User;
import com.tms.logistica.authservice.domain.enums.AuditEventType;
import com.tms.logistica.authservice.domain.enums.UserStatus;
import com.tms.logistica.authservice.domain.repository.RoleRepository;
import com.tms.logistica.authservice.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuditService auditService;
    private final TokenService tokenService;

    @Transactional
    public UserDetailResponse createUser(RegisterUserRequest request, String creatorUsername, String ip) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new DuplicateResourceException("El username ya esta en uso");
        if (userRepository.existsByCorreo(request.getCorreo()))
            throw new DuplicateResourceException("El correo ya esta registrado");

        Role rol = request.getRolId() != null
                ? roleRepository.findById(request.getRolId())
                        .orElseThrow(() -> new UserNotFoundException("Rol no encontrado"))
                : roleRepository.findByNombre("OPERADOR_LOGISTICO")
                        .orElseThrow(() -> new UserNotFoundException("Rol por defecto no encontrado"));

        User user = User.builder()
                .username(request.getUsername())
                .correo(request.getCorreo())
                .nombreCompleto(request.getNombreCompleto())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .build();
        userRepository.save(user);

        userRepository.findByUsername(creatorUsername).ifPresent(creator ->
            auditService.log(creator, AuditEventType.CREACION_USUARIO, "M8",
                    "Creo usuario: " + user.getUsername(), ip));
        return toDetail(user);
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> listUsers(String nombre, UserStatus estado, Pageable pageable) {
        return userRepository.findAllWithFilters(nombre, estado, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(UUID id) {
        return toDetail(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + id)));
    }

    @Transactional
    public UserDetailResponse changeUserStatus(UUID id, UserStatus newStatus, String editorUsername, String ip) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + id));
        user.setEstado(newStatus);
        if (newStatus == UserStatus.INACTIVO) tokenService.revokeAllUserTokens(id);
        userRepository.save(user);
        userRepository.findByUsername(editorUsername).ifPresent(editor ->
            auditService.log(editor, AuditEventType.INACTIVACION_USUARIO, "M8",
                    "Cambio estado de " + user.getUsername() + " a " + newStatus, ip));
        return toDetail(user);
    }

    @Transactional
    public UserDetailResponse assignRole(UUID userId, UUID rolId, String editorUsername, String ip) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        Role rol = roleRepository.findById(rolId)
                .orElseThrow(() -> new UserNotFoundException("Rol no encontrado"));
        user.setRol(rol);
        userRepository.save(user);
        tokenService.revokeAllUserTokens(userId);
        userRepository.findByUsername(editorUsername).ifPresent(editor ->
            auditService.log(editor, AuditEventType.CAMBIO_ROL, "M8",
                    "Asigno rol " + rol.getNombre() + " a " + user.getUsername(), ip));
        return toDetail(user);
    }

    private UserSummaryResponse toSummary(User u) {
        return UserSummaryResponse.builder()
                .id(u.getId()).username(u.getUsername()).correo(u.getCorreo())
                .nombreCompleto(u.getNombreCompleto()).rol(u.getRol().getNombre()).estado(u.getEstado()).build();
    }

    private UserDetailResponse toDetail(User u) {
        return UserDetailResponse.builder()
                .id(u.getId()).username(u.getUsername()).correo(u.getCorreo())
                .nombreCompleto(u.getNombreCompleto())
                .rol(RoleResponse.builder().id(u.getRol().getId()).nombre(u.getRol().getNombre()).build())
                .estado(u.getEstado()).intentosFallidos(u.getIntentosFallidos())
                .bloqueadoHasta(u.getBloqueadoHasta()).ultimoAcceso(u.getUltimoAcceso())
                .createdAt(u.getCreatedAt()).updatedAt(u.getUpdatedAt()).build();
    }
}

package com.tms.logistica.authservice.application.service;

import com.tms.logistica.authservice.application.dto.request.AssignPermissionRequest;
import com.tms.logistica.authservice.application.dto.request.CreateRoleRequest;
import com.tms.logistica.authservice.application.dto.response.PermissionResponse;
import com.tms.logistica.authservice.application.dto.response.RoleResponse;
import com.tms.logistica.authservice.application.exception.DuplicateResourceException;
import com.tms.logistica.authservice.application.exception.UserNotFoundException;
import com.tms.logistica.authservice.domain.entity.*;
import com.tms.logistica.authservice.domain.enums.AuditEventType;
import com.tms.logistica.authservice.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final SecurityAuditService auditService;

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request, String creatorUsername, String ip) {
        if (roleRepository.existsByNombre(request.getNombre()))
            throw new DuplicateResourceException("El rol ya existe: " + request.getNombre());
        Role role = Role.builder().nombre(request.getNombre()).descripcion(request.getDescripcion()).build();
        roleRepository.save(role);
        userRepository.findByUsername(creatorUsername).ifPresent(u ->
            auditService.log(u, AuditEventType.ASIGNACION_PERMISO, "M8", "Creo rol: " + role.getNombre(), ip));
        return toResponse(role);
    }

    @Transactional
    public RoleResponse assignPermission(UUID rolId, AssignPermissionRequest request, String editorUsername, String ip) {
        Role role = roleRepository.findById(rolId).orElseThrow(() -> new UserNotFoundException("Rol no encontrado"));
        Permission perm = permissionRepository.findById(request.getPermisoId())
                .orElseThrow(() -> new UserNotFoundException("Permiso no encontrado"));
        RolePermissionId id = new RolePermissionId(rolId, perm.getId());
        if (rolePermissionRepository.existsById(id))
            throw new DuplicateResourceException("El permiso ya esta asignado a este rol");
        rolePermissionRepository.save(RolePermission.builder().id(id).role(role).permission(perm).build());
        userRepository.findByUsername(editorUsername).ifPresent(u ->
            auditService.log(u, AuditEventType.ASIGNACION_PERMISO, "M8",
                    "Asigno permiso " + perm.getCodigo() + " a rol " + role.getNombre(), ip));
        return toResponse(role);
    }

    @Transactional
    public void removePermission(UUID rolId, UUID permisoId, String editorUsername, String ip) {
        rolePermissionRepository.deleteByRoleIdAndPermissionId(rolId, permisoId);
        userRepository.findByUsername(editorUsername).ifPresent(u ->
            auditService.log(u, AuditEventType.ASIGNACION_PERMISO, "M8",
                    "Removio permiso " + permisoId + " del rol " + rolId, ip));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream().map(this::toPermResponse).toList();
    }

    private RoleResponse toResponse(Role role) {
        List<PermissionResponse> perms = rolePermissionRepository.findByRoleId(role.getId()).stream()
                .map(rp -> toPermResponse(rp.getPermission())).toList();
        return RoleResponse.builder().id(role.getId()).nombre(role.getNombre())
                .descripcion(role.getDescripcion()).activo(role.getActivo()).permisos(perms).build();
    }

    private PermissionResponse toPermResponse(Permission p) {
        return PermissionResponse.builder().id(p.getId()).codigo(p.getCodigo())
                .descripcion(p.getDescripcion()).modulo(p.getModulo()).build();
    }
}

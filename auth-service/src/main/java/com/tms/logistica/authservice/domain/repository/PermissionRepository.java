package com.tms.logistica.authservice.domain.repository;

import com.tms.logistica.authservice.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByCodigo(String codigo);
    List<Permission> findByModulo(String modulo);
    boolean existsByCodigo(String codigo);
}

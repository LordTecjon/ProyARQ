package com.tms.logistica.authservice.domain.repository;

import com.tms.logistica.authservice.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}

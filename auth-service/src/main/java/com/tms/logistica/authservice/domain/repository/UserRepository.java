package com.tms.logistica.authservice.domain.repository;

import com.tms.logistica.authservice.domain.entity.User;
import com.tms.logistica.authservice.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);
    Optional<User> findByCorreo(String correo);
    boolean existsByUsername(String username);
    boolean existsByCorreo(String correo);

    @Query("""
        SELECT u FROM User u
        WHERE (:nombre IS NULL OR LOWER(u.nombreCompleto) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:estado IS NULL OR u.estado = :estado)
        """)
    Page<User> findAllWithFilters(@Param("nombre") String nombre,
                                  @Param("estado") UserStatus estado,
                                  Pageable pageable);
}

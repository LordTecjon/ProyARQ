package com.tms.logistica.authservice.domain.repository;

import com.tms.logistica.authservice.domain.entity.AuditEvent;
import com.tms.logistica.authservice.domain.enums.AuditEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    @Query("""
        SELECT ae FROM AuditEvent ae
        WHERE (:userId IS NULL OR ae.usuario.id = :userId)
          AND (:tipoAccion IS NULL OR ae.tipoAccion = :tipoAccion)
          AND (:desde IS NULL OR ae.createdAt >= :desde)
          AND (:hasta IS NULL OR ae.createdAt <= :hasta)
        ORDER BY ae.createdAt DESC
        """)
    Page<AuditEvent> findWithFilters(@Param("userId") UUID userId,
                                     @Param("tipoAccion") AuditEventType tipoAccion,
                                     @Param("desde") LocalDateTime desde,
                                     @Param("hasta") LocalDateTime hasta,
                                     Pageable pageable);
}

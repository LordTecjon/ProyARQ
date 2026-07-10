package com.tms.logistica.authservice.domain.repository;

import com.tms.logistica.authservice.domain.entity.RolePermission;
import com.tms.logistica.authservice.domain.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    @Query("SELECT rp FROM RolePermission rp JOIN FETCH rp.permission WHERE rp.role.id = :rolId")
    List<RolePermission> findByRoleId(@Param("rolId") UUID rolId);

    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.id.rolId = :rolId AND rp.id.permisoId = :permisoId")
    void deleteByRoleIdAndPermissionId(@Param("rolId") UUID rolId, @Param("permisoId") UUID permisoId);
}

package com.tms.logistica.authservice.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class RolePermissionId implements Serializable {
    private UUID rolId;
    private UUID permisoId;
}

package com.tms.logistica.customerservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "auditoria_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Customer customer;

    @Column(name = "campo_modificado", nullable = false, length = 100)
    private String campoModificado;

    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;

    @Column(name = "modificado_por", length = 100)
    private String modificadoPor;

    @CreationTimestamp
    @Column(name = "modified_at", nullable = false, updatable = false)
    private LocalDateTime modifiedAt;
}

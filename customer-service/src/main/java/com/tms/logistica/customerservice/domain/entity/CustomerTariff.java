package com.tms.logistica.customerservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tarifa_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerTariff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Customer customer;

    @Column(name = "tarifa_id", nullable = false)
    private UUID tarifaId;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigentDesde;

    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

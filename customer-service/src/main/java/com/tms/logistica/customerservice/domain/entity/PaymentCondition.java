package com.tms.logistica.customerservice.domain.entity;

import com.tms.logistica.customerservice.domain.enums.BillingType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "condicion_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Customer customer;

    @Column(name = "plazo_dias", nullable = false)
    private Integer plazoDias;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_facturacion", nullable = false, length = 20)
    @Builder.Default
    private BillingType tipoFacturacion = BillingType.CONTADO;

    @Column(name = "linea_credito", precision = 12, scale = 2)
    private BigDecimal lineaCredito;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigentDesde;

    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

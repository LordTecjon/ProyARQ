package com.tms.logistica.billingservice.model.entity;

import com.tms.logistica.billingservice.model.enums.EstadoComprobante;
import com.tms.logistica.billingservice.model.enums.Moneda;
import com.tms.logistica.billingservice.model.enums.TipoComprobante;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comprobante",
        uniqueConstraints = @UniqueConstraint(columnNames = {"serie", "correlativo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36, unique = true, updatable = false)
    @UuidGenerator
    private String uuid;

    @Column(nullable = false, length = 4)
    private String serie;

    @Column(nullable = false, length = 8)
    private String correlativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoComprobante tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoComprobante estado = EstadoComprobante.BORRADOR;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "cliente_nombre", nullable = false, length = 200)
    private String clienteNombre;

    @Column(name = "ot_id")
    private Long otId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    @Builder.Default
    private Moneda moneda = Moneda.PEN;

    @Column(name = "tipo_cambio", precision = 10, scale = 4)
    private BigDecimal tipoCambio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "monto_pagado", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Column(name = "saldo_pendiente", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoPendiente;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "xml_firmado", columnDefinition = "TEXT")
    private String xmlFirmado;

    @Column(name = "cdr_respuesta", columnDefinition = "TEXT")
    private String cdrRespuesta;

    @Column(name = "motivo", length = 300)
    private String motivo;

    @Column(name = "comprobante_origen_id")
    private Long comprobanteOrigenId;

    @Version
    private Integer version;

    @Column(name = "creado_por", nullable = false, length = 100)
    private String creadoPor;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "modificado_en", nullable = false)
    private LocalDateTime modificadoEn;

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LineaComprobante> lineas = new ArrayList<>();

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaPago ASC")
    @Builder.Default
    private List<Pago> pagos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
        this.modificadoEn = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modificadoEn = LocalDateTime.now();
    }
}

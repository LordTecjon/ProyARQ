package com.tms.logistica.costservice.model.entity;

import com.tms.logistica.costservice.model.enums.EstadoCosto;
import com.tms.logistica.costservice.model.enums.TipoServicio;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "costo_transporte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostoTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36, unique = true, updatable = false)
    @UuidGenerator
    private String uuid;

    @Column(name = "orden_id")
    private Long ordenId;

    @Column(name = "costo_estimado", precision = 12, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "costo_real_manual", precision = 12, scale = 2)
    private BigDecimal costoRealManual;

    @Column(name = "ingreso_viaje", precision = 12, scale = 2)
    private BigDecimal ingresoViaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", length = 20)
    private TipoServicio tipoServicio;

    @Column(name = "distancia_km", precision = 12, scale = 2)
    private BigDecimal distanciaKm;

    @Column(name = "peso_kg", precision = 12, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "tarifa_base", precision = 12, scale = 2)
    private BigDecimal tarifaBase;

    @Column(name = "costo_distancia", precision = 12, scale = 2)
    private BigDecimal costoDistancia;

    @Column(name = "costo_peso", precision = 12, scale = 2)
    private BigDecimal costoPeso;

    @Column(name = "recargo_servicio", precision = 12, scale = 2)
    private BigDecimal recargoServicio;

    @Column(name = "subtotal", precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCosto estado = EstadoCosto.COTIZADO;

    @Column(length = 500)
    private String observaciones;

    @OneToMany(mappedBy = "costo", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GastoViaje> gastos = new ArrayList<>();

    @Column(name = "creado_por", nullable = false, length = 100)
    private String creadoPor;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "modificado_en", nullable = false)
    private LocalDateTime modificadoEn;

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

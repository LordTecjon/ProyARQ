package com.tms.logistica.orderservice.model.entity;

import com.tms.logistica.orderservice.model.enums.EstadoOrden;
import com.tms.logistica.orderservice.model.enums.TipoServicio;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orden_transporte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36, unique = true, updatable = false)
    @UuidGenerator
    private String uuid;

    @Column(name = "codigo_orden", nullable = false, length = 20, unique = true)
    private String codigoOrden;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "cliente_nombre", nullable = false, length = 200)
    private String clienteNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false, length = 20)
    private TipoServicio tipoServicio;

    @Column(name = "origen_direccion", nullable = false, length = 300)
    private String origenDireccion;

    @Column(name = "origen_ubigeo", nullable = false, length = 6)
    private String origenUbigeo;

    @Column(name = "destino_direccion", nullable = false, length = 300)
    private String destinoDireccion;

    @Column(name = "destino_ubigeo", nullable = false, length = 6)
    private String destinoUbigeo;

    @Column(name = "fecha_recojo", nullable = false)
    private LocalDate fechaRecojo;

    @Column(name = "fecha_entrega_estimada")
    private LocalDate fechaEntregaEstimada;

    @Column(name = "distancia_km", precision = 12, scale = 2)
    private BigDecimal distanciaKm;

    @Column(name = "peso_total_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal pesoTotalKg;

    @Column(name = "vehiculo_id")
    private Long vehiculoId;

    @Column(name = "conductor_id")
    private Long conductorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoOrden estado = EstadoOrden.PENDIENTE;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "creado_por", nullable = false, length = 100)
    private String creadoPor;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "modificado_en", nullable = false)
    private LocalDateTime modificadoEn;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrdenDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha ASC")
    @Builder.Default
    private List<OrdenTrazabilidad> trazabilidad = new ArrayList<>();

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

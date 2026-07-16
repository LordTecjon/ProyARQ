package com.tms.logistica.orderservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "orden_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    private OrdenTransporte orden;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "peso_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "volumen_m3", precision = 12, scale = 2)
    private BigDecimal volumenM3;
}

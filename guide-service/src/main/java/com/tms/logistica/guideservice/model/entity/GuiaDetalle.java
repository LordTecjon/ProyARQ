package com.tms.logistica.guideservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "guia_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guia_id", nullable = false)
    private GuiaRemision guia;

    @Column(nullable = false)
    private Integer item;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "unidad_medida", nullable = false, length = 3)
    private String unidadMedida;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "peso_bruto_kg", nullable = false, precision = 12, scale = 3)
    private BigDecimal pesoBrutoKg;
}

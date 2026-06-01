package com.tms.logistica.guideservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_guia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaGuia {

    public enum Resultado { OK, ERROR, DENEGADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guia_id")
    private Long guiaId;

    @Column(nullable = false, length = 50)
    private String accion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Resultado resultado;

    @Column(length = 1000)
    private String detalle;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "registrado_en", nullable = false, updatable = false)
    private LocalDateTime registradoEn;

    @PrePersist
    protected void onCreate() {
        this.registradoEn = LocalDateTime.now();
    }
}

package com.tms.logistica.guideservice.model.entity;

import com.tms.logistica.guideservice.model.enums.EstadoGuia;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * GuiaRemision - Entidad principal del Modulo 3
 *
 * representa una Guia de Remision Electronica  emitida ante sunat
 * almacena un snapshot completo de todos los datos al momento de la emision:
 * datos del traslado, remitente, destinatario, conductor y vehiculo
 *
 * el campo uuid es el identificador publico de la guia
 * el campo id es el identificador interno de la bd
 *

 * la relacion con order-service se mantiene por referencia simple
 * sin fk cruzada entre microservicios siguiendo el principio de bajo
 * acoplamiento entre servicios de la arquitectura
 */
@Entity
@Table(name = "guia_remision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaRemision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // uuid generado automaticamente por hibernate como identificador publico
    @Column(nullable = false, length = 36, unique = true, updatable = false)
    @UuidGenerator
    private String uuid;
    // serie de la guia segun el formato sunat: T001 para guias de traslado
    // privado
    @Column(nullable = false, length = 4)
    private String serie;
    // correlativo de 8 digitos con ceros a la izquierda: 00000001, 00000002,
    // etc.
    @Column(nullable = false, length = 8)
    private String correlativo;
    // referencia al order-service. sin fk porque son microservicios
    // independientes.
    @Column(name = "orden_id", nullable = false)
    private Long ordenId;
    // codigo de motivo de traslado segun catalogo de sunat
    @Column(name = "motivo_traslado", nullable = false, length = 6)
    private String motivoTraslado;
    // "01" = transporte publico, "02" = transporte privado
    @Column(nullable = false, length = 2)
    private String modalidad;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    // Datos del remitente
    @Column(name = "remitente_ruc", nullable = false, length = 11)
    private String remitenteRuc;

    @Column(name = "remitente_razon", nullable = false, length = 200)
    private String remitenteRazon;

    @Column(name = "remitente_dir", nullable = false, length = 300)
    private String remitenteDir;
    // Ubigeo de 6 digitos: departamento(2) + provincia(2) + distrito(2)
    @Column(name = "remitente_ubigeo", nullable = false, length = 6)
    private String remitenteUbigeo;

    // Datos del destinatario (quien recibe la mercancia)
    @Column(name = "destinatario_ruc", nullable = false, length = 11)
    private String destinatarioRuc;

    @Column(name = "destinatario_razon", nullable = false, length = 200)
    private String destinatarioRazon;

    @Column(name = "destinatario_dir", nullable = false, length = 300)
    private String destinatarioDir;

    @Column(name = "destinatario_ubigeo", nullable = false, length = 6)
    private String destinatarioUbigeo;

    // Punto de llegada
    @Column(name = "destino_dir", nullable = false, length = 300)
    private String destinoDir;

    @Column(name = "destino_ubigeo", nullable = false, length = 6)
    private String destinoUbigeo;

    // Datos del vehiculo y conductor
    // se guarda un snapshot porque los datos del conductor pueden
    // cambiar
    // en el futuro pero la guia debe reflejar los datos al momento de emision.
    @Column(name = "vehiculo_placa", nullable = false, length = 8)
    private String vehiculoPlaca;

    @Column(name = "conductor_dni", nullable = false, length = 8)
    private String conductorDni;

    @Column(name = "conductor_nombre", nullable = false, length = 200)
    private String conductorNombre;

    @Column(name = "conductor_licencia", nullable = false, length = 12)
    private String conductorLicencia;

    // estado y datos de la respuesta de sunat
    // estado inicial PENDIENTE; cambia segun la respuesta del ose
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoGuia estado = EstadoGuia.PENDIENTE;
    // xml firmado generado por grebuilder
    @Column(name = "xml_firmado", columnDefinition = "LONGTEXT")
    private String xmlFirmado;
    // respuesta json completa del ose
    @Column(name = "cdr_response", columnDefinition = "TEXT")
    private String cdrResponse;
    // codigo cdr de SUNAT: "0" = aceptada, otro codigo = observada o rechazada
    @Column(name = "cdr_codigo", length = 100)
    private String cdrCodigo;
    // descripcion legible del resultado del cdr
    @Column(name = "cdr_descripcion", length = 500)
    private String cdrDescripcion;
    // ruta del pdf generado para futuro almacenamiento en disco
    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    // cola de reenvio automatico
    // cuenta los intentos de envio para limitar los reintentos a 5 como maximo

    @Column(name = "intentos_envio", nullable = false)
    @Builder.Default
    private Integer intentosEnvio = 0;
    // fecha y hora del proximo intento de reenvio (null si no esta en cola)
    @Column(name = "proximo_reenvio")
    private LocalDateTime proximoReenvio;

    // Trazabilidad y auditoria
    @Column(name = "creado_por", nullable = false, length = 100)
    private String creadoPor;
    // updatable = false para que hibernate nunca modifique este campo tras
    // la creacion
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "modificado_en", nullable = false)
    private LocalDateTime modificadoEn;

    @Column(name = "anulado_en")
    private LocalDateTime anuladoEn;

    @Column(name = "anulado_por", length = 100)
    private String anuladoPor;

    @Column(name = "motivo_anulacion", length = 300)
    private String motivoAnulacion;

    // relacion con los bienes transportados
    // CascadeType.ALL las operaciones sobre la guia se propagan a los detalles
    // orphanRemoval = true eliminar un detalle de la lista lo borra de la bd
    @OneToMany(mappedBy = "guia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<GuiaDetalle> detalles = new ArrayList<>();
    // ── Hooks del ciclo de vida JPA ───────────────────────────────────────────

    /**
     * se ejecuta automaticamente antes de la primera insercion en bd
     * establece las fechas de creacion y modificacion
     */

    @PrePersist
    protected void onCreate() {
        this.creadoEn    = LocalDateTime.now();
        this.modificadoEn = LocalDateTime.now();
    }
    /**
     * se ejecuta automaticamente antes de cada actualizacion en bd
     * mantiene actualizada la fecha de ultima modificacion
     */

    @PreUpdate
    protected void onUpdate() {
        this.modificadoEn = LocalDateTime.now();
    }
}

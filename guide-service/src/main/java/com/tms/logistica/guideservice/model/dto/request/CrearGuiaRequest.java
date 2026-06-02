package com.tms.logistica.guideservice.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CrearGuiaRequest {

    @NotNull(message = "el ID de la orden es obligatorio")
    private Long ordenId;

    @NotBlank(message = "el motivo de traslado es obligatorio")
    @Size(max = 6)
    private String motivoTraslado;

    @NotBlank(message = "la modalidad es obligatoria")
    @Pattern(regexp = "01|02", message = "modalidad debe ser 01 público o " +
            "02 privado")
    private String modalidad;

    @NotNull(message = "la fecha de inicio es obligatoria")
    @FutureOrPresent(message = "la fecha de inicio no puede ser pasada")
    private LocalDate fechaInicio;

    // Remitente
    @NotBlank @Size(min = 11, max = 11, message = "el ruc debe tener 11 " +
            "digitos")
    @Pattern(regexp = "\\d{11}")
    private String remitenteRuc;

    @NotBlank @Size(max = 200)
    private String remitenteRazon;

    @NotBlank @Size(max = 300)
    private String remitenteDir;

    @NotBlank @Size(min = 6, max = 6, message = "el ubigeo debe tener 6 " +
            "digitos")
    private String remitenteUbigeo;

    // Destinatario
    @NotBlank @Size(min = 11, max = 11)
    @Pattern(regexp = "\\d{11}")
    private String destinatarioRuc;

    @NotBlank @Size(max = 200)
    private String destinatarioRazon;

    @NotBlank @Size(max = 300)
    private String destinatarioDir;

    @NotBlank @Size(min = 6, max = 6)
    private String destinatarioUbigeo;

    // Destino
    @NotBlank @Size(max = 300)
    private String destinoDir;

    @NotBlank @Size(min = 6, max = 6)
    private String destinoUbigeo;

    // Vehículo y conductor
    @NotBlank @Size(max = 8)
    private String vehiculoPlaca;

    @NotBlank @Size(min = 8, max = 8, message = "el dni debe tener 8 digitos")
    @Pattern(regexp = "\\d{8}")
    private String conductorDni;

    @NotBlank @Size(max = 200)
    private String conductorNombre;

    @NotBlank @Size(max = 12)
    private String conductorLicencia;

    // Bienes
    @NotEmpty(message = "debe incluir al menos un bien transportado")
    @Valid
    private List<DetalleRequest> detalles;

    // ── Inner DTO ──────────────────────────────────────────────
    @Data
    public static class DetalleRequest {

        @NotBlank @Size(max = 500)
        private String descripcion;

        @NotBlank @Size(min = 3, max = 3, message = "codigo uom debe tener 3 " +
                "caracteres")
        private String unidadMedida;

        @NotNull @DecimalMin("0.001")
        private BigDecimal cantidad;

        @NotNull @DecimalMin("0.001")
        private BigDecimal pesoBrutoKg;
    }
}

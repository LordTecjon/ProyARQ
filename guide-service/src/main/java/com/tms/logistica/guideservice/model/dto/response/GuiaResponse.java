package com.tms.logistica.guideservice.model.dto.response;

import com.tms.logistica.guideservice.model.enums.EstadoGuia;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GuiaResponse {

    private Long id;
    private String uuid;
    private String numeroCompleto;
    private Long ordenId;
    private EstadoGuia estado;
    private LocalDate fechaInicio;
    private String motivoTraslado;
    private String modalidad;

    private String remitenteRuc;
    private String remitenteRazon;

    private String destinatarioRuc;
    private String destinatarioRazon;

    private String vehiculoPlaca;
    private String conductorNombre;
    private String conductorLicencia;

    private String cdrCodigo;
    private String cdrDescripcion;
    private String pdfPath;

    private LocalDateTime creadoEn;
    private LocalDateTime modificadoEn;

    private List<DetalleResponse> detalles;

    @Data
    @Builder
    public static class DetalleResponse {
        private Integer item;
        private String descripcion;
        private String unidadMedida;
        private BigDecimal cantidad;
        private BigDecimal pesoBrutoKg;
    }
}

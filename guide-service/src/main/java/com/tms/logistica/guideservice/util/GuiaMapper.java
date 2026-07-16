package com.tms.logistica.guideservice.util;

import com.tms.logistica.guideservice.model.dto.response.GuiaResponse;
import com.tms.logistica.guideservice.model.entity.GuiaRemision;

import java.util.List;
/**
 * GuiaMapper - convierte entidades de BD a DTOs de respuesta
 *
 * Separa la capa de persistencia (entidades JPA) de la capa de presentacion
 * (DTOs expuestos en la API REST) esta separacion garantiza
 *   - Los campos internos de la BD no se expongan accidentalmente en la API
 *   - El modelo de datos interno puede cambiar sin afectar el contrato del API
 *   - Los datos sensibles no se incluyen en la respuesta como xml_firmado
 *
 * Se usa una clase de utilidad final  en lugar de un framework de mapeo como
 * MapStruct para mantener el proyecto simple.
 */
public final class GuiaMapper {

    private GuiaMapper() {}
    /**
     * Convierte una entidad GuiaRemision al DTO GuiaResponse para la API.
     *
     * Campos que se incluyen como datos publicos de la guia, estado, CDR y
     * detalles.
     * Campos que no se incluyen: xmlFirmado, cdrResponse (datos internos
     * sensibles).
     * numeroCompleto se construye concatenando serie y correlativo.
     *
     * @param "g" Entidad de la base de datos
     * @return DTO de respuesta listo para serializar como JSON
     */
    public static GuiaResponse toResponse(GuiaRemision g) {
        List<GuiaResponse.DetalleResponse> detalles = g.getDetalles().stream()
                .map(d -> GuiaResponse.DetalleResponse.builder()
                        .item(d.getItem())
                        .descripcion(d.getDescripcion())
                        .unidadMedida(d.getUnidadMedida())
                        .cantidad(d.getCantidad())
                        .pesoBrutoKg(d.getPesoBrutoKg())
                        .build())
                .toList();

        return GuiaResponse.builder()
                .id(g.getId())
                .uuid(g.getUuid())
                .numeroCompleto(g.getSerie() + "-" + g.getCorrelativo())
                .ordenId(g.getOrdenId())
                .estado(g.getEstado())
                .fechaInicio(g.getFechaInicio())
                .motivoTraslado(g.getMotivoTraslado())
                .modalidad(g.getModalidad())
                .remitenteRuc(g.getRemitenteRuc())
                .remitenteRazon(g.getRemitenteRazon())
                .destinatarioRuc(g.getDestinatarioRuc())
                .destinatarioRazon(g.getDestinatarioRazon())
                .vehiculoPlaca(g.getVehiculoPlaca())
                .conductorNombre(g.getConductorNombre())
                .conductorLicencia(g.getConductorLicencia())
                .cdrCodigo(g.getCdrCodigo())
                .cdrDescripcion(g.getCdrDescripcion())
                .pdfPath(g.getPdfPath())
                .creadoEn(g.getCreadoEn())
                .modificadoEn(g.getModificadoEn())
                .detalles(detalles)
                .build();
    }
}

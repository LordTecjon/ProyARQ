package com.tms.logistica.orderservice.util;

import com.tms.logistica.orderservice.model.dto.request.CrearOrdenDetalleRequest;
import com.tms.logistica.orderservice.model.dto.response.OrdenDetalleResponse;
import com.tms.logistica.orderservice.model.dto.response.OrdenResponse;
import com.tms.logistica.orderservice.model.dto.response.TrazabilidadResponse;
import com.tms.logistica.orderservice.model.entity.OrdenDetalle;
import com.tms.logistica.orderservice.model.entity.OrdenTrazabilidad;
import com.tms.logistica.orderservice.model.entity.OrdenTransporte;

import java.math.BigDecimal;
import java.util.List;

public final class OrdenMapper {

    private OrdenMapper() {
    }

    public static OrdenDetalle toDetalle(CrearOrdenDetalleRequest request, OrdenTransporte orden) {
        return OrdenDetalle.builder()
                .orden(orden)
                .descripcion(request.getDescripcion())
                .cantidad(request.getCantidad())
                .pesoKg(request.getPesoKg())
                .volumenM3(request.getVolumenM3())
                .build();
    }

    public static BigDecimal calcularPesoTotal(List<CrearOrdenDetalleRequest> detalles) {
        return detalles.stream()
                .map(detalle -> detalle.getPesoKg().multiply(BigDecimal.valueOf(detalle.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static OrdenResponse toResponse(OrdenTransporte orden) {
        return OrdenResponse.builder()
                .id(orden.getId())
                .uuid(orden.getUuid())
                .codigoOrden(orden.getCodigoOrden())
                .clienteId(orden.getClienteId())
                .clienteNombre(orden.getClienteNombre())
                .tipoServicio(orden.getTipoServicio())
                .origenDireccion(orden.getOrigenDireccion())
                .origenUbigeo(orden.getOrigenUbigeo())
                .destinoDireccion(orden.getDestinoDireccion())
                .destinoUbigeo(orden.getDestinoUbigeo())
                .fechaRecojo(orden.getFechaRecojo())
                .fechaEntregaEstimada(orden.getFechaEntregaEstimada())
                .distanciaKm(orden.getDistanciaKm())
                .pesoTotalKg(orden.getPesoTotalKg())
                .vehiculoId(orden.getVehiculoId())
                .conductorId(orden.getConductorId())
                .estado(orden.getEstado())
                .observaciones(orden.getObservaciones())
                .creadoEn(orden.getCreadoEn())
                .modificadoEn(orden.getModificadoEn())
                .detalles(orden.getDetalles().stream().map(OrdenMapper::toDetalleResponse).toList())
                .trazabilidad(orden.getTrazabilidad().stream().map(OrdenMapper::toTrazabilidadResponse).toList())
                .build();
    }

    private static OrdenDetalleResponse toDetalleResponse(OrdenDetalle detalle) {
        return OrdenDetalleResponse.builder()
                .id(detalle.getId())
                .descripcion(detalle.getDescripcion())
                .cantidad(detalle.getCantidad())
                .pesoKg(detalle.getPesoKg())
                .volumenM3(detalle.getVolumenM3())
                .build();
    }

    public static TrazabilidadResponse toTrazabilidadResponse(OrdenTrazabilidad trazabilidad) {
        return TrazabilidadResponse.builder()
                .id(trazabilidad.getId())
                .estadoAnterior(trazabilidad.getEstadoAnterior())
                .estadoNuevo(trazabilidad.getEstadoNuevo())
                .accion(trazabilidad.getAccion())
                .observaciones(trazabilidad.getObservaciones())
                .usuario(trazabilidad.getUsuario())
                .fecha(trazabilidad.getFecha())
                .build();
    }
}

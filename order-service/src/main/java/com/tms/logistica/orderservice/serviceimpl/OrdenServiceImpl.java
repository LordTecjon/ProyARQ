package com.tms.logistica.orderservice.serviceimpl;

import com.tms.logistica.orderservice.exception.OrdenException;
import com.tms.logistica.orderservice.model.dto.request.*;
import com.tms.logistica.orderservice.model.dto.response.OrdenResponse;
import com.tms.logistica.orderservice.model.dto.response.TrazabilidadResponse;
import com.tms.logistica.orderservice.model.entity.OrdenTransporte;
import com.tms.logistica.orderservice.model.enums.EstadoOrden;
import com.tms.logistica.orderservice.repository.OrdenTransporteRepository;
import com.tms.logistica.orderservice.repository.OrdenTrazabilidadRepository;
import com.tms.logistica.orderservice.service.OrderStateManager;
import com.tms.logistica.orderservice.service.OrderTraceabilityService;
import com.tms.logistica.orderservice.service.OrdenService;
import com.tms.logistica.orderservice.service.ResourceAssignmentClient;
import com.tms.logistica.orderservice.util.CodigoOrdenGenerator;
import com.tms.logistica.orderservice.util.OrdenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenServiceImpl implements OrdenService {

    private final OrdenTransporteRepository ordenRepository;
    private final OrdenTrazabilidadRepository trazabilidadRepository;
    private final CodigoOrdenGenerator codigoOrdenGenerator;
    private final OrderStateManager orderStateManager;
    private final OrderTraceabilityService traceabilityService;
    private final ResourceAssignmentClient resourceAssignmentClient;

    @Override
    @Transactional
    public OrdenResponse crearOrden(CrearOrdenRequest request, String usuario) {
        resourceAssignmentClient.validarCliente(request.getClienteId());
        OrdenTransporte orden = OrdenTransporte.builder()
                .codigoOrden(generarCodigo())
                .clienteId(request.getClienteId())
                .clienteNombre(request.getClienteNombre())
                .tipoServicio(request.getTipoServicio())
                .origenDireccion(request.getOrigenDireccion())
                .origenUbigeo(request.getOrigenUbigeo())
                .destinoDireccion(request.getDestinoDireccion())
                .destinoUbigeo(request.getDestinoUbigeo())
                .fechaRecojo(request.getFechaRecojo())
                .fechaEntregaEstimada(request.getFechaEntregaEstimada())
                .distanciaKm(request.getDistanciaKm())
                .pesoTotalKg(OrdenMapper.calcularPesoTotal(request.getDetalles()))
                .vehiculoId(request.getVehiculoId())
                .conductorId(request.getConductorId())
                .observaciones(request.getObservaciones())
                .creadoPor(usuario)
                .build();

        request.getDetalles().forEach(detalle -> orden.getDetalles().add(OrdenMapper.toDetalle(detalle, orden)));
        traceabilityService.registrar(orden, null, EstadoOrden.PENDIENTE, "REGISTRO", "Orden registrada", usuario);
        return OrdenMapper.toResponse(ordenRepository.save(orden));
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenResponse obtenerPorCodigo(String codigoOrden) {
        return OrdenMapper.toResponse(buscarPorCodigo(codigoOrden));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenResponse> listarTodas() {
        return ordenRepository.findAll().stream().map(OrdenMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenResponse> listarPorCliente(Long clienteId) {
        return ordenRepository.findByClienteId(clienteId).stream().map(OrdenMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenResponse> listarPorEstado(EstadoOrden estado) {
        return ordenRepository.findByEstado(estado).stream().map(OrdenMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public OrdenResponse actualizarOrden(String codigoOrden, ActualizarOrdenRequest request, String usuario) {
        OrdenTransporte orden = buscarPorCodigo(codigoOrden);
        orderStateManager.validarEditable(orden.getEstado());
        if (request.getClienteNombre() != null) orden.setClienteNombre(request.getClienteNombre());
        if (request.getTipoServicio() != null) orden.setTipoServicio(request.getTipoServicio());
        if (request.getOrigenDireccion() != null) orden.setOrigenDireccion(request.getOrigenDireccion());
        if (request.getOrigenUbigeo() != null) orden.setOrigenUbigeo(request.getOrigenUbigeo());
        if (request.getDestinoDireccion() != null) orden.setDestinoDireccion(request.getDestinoDireccion());
        if (request.getDestinoUbigeo() != null) orden.setDestinoUbigeo(request.getDestinoUbigeo());
        if (request.getFechaRecojo() != null) orden.setFechaRecojo(request.getFechaRecojo());
        if (request.getFechaEntregaEstimada() != null) orden.setFechaEntregaEstimada(request.getFechaEntregaEstimada());
        if (request.getDistanciaKm() != null) orden.setDistanciaKm(request.getDistanciaKm());
        if (request.getObservaciones() != null) orden.setObservaciones(request.getObservaciones());
        traceabilityService.registrar(orden, orden.getEstado(), orden.getEstado(), "EDICION", "Orden editada", usuario);
        return OrdenMapper.toResponse(ordenRepository.save(orden));
    }

    @Override
    @Transactional
    public OrdenResponse asignarVehiculo(String codigoOrden, AsignarVehiculoRequest request, String usuario) {
        OrdenTransporte orden = buscarPorCodigo(codigoOrden);
        orderStateManager.validarAsignable(orden.getEstado());
        resourceAssignmentClient.validarVehiculo(request.getVehiculoId());
        orden.setVehiculoId(request.getVehiculoId());
        traceabilityService.registrar(orden, orden.getEstado(), orden.getEstado(), "ASIGNACION_VEHICULO", request.getObservaciones(), usuario);
        programarSiTieneRecursos(orden, usuario);
        return OrdenMapper.toResponse(ordenRepository.save(orden));
    }

    @Override
    @Transactional
    public OrdenResponse asignarConductor(String codigoOrden, AsignarConductorRequest request, String usuario) {
        OrdenTransporte orden = buscarPorCodigo(codigoOrden);
        orderStateManager.validarAsignable(orden.getEstado());
        resourceAssignmentClient.validarConductor(request.getConductorId());
        orden.setConductorId(request.getConductorId());
        traceabilityService.registrar(orden, orden.getEstado(), orden.getEstado(), "ASIGNACION_CONDUCTOR", request.getObservaciones(), usuario);
        programarSiTieneRecursos(orden, usuario);
        return OrdenMapper.toResponse(ordenRepository.save(orden));
    }

    @Override
    @Transactional
    public OrdenResponse actualizarEstado(String codigoOrden, ActualizarEstadoOrdenRequest request, String usuario) {
        OrdenTransporte orden = buscarPorCodigo(codigoOrden);
        EstadoOrden anterior = orden.getEstado();
        orderStateManager.validarTransicion(anterior, request.getEstado());
        orden.setEstado(request.getEstado());
        if (request.getObservaciones() != null && !request.getObservaciones().isBlank()) {
            orden.setObservaciones(request.getObservaciones());
        }
        traceabilityService.registrar(orden, anterior, request.getEstado(), "CAMBIO_ESTADO", request.getObservaciones(), usuario);
        return OrdenMapper.toResponse(ordenRepository.save(orden));
    }

    @Override
    @Transactional
    public OrdenResponse cerrar(String codigoOrden, String usuario) {
        OrdenTransporte orden = buscarPorCodigo(codigoOrden);
        EstadoOrden anterior = orden.getEstado();
        orderStateManager.validarTransicion(anterior, EstadoOrden.CERRADA);
        orden.setEstado(EstadoOrden.CERRADA);
        traceabilityService.registrar(orden, anterior, EstadoOrden.CERRADA, "CIERRE", "Orden cerrada", usuario);
        return OrdenMapper.toResponse(ordenRepository.save(orden));
    }

    @Override
    @Transactional
    public OrdenResponse anular(String codigoOrden, String motivo, String usuario) {
        OrdenTransporte orden = buscarPorCodigo(codigoOrden);
        if (orden.getEstado() == EstadoOrden.CERRADA) {
            throw new OrdenException("ESTADO_INVALIDO", "No se puede anular una orden cerrada");
        }
        EstadoOrden anterior = orden.getEstado();
        orden.setEstado(EstadoOrden.ANULADA);
        orden.setObservaciones(motivo);
        traceabilityService.registrar(orden, anterior, EstadoOrden.ANULADA, "ANULACION", motivo, usuario);
        return OrdenMapper.toResponse(ordenRepository.save(orden));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrazabilidadResponse> consultarTrazabilidad(String codigoOrden) {
        buscarPorCodigo(codigoOrden);
        return trazabilidadRepository.findByOrdenCodigoOrdenOrderByFechaAsc(codigoOrden).stream()
                .map(OrdenMapper::toTrazabilidadResponse)
                .toList();
    }

    private OrdenTransporte buscarPorCodigo(String codigoOrden) {
        return ordenRepository.findByCodigoOrden(codigoOrden)
                .orElseThrow(() -> new OrdenException("ORDEN_NOT_FOUND", "Orden no encontrada: " + codigoOrden));
    }

    private String generarCodigo() {
        long secuencia = ordenRepository.count() + 1;
        String codigo = codigoOrdenGenerator.generar(secuencia);
        while (ordenRepository.existsByCodigoOrden(codigo)) {
            secuencia++;
            codigo = codigoOrdenGenerator.generar(secuencia);
        }
        return codigo;
    }

    private void programarSiTieneRecursos(OrdenTransporte orden, String usuario) {
        if (orden.getEstado() == EstadoOrden.PENDIENTE
                && orden.getVehiculoId() != null
                && orden.getConductorId() != null) {
            EstadoOrden anterior = orden.getEstado();
            orden.setEstado(EstadoOrden.PROGRAMADA);
            traceabilityService.registrar(orden, anterior, EstadoOrden.PROGRAMADA, "PROGRAMACION", "Orden programada con vehiculo y conductor", usuario);
        }
    }
}

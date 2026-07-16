package com.tms.logistica.orderservice.controller;

import com.tms.logistica.orderservice.model.dto.request.*;
import com.tms.logistica.orderservice.model.dto.response.ApiResponse;
import com.tms.logistica.orderservice.model.dto.response.OrdenResponse;
import com.tms.logistica.orderservice.model.dto.response.TrazabilidadResponse;
import com.tms.logistica.orderservice.model.enums.EstadoOrden;
import com.tms.logistica.orderservice.service.OrdenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdenController {

    private static final String HEADER_USUARIO = "X-Usuario";
    private static final String USUARIO_DEFAULT = "sistema";

    private final OrdenService ordenService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrdenResponse>> crear(
            @Valid @RequestBody CrearOrdenRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("orden creada exitosamente", ordenService.crearOrden(request, usuario)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrdenResponse>> obtener(@PathVariable("id") String codigoOrden) {
        return ResponseEntity.ok(ApiResponse.ok("orden encontrada", ordenService.obtenerPorCodigo(codigoOrden)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrdenResponse>>> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) EstadoOrden estado) {
        List<OrdenResponse> ordenes;
        if (clienteId != null) {
            ordenes = ordenService.listarPorCliente(clienteId);
        } else if (estado != null) {
            ordenes = ordenService.listarPorEstado(estado);
        } else {
            ordenes = ordenService.listarTodas();
        }
        return ResponseEntity.ok(ApiResponse.ok("ordenes encontradas", ordenes));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrdenResponse>> actualizar(
            @PathVariable("id") String codigoOrden,
            @Valid @RequestBody ActualizarOrdenRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("orden actualizada", ordenService.actualizarOrden(codigoOrden, request, usuario)));
    }

    @PatchMapping("/{id}/vehicle")
    public ResponseEntity<ApiResponse<OrdenResponse>> asignarVehiculo(
            @PathVariable("id") String codigoOrden,
            @Valid @RequestBody AsignarVehiculoRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("vehiculo asignado", ordenService.asignarVehiculo(codigoOrden, request, usuario)));
    }

    @PatchMapping("/{id}/driver")
    public ResponseEntity<ApiResponse<OrdenResponse>> asignarConductor(
            @PathVariable("id") String codigoOrden,
            @Valid @RequestBody AsignarConductorRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("conductor asignado", ordenService.asignarConductor(codigoOrden, request, usuario)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrdenResponse>> actualizarEstado(
            @PathVariable("id") String codigoOrden,
            @Valid @RequestBody ActualizarEstadoOrdenRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("estado actualizado", ordenService.actualizarEstado(codigoOrden, request, usuario)));
    }

    @GetMapping("/{id}/traceability")
    public ResponseEntity<ApiResponse<List<TrazabilidadResponse>>> trazabilidad(@PathVariable("id") String codigoOrden) {
        return ResponseEntity.ok(ApiResponse.ok("trazabilidad encontrada", ordenService.consultarTrazabilidad(codigoOrden)));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<OrdenResponse>> cerrar(
            @PathVariable("id") String codigoOrden,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("orden cerrada", ordenService.cerrar(codigoOrden, usuario)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<OrdenResponse>>> historial(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) EstadoOrden estado) {
        return listar(clienteId, estado);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrdenResponse>> anular(
            @PathVariable("id") String codigoOrden,
            @RequestParam String motivo,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("orden anulada", ordenService.anular(codigoOrden, motivo, usuario)));
    }
}

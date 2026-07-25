package com.tms.logistica.costservice.controller;

import com.tms.logistica.costservice.model.dto.request.CalcularCostoRequest;
import com.tms.logistica.costservice.model.dto.request.GastoRequest;
import com.tms.logistica.costservice.model.dto.request.IngresoViajeRequest;
import com.tms.logistica.costservice.model.dto.request.MontoCostoRequest;
import com.tms.logistica.costservice.model.dto.response.ApiResponse;
import com.tms.logistica.costservice.model.dto.response.ComparacionCostoResponse;
import com.tms.logistica.costservice.model.dto.response.CostoResponse;
import com.tms.logistica.costservice.model.dto.response.GastoResponse;
import com.tms.logistica.costservice.model.dto.response.MargenResponse;
import com.tms.logistica.costservice.model.dto.response.ResumenCostoResponse;
import com.tms.logistica.costservice.service.TripCostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trip-costs")
@RequiredArgsConstructor
public class CostoController {

    private static final String HEADER_USUARIO = "X-Usuario";
    private static final String USUARIO_DEFAULT = "sistema";

    private final TripCostService tripCostService;


    @GetMapping
    public ResponseEntity<ApiResponse<List<CostoResponse>>> listarCostos() {
        return ResponseEntity.ok(ApiResponse.ok("costos encontrados", tripCostService.listarCostos()));
    }
    @PostMapping("/quote")
    public ResponseEntity<ApiResponse<CostoResponse>> cotizar(
            @Valid @RequestBody CalcularCostoRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("cotizacion calculada", tripCostService.cotizarCosto(request, usuario)));
    }

    @PostMapping("/{orderId}/estimated")
    public ResponseEntity<ApiResponse<CostoResponse>> registrarEstimado(
            @PathVariable Long orderId,
            @Valid @RequestBody MontoCostoRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("costo estimado registrado", tripCostService.registrarCostoEstimado(orderId, request, usuario)));
    }

    @PostMapping("/{orderId}/actual")
    public ResponseEntity<ApiResponse<CostoResponse>> registrarReal(
            @PathVariable Long orderId,
            @Valid @RequestBody MontoCostoRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("costo real registrado", tripCostService.registrarCostoReal(orderId, request, usuario)));
    }

    @PostMapping("/{orderId}/expenses/fuel")
    public ResponseEntity<ApiResponse<GastoResponse>> registrarCombustible(
            @PathVariable Long orderId,
            @Valid @RequestBody GastoRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("gasto de combustible registrado", tripCostService.registrarCombustible(orderId, request, usuario)));
    }

    @PostMapping("/{orderId}/expenses/tolls")
    public ResponseEntity<ApiResponse<GastoResponse>> registrarPeajes(
            @PathVariable Long orderId,
            @Valid @RequestBody GastoRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("gasto de peajes registrado", tripCostService.registrarPeajes(orderId, request, usuario)));
    }

    @PostMapping("/{orderId}/expenses/allowances")
    public ResponseEntity<ApiResponse<GastoResponse>> registrarViaticos(
            @PathVariable Long orderId,
            @Valid @RequestBody GastoRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("viaticos registrados", tripCostService.registrarViaticos(orderId, request, usuario)));
    }

    @PostMapping("/{orderId}/expenses/others")
    public ResponseEntity<ApiResponse<GastoResponse>> registrarOtros(
            @PathVariable Long orderId,
            @Valid @RequestBody GastoRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("otros gastos registrados", tripCostService.registrarOtrosGastos(orderId, request, usuario)));
    }

    @GetMapping("/{orderId}/summary")
    public ResponseEntity<ApiResponse<ResumenCostoResponse>> resumen(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("resumen de costos", tripCostService.consultarResumen(orderId)));
    }

    @GetMapping("/{orderId}/comparison")
    public ResponseEntity<ApiResponse<ComparacionCostoResponse>> comparacion(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("comparacion de costos", tripCostService.compararCostos(orderId)));
    }

    @GetMapping("/{orderId}/margin")
    public ResponseEntity<ApiResponse<MargenResponse>> margen(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok("margen del viaje", tripCostService.consultarMargen(orderId)));
    }

    @PostMapping("/{orderId}/income")
    public ResponseEntity<ApiResponse<MargenResponse>> registrarIngreso(
            @PathVariable Long orderId,
            @Valid @RequestBody IngresoViajeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("ingreso registrado", tripCostService.registrarIngresoYConsultarMargen(orderId, request)));
    }
}

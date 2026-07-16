package com.tms.logistica.billingservice.controller;

import com.tms.logistica.billingservice.model.dto.request.RegistrarPagoRequest;
import com.tms.logistica.billingservice.model.dto.response.ApiResponse;
import com.tms.logistica.billingservice.model.dto.response.ComprobanteResponse;
import com.tms.logistica.billingservice.model.dto.response.CuentaPorCobrarResponse;
import com.tms.logistica.billingservice.model.dto.response.PagoResponse;
import com.tms.logistica.billingservice.service.CuentaPorCobrarService;
import com.tms.logistica.billingservice.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class PagoController {

    private static final String HEADER_USUARIO = "X-Usuario";
    private static final String USUARIO_DEFAULT = "sistema";

    private final PagoService pagoService;
    private final CuentaPorCobrarService cuentaPorCobrarService;

    // RF7.4 — Registro de pagos totales y parciales
    @PostMapping("/invoices/{id}/payments")
    public ResponseEntity<ApiResponse<ComprobanteResponse>> registrarPago(
            @PathVariable("id") Long comprobanteId,
            @Valid @RequestBody RegistrarPagoRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("pago registrado",
                        pagoService.registrarPago(comprobanteId, request, usuario)));
    }

    @GetMapping("/invoices/{id}/payments")
    public ResponseEntity<ApiResponse<List<PagoResponse>>> listarPagos(
            @PathVariable("id") Long comprobanteId) {
        return ResponseEntity.ok(ApiResponse.ok("pagos encontrados",
                pagoService.listarPagos(comprobanteId)));
    }

    // RF7.5 — Panel de control de Cuentas por Cobrar
    @GetMapping("/receivables")
    public ResponseEntity<ApiResponse<List<CuentaPorCobrarResponse>>> cuentasPorCobrar(
            @RequestParam(required = false) Long clienteId) {
        return ResponseEntity.ok(ApiResponse.ok("cuentas por cobrar",
                cuentaPorCobrarService.listarPendientes(clienteId)));
    }

    // RF7.6 — Vencimientos y alertas de cobranza
    @GetMapping("/receivables/overdue")
    public ResponseEntity<ApiResponse<List<CuentaPorCobrarResponse>>> vencidas() {
        return ResponseEntity.ok(ApiResponse.ok("cuentas vencidas",
                cuentaPorCobrarService.listarVencidas()));
    }
}

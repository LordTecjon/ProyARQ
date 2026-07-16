package com.tms.logistica.billingservice.controller;

import com.tms.logistica.billingservice.model.dto.request.AnularComprobanteRequest;
import com.tms.logistica.billingservice.model.dto.request.EmitirComprobanteRequest;
import com.tms.logistica.billingservice.model.dto.request.EmitirNotaCreditoRequest;
import com.tms.logistica.billingservice.model.dto.response.ApiResponse;
import com.tms.logistica.billingservice.model.dto.response.ComprobanteResponse;
import com.tms.logistica.billingservice.model.dto.response.HistoricoTributarioResponse;
import com.tms.logistica.billingservice.model.enums.EstadoComprobante;
import com.tms.logistica.billingservice.service.ComprobanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing/invoices")
@RequiredArgsConstructor
public class ComprobanteController {

    private static final String HEADER_USUARIO = "X-Usuario";
    private static final String USUARIO_DEFAULT = "sistema";

    private final ComprobanteService comprobanteService;

    // RF7.1 / RF7.2 / RF7.3 — Emision desde OT, envio a SUNAT y notificacion al cliente
    @PostMapping
    public ResponseEntity<ApiResponse<ComprobanteResponse>> emitir(
            @Valid @RequestBody EmitirComprobanteRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("comprobante emitido", comprobanteService.emitir(request, usuario)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComprobanteResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("comprobante encontrado", comprobanteService.obtener(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComprobanteResponse>>> listar(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) EstadoComprobante estado) {
        return ResponseEntity.ok(ApiResponse.ok("comprobantes encontrados",
                comprobanteService.listar(clienteId, estado)));
    }

    // RF7.2 — Reenvio a SUNAT de un comprobante en cola o rechazado
    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<ComprobanteResponse>> reenviar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("comprobante reenviado a SUNAT",
                comprobanteService.reenviarASunat(id)));
    }

    // RF7.3 — Reenvio de la notificacion al cliente
    @PostMapping("/{id}/notify")
    public ResponseEntity<ApiResponse<ComprobanteResponse>> notificar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("cliente notificado",
                comprobanteService.notificarCliente(id)));
    }

    // RF7.7 — Emision de nota de credito electronica
    @PostMapping("/{id}/credit-notes")
    public ResponseEntity<ApiResponse<ComprobanteResponse>> emitirNotaCredito(
            @PathVariable Long id,
            @Valid @RequestBody EmitirNotaCreditoRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("nota de credito emitida",
                        comprobanteService.emitirNotaCredito(id, request, usuario)));
    }

    // RF7.8 — Anulacion de comprobante electronico
    @PatchMapping("/{id}/void")
    public ResponseEntity<ApiResponse<ComprobanteResponse>> anular(
            @PathVariable Long id,
            @Valid @RequestBody AnularComprobanteRequest request,
            @RequestHeader(value = HEADER_USUARIO, defaultValue = USUARIO_DEFAULT) String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("comprobante anulado",
                comprobanteService.anular(id, request, usuario)));
    }

    // RF7.9 — Historico tributario y financiero por cliente
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<HistoricoTributarioResponse>> historico(
            @RequestParam Long clienteId) {
        return ResponseEntity.ok(ApiResponse.ok("historico del cliente",
                comprobanteService.historicoPorCliente(clienteId)));
    }
}

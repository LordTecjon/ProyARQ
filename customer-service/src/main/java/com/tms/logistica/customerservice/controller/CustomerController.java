package com.tms.logistica.customerservice.controller;

import com.tms.logistica.customerservice.domain.enums.CustomerStatus;
import com.tms.logistica.customerservice.dto.request.*;
import com.tms.logistica.customerservice.dto.response.ApiResponse;
import com.tms.logistica.customerservice.dto.response.CustomerDetailResponse;
import com.tms.logistica.customerservice.dto.response.CustomerSummaryResponse;
import com.tms.logistica.customerservice.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Módulo 4 — Gestión de Clientes", description = "CRUD de clientes, condiciones de pago, tarifas e incidencias")
public class CustomerController {

    private final CustomerService customerService;

    // -------------------------------------------------------------------------
    // RF4.1 — Registrar cliente
    // -------------------------------------------------------------------------
    @PostMapping
    @Operation(summary = "RF4.1 — Registrar nuevo cliente")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> create(
            @Valid @RequestBody CreateCustomerRequest request) {
        CustomerDetailResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cliente registrado exitosamente.", response));
    }

    // -------------------------------------------------------------------------
    // RF4.2 — Listar clientes (paginado + filtros)
    // -------------------------------------------------------------------------
    @GetMapping
    @Operation(summary = "RF4.2 — Listar clientes con filtros y paginación")
    public ResponseEntity<ApiResponse<Page<CustomerSummaryResponse>>> list(
            @RequestParam(required = false) String razonSocial,
            @RequestParam(required = false) String ruc,
            @RequestParam(required = false) CustomerStatus estado,
            @PageableDefault(size = 20, sort = "razonSocial", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                customerService.listCustomers(razonSocial, ruc, estado, pageable)));
    }

    // -------------------------------------------------------------------------
    // RF4.3 — Detalle completo
    // -------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(summary = "RF4.3 — Obtener detalle completo del cliente")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getCustomer(id)));
    }

    // -------------------------------------------------------------------------
    // RF4.4 — Editar datos
    // -------------------------------------------------------------------------
    @PutMapping("/{id}")
    @Operation(summary = "RF4.4 — Actualizar datos del cliente (RUC no modificable)")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente actualizado.", customerService.updateCustomer(id, request)));
    }

    // -------------------------------------------------------------------------
    // RF4.5 — Inactivar cliente
    // -------------------------------------------------------------------------
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "RF4.5 — Inactivar cliente")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> deactivate(
            @PathVariable UUID id,
            @Valid @RequestBody DeactivateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente inactivado.", customerService.deactivateCustomer(id, request)));
    }

    // -------------------------------------------------------------------------
    // RF4.7 — Registrar incidencia
    // -------------------------------------------------------------------------
    @PostMapping("/{id}/incidents")
    @Operation(summary = "RF4.7 — Registrar incidencia del cliente")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> registerIncident(
            @PathVariable UUID id,
            @Valid @RequestBody RegisterIncidentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Incidencia registrada.", customerService.registerIncident(id, request)));
    }

    // -------------------------------------------------------------------------
    // RF4.8 — Clientes frecuentes
    // -------------------------------------------------------------------------
    @GetMapping("/frequent")
    @Operation(summary = "RF4.8 — Listar clientes frecuentes")
    public ResponseEntity<ApiResponse<List<CustomerSummaryResponse>>> frequent(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getFrequentCustomers(limit)));
    }

    // -------------------------------------------------------------------------
    // RF4.9 — Condición de pago
    // -------------------------------------------------------------------------
    @PutMapping("/{id}/payment-conditions")
    @Operation(summary = "RF4.9 — Gestionar condición de pago del cliente")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> setPaymentCondition(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentConditionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Condición de pago actualizada.",
                customerService.setPaymentCondition(id, request)));
    }

    // -------------------------------------------------------------------------
    // RF4.10 — Asignar tarifa
    // -------------------------------------------------------------------------
    @PostMapping("/{id}/tariffs")
    @Operation(summary = "RF4.10 — Asignar tarifa comercial al cliente")
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> assignTariff(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTariffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Tarifa asignada.", customerService.assignTariff(id, request)));
    }

    // -------------------------------------------------------------------------
    // Batch — Consulta masiva (para Módulo 7)
    // -------------------------------------------------------------------------
    @PostMapping("/batch")
    @Operation(summary = "Batch — Consultar hasta 500 clientes por ID (uso interno M7)")
    public ResponseEntity<ApiResponse<List<CustomerSummaryResponse>>> batch(
            @Valid @RequestBody BatchCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getBatch(request)));
    }
}

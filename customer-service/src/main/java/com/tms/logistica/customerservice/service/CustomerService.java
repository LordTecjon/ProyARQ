package com.tms.logistica.customerservice.service;

import com.tms.logistica.customerservice.domain.enums.CustomerStatus;
import com.tms.logistica.customerservice.dto.request.*;
import com.tms.logistica.customerservice.dto.response.CustomerDetailResponse;
import com.tms.logistica.customerservice.dto.response.CustomerSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    /** RF4.1 — Registrar cliente */
    CustomerDetailResponse createCustomer(CreateCustomerRequest request);

    /** RF4.2 — Listar clientes con filtros */
    Page<CustomerSummaryResponse> listCustomers(String razonSocial, String ruc, CustomerStatus estado, Pageable pageable);

    /** RF4.3 — Detalle completo del cliente */
    CustomerDetailResponse getCustomer(UUID id);

    /** RF4.4 — Editar datos del cliente */
    CustomerDetailResponse updateCustomer(UUID id, UpdateCustomerRequest request);

    /** RF4.5 — Inactivar cliente */
    CustomerDetailResponse deactivateCustomer(UUID id, DeactivateCustomerRequest request);

    /** RF4.7 — Registrar incidencia */
    CustomerDetailResponse registerIncident(UUID id, RegisterIncidentRequest request);

    /** RF4.8 — Clientes frecuentes */
    List<CustomerSummaryResponse> getFrequentCustomers(int limit);

    /** RF4.9 — Gestionar condición de pago */
    CustomerDetailResponse setPaymentCondition(UUID id, PaymentConditionRequest request);

    /** RF4.10 — Asignar tarifa */
    CustomerDetailResponse assignTariff(UUID id, AssignTariffRequest request);

    /** Batch — consulta masiva para Módulo 7 */
    List<CustomerSummaryResponse> getBatch(BatchCustomerRequest request);
}

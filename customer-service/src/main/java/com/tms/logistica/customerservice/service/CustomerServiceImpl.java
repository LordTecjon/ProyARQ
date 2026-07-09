package com.tms.logistica.customerservice.service;

import com.tms.logistica.customerservice.domain.entity.*;
import com.tms.logistica.customerservice.domain.enums.CustomerStatus;
import com.tms.logistica.customerservice.domain.enums.IncidentStatus;
import com.tms.logistica.customerservice.domain.repository.*;
import com.tms.logistica.customerservice.dto.request.*;
import com.tms.logistica.customerservice.dto.response.CustomerDetailResponse;
import com.tms.logistica.customerservice.dto.response.CustomerSummaryResponse;
import com.tms.logistica.customerservice.exception.CustomerActiveOrdersException;
import com.tms.logistica.customerservice.exception.CustomerNotFoundException;
import com.tms.logistica.customerservice.exception.DuplicateRucException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository         customerRepo;
    private final PaymentConditionRepository paymentRepo;
    private final CustomerTariffRepository   tariffRepo;
    private final CustomerIncidentRepository incidentRepo;
    private final CustomerAuditRepository    auditRepo;

    // -------------------------------------------------------------------------
    // RF4.1 — Registrar cliente
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public CustomerDetailResponse createCustomer(CreateCustomerRequest req) {
        if (customerRepo.existsByRuc(req.getRuc())) {
            throw new DuplicateRucException(req.getRuc());
        }

        String codigo = generateCodigo();

        Customer customer = Customer.builder()
                .codigoCliente(codigo)
                .razonSocial(req.getRazonSocial())
                .ruc(req.getRuc())
                .direccionFiscal(req.getDireccionFiscal())
                .telefono(req.getTelefono())
                .correo(req.getCorreo())
                .personaContacto(req.getPersonaContacto())
                .estado(CustomerStatus.ACTIVO)
                .build();

        customerRepo.save(customer);
        log.info("Cliente creado: {} — RUC {}", codigo, req.getRuc());
        return toDetail(customer);
    }

    // -------------------------------------------------------------------------
    // RF4.2 — Listar con filtros
    // -------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public Page<CustomerSummaryResponse> listCustomers(String razonSocial, String ruc,
                                                       CustomerStatus estado, Pageable pageable) {
        return customerRepo.findAllWithFilters(razonSocial, ruc, estado, pageable)
                .map(this::toSummary);
    }

    // -------------------------------------------------------------------------
    // RF4.3 — Detalle
    // -------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomer(UUID id) {
        Customer c = findOrThrow(id);
        return toDetail(c);
    }

    // -------------------------------------------------------------------------
    // RF4.4 — Editar (RUC no modificable)
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public CustomerDetailResponse updateCustomer(UUID id, UpdateCustomerRequest req) {
        Customer c = findOrThrow(id);

        auditIfChanged(c, "direccionFiscal", c.getDireccionFiscal(), req.getDireccionFiscal(), req.getModificadoPor());
        auditIfChanged(c, "telefono",        c.getTelefono(),        req.getTelefono(),        req.getModificadoPor());
        auditIfChanged(c, "correo",          c.getCorreo(),          req.getCorreo(),          req.getModificadoPor());
        auditIfChanged(c, "personaContacto", c.getPersonaContacto(), req.getPersonaContacto(), req.getModificadoPor());

        if (req.getDireccionFiscal()  != null) c.setDireccionFiscal(req.getDireccionFiscal());
        if (req.getTelefono()         != null) c.setTelefono(req.getTelefono());
        if (req.getCorreo()           != null) c.setCorreo(req.getCorreo());
        if (req.getPersonaContacto()  != null) c.setPersonaContacto(req.getPersonaContacto());

        customerRepo.save(c);
        return toDetail(c);
    }

    // -------------------------------------------------------------------------
    // RF4.5 — Inactivar (no si tiene órdenes activas)
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public CustomerDetailResponse deactivateCustomer(UUID id, DeactivateCustomerRequest req) {
        Customer c = findOrThrow(id);

        if (c.getEstado() == CustomerStatus.INACTIVO) {
            throw new IllegalArgumentException("El cliente ya se encuentra inactivo.");
        }

        // TODO: integrar con order-service para verificar órdenes activas
        // Por ahora la validación se delega al llamador externo.
        // Si el order-service retorna órdenes en estado PENDIENTE/PROGRAMADA/EN_RUTA,
        // se lanza CustomerActiveOrdersException.

        c.setEstado(CustomerStatus.INACTIVO);
        c.setMotivoInactivacion(req.getMotivoInactivacion());
        c.setFechaInactivacion(LocalDate.now());

        auditRepo.save(CustomerAudit.builder()
                .customer(c)
                .campoModificado("estado")
                .valorAnterior("ACTIVO")
                .valorNuevo("INACTIVO")
                .modificadoPor("sistema")
                .build());

        customerRepo.save(c);
        log.info("Cliente inactivado: {} — Motivo: {}", c.getCodigoCliente(), req.getMotivoInactivacion());
        return toDetail(c);
    }

    // -------------------------------------------------------------------------
    // RF4.7 — Registrar incidencia
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public CustomerDetailResponse registerIncident(UUID id, RegisterIncidentRequest req) {
        Customer c = findOrThrow(id);

        CustomerIncident inc = CustomerIncident.builder()
                .customer(c)
                .tipoIncidencia(req.getTipoIncidencia())
                .descripcion(req.getDescripcion())
                .ordenId(req.getOrdenId())
                .estado(IncidentStatus.ACTIVA)
                .registradoPor(req.getRegistradoPor())
                .build();

        incidentRepo.save(inc);
        return toDetail(customerRepo.findById(id).orElseThrow());
    }

    // -------------------------------------------------------------------------
    // RF4.8 — Clientes frecuentes (stub hasta integrar M1)
    // -------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<CustomerSummaryResponse> getFrequentCustomers(int limit) {
        return customerRepo.findFrequentCustomers(PageRequest.of(0, limit))
                .stream().map(this::toSummary).toList();
    }

    // -------------------------------------------------------------------------
    // RF4.9 — Condición de pago
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public CustomerDetailResponse setPaymentCondition(UUID id, PaymentConditionRequest req) {
        Customer c = findOrThrow(id);

        // Cierra la condición actual activa (sin fecha de fin)
        paymentRepo.findFirstByCustomerIdAndVigenteHastaIsNullOrderByVigentDesdeDesc(id)
                .ifPresent(cp -> {
                    cp.setVigenteHasta(req.getVigentDesde().minusDays(1));
                    paymentRepo.save(cp);
                });

        PaymentCondition cond = PaymentCondition.builder()
                .customer(c)
                .plazoDias(req.getPlazoDias())
                .tipoFacturacion(req.getTipoFacturacion())
                .lineaCredito(req.getLineaCredito())
                .vigentDesde(req.getVigentDesde())
                .vigenteHasta(req.getVigenteHasta())
                .build();

        paymentRepo.save(cond);

        auditRepo.save(CustomerAudit.builder()
                .customer(c)
                .campoModificado("condicionPago")
                .valorNuevo("plazo=" + req.getPlazoDias() + " días, tipo=" + req.getTipoFacturacion())
                .build());

        return toDetail(customerRepo.findById(id).orElseThrow());
    }

    // -------------------------------------------------------------------------
    // RF4.10 — Asignar tarifa
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public CustomerDetailResponse assignTariff(UUID id, AssignTariffRequest req) {
        Customer c = findOrThrow(id);

        // Un cliente solo puede tener una tarifa activa por período
        boolean overlap = customerRepo.existsActiveTariffForPeriod(id, req.getVigentDesde());
        if (overlap) {
            throw new IllegalArgumentException(
                    "El cliente ya tiene una tarifa activa en el período solicitado. " +
                    "Finalice la tarifa actual antes de asignar una nueva.");
        }

        CustomerTariff tariff = CustomerTariff.builder()
                .customer(c)
                .tarifaId(req.getTarifaId())
                .vigentDesde(req.getVigentDesde())
                .vigenteHasta(req.getVigenteHasta())
                .build();

        tariffRepo.save(tariff);
        return toDetail(customerRepo.findById(id).orElseThrow());
    }

    // -------------------------------------------------------------------------
    // Batch — Consulta masiva para Módulo 7 (RF batch / QA-Rendimiento M4)
    // -------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public List<CustomerSummaryResponse> getBatch(BatchCustomerRequest req) {
        return customerRepo.findAllByIdIn(req.getIds())
                .stream().map(this::toSummary).toList();
    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    private Customer findOrThrow(UUID id) {
        return customerRepo.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    private String generateCodigo() {
        int next = customerRepo.findMaxCodigoSequence() + 1;
        return String.format("CLI-%05d", next);
    }

    private void auditIfChanged(Customer c, String campo, String anterior, String nuevo, String modificadoPor) {
        if (nuevo != null && !nuevo.equals(anterior)) {
            auditRepo.save(CustomerAudit.builder()
                    .customer(c)
                    .campoModificado(campo)
                    .valorAnterior(anterior)
                    .valorNuevo(nuevo)
                    .modificadoPor(modificadoPor)
                    .build());
        }
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private CustomerSummaryResponse toSummary(Customer c) {
        return CustomerSummaryResponse.builder()
                .id(c.getId())
                .codigoCliente(c.getCodigoCliente())
                .razonSocial(c.getRazonSocial())
                .ruc(c.getRuc())
                .estado(c.getEstado())
                .correo(c.getCorreo())
                .telefono(c.getTelefono())
                .build();
    }

    private CustomerDetailResponse toDetail(Customer c) {
        List<CustomerDetailResponse.PaymentConditionDto> condiciones =
                paymentRepo.findByCustomerIdOrderByVigentDesdeDesc(c.getId())
                        .stream()
                        .map(cp -> CustomerDetailResponse.PaymentConditionDto.builder()
                                .id(cp.getId())
                                .plazoDias(cp.getPlazoDias())
                                .tipoFacturacion(cp.getTipoFacturacion())
                                .lineaCredito(cp.getLineaCredito())
                                .vigentDesde(cp.getVigentDesde())
                                .vigenteHasta(cp.getVigenteHasta())
                                .build())
                        .toList();

        List<CustomerDetailResponse.TariffDto> tarifas =
                tariffRepo.findByCustomerIdOrderByVigentDesdeDesc(c.getId())
                        .stream()
                        .map(t -> CustomerDetailResponse.TariffDto.builder()
                                .id(t.getId())
                                .tarifaId(t.getTarifaId())
                                .vigentDesde(t.getVigentDesde())
                                .vigenteHasta(t.getVigenteHasta())
                                .build())
                        .toList();

        List<CustomerDetailResponse.IncidentDto> incidencias =
                incidentRepo.findByCustomerIdOrderByCreatedAtDesc(c.getId())
                        .stream()
                        .map(i -> CustomerDetailResponse.IncidentDto.builder()
                                .id(i.getId())
                                .tipoIncidencia(i.getTipoIncidencia())
                                .descripcion(i.getDescripcion())
                                .ordenId(i.getOrdenId())
                                .estado(i.getEstado())
                                .registradoPor(i.getRegistradoPor())
                                .createdAt(i.getCreatedAt())
                                .build())
                        .toList();

        return CustomerDetailResponse.builder()
                .id(c.getId())
                .codigoCliente(c.getCodigoCliente())
                .razonSocial(c.getRazonSocial())
                .ruc(c.getRuc())
                .direccionFiscal(c.getDireccionFiscal())
                .telefono(c.getTelefono())
                .correo(c.getCorreo())
                .personaContacto(c.getPersonaContacto())
                .estado(c.getEstado())
                .motivoInactivacion(c.getMotivoInactivacion())
                .fechaInactivacion(c.getFechaInactivacion())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .condicionesPago(condiciones)
                .tarifas(tarifas)
                .incidencias(incidencias)
                .build();
    }
}

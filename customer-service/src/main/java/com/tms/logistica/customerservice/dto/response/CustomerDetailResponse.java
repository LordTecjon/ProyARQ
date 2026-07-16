package com.tms.logistica.customerservice.dto.response;

import com.tms.logistica.customerservice.domain.enums.BillingType;
import com.tms.logistica.customerservice.domain.enums.CustomerStatus;
import com.tms.logistica.customerservice.domain.enums.IncidentStatus;
import com.tms.logistica.customerservice.domain.enums.IncidentType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Vista completa del cliente para RF4.3 */
@Data
@Builder
public class CustomerDetailResponse {

    private UUID id;
    private String codigoCliente;
    private String razonSocial;
    private String ruc;
    private String direccionFiscal;
    private String telefono;
    private String correo;
    private String personaContacto;
    private CustomerStatus estado;
    private String motivoInactivacion;
    private LocalDate fechaInactivacion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<PaymentConditionDto> condicionesPago;
    private List<TariffDto> tarifas;
    private List<IncidentDto> incidencias;

    @Data
    @Builder
    public static class PaymentConditionDto {
        private UUID id;
        private Integer plazoDias;
        private BillingType tipoFacturacion;
        private BigDecimal lineaCredito;
        private LocalDate vigentDesde;
        private LocalDate vigenteHasta;
    }

    @Data
    @Builder
    public static class TariffDto {
        private UUID id;
        private UUID tarifaId;
        private LocalDate vigentDesde;
        private LocalDate vigenteHasta;
    }

    @Data
    @Builder
    public static class IncidentDto {
        private UUID id;
        private IncidentType tipoIncidencia;
        private String descripcion;
        private UUID ordenId;
        private IncidentStatus estado;
        private String registradoPor;
        private LocalDateTime createdAt;
    }
}

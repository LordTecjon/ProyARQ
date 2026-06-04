package com.tms.logistica.customerservice.dto.response;

import com.tms.logistica.customerservice.domain.enums.CustomerStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/** Versión resumida para listados (RF4.2) */
@Data
@Builder
public class CustomerSummaryResponse {
    private UUID id;
    private String codigoCliente;
    private String razonSocial;
    private String ruc;
    private CustomerStatus estado;
    private String correo;
    private String telefono;
}

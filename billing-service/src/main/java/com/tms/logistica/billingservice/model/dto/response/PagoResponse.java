package com.tms.logistica.billingservice.model.dto.response;

import com.tms.logistica.billingservice.model.enums.MedioPago;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PagoResponse {
    private Long id;
    private BigDecimal monto;
    private MedioPago medioPago;
    private LocalDate fechaPago;
    private String numeroOperacion;
    private String registradoPor;
}

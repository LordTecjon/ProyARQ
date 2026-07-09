package com.tms.logistica.customerservice.dto.request;

import com.tms.logistica.customerservice.domain.enums.BillingType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentConditionRequest {

    @NotNull(message = "El plazo de días es obligatorio")
    @Min(value = 0, message = "El plazo no puede ser negativo")
    private Integer plazoDias;

    @NotNull(message = "El tipo de facturación es obligatorio")
    private BillingType tipoFacturacion;

    private BigDecimal lineaCredito;

    @NotNull(message = "La fecha de vigencia es obligatoria")
    private LocalDate vigentDesde;

    private LocalDate vigenteHasta;
}

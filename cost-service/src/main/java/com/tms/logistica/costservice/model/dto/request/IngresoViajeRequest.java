package com.tms.logistica.costservice.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class IngresoViajeRequest {

    @NotNull(message = "El ingreso es obligatorio")
    @DecimalMin(value = "0.00", message = "El ingreso debe ser mayor o igual a cero")
    private BigDecimal ingreso;
}

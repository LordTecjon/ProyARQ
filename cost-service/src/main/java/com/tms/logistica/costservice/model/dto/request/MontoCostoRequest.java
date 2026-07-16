package com.tms.logistica.costservice.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MontoCostoRequest {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto debe ser mayor o igual a cero")
    private BigDecimal monto;

    private String observaciones;
}

package com.tms.logistica.billingservice.model.dto.request;

import com.tms.logistica.billingservice.model.enums.MedioPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RegistrarPagoRequest {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto del pago debe ser mayor a cero")
    private BigDecimal monto;

    @NotNull(message = "El medio de pago es obligatorio")
    private MedioPago medioPago;

    @NotNull(message = "La fecha de pago es obligatoria")
    private LocalDate fechaPago;

    private String numeroOperacion;
}

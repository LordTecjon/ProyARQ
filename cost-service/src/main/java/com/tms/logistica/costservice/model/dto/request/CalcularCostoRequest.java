package com.tms.logistica.costservice.model.dto.request;

import com.tms.logistica.costservice.model.enums.TipoServicio;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CalcularCostoRequest {

    private Long ordenId;

    @NotNull(message = "El tipo de servicio es obligatorio")
    private TipoServicio tipoServicio;

    @NotNull(message = "La distancia es obligatoria")
    @DecimalMin(value = "0.01", message = "La distancia debe ser mayor a cero")
    private BigDecimal distanciaKm;

    @NotNull(message = "El peso es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a cero")
    private BigDecimal pesoKg;

    private String observaciones;
}

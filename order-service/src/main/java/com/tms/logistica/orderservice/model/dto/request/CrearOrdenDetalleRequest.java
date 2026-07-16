package com.tms.logistica.orderservice.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CrearOrdenDetalleRequest {

    @NotBlank(message = "La descripcion del item es obligatoria")
    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "El peso del item es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a cero")
    private BigDecimal pesoKg;

    @DecimalMin(value = "0.00", message = "El volumen no puede ser negativo")
    private BigDecimal volumenM3;
}

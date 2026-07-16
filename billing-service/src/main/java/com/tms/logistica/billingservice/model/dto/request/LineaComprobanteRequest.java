package com.tms.logistica.billingservice.model.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LineaComprobanteRequest {

    @NotBlank(message = "La descripcion de la linea es obligatoria")
    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio unitario no puede ser negativo")
    private BigDecimal precioUnitario;

    private Long otId;
}

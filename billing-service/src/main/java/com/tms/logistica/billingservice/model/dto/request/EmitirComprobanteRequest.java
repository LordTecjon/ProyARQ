package com.tms.logistica.billingservice.model.dto.request;

import com.tms.logistica.billingservice.model.enums.Moneda;
import com.tms.logistica.billingservice.model.enums.TipoComprobante;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class EmitirComprobanteRequest {

    @NotNull(message = "El tipo de comprobante es obligatorio")
    private TipoComprobante tipo;

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String clienteNombre;

    private Long otId;

    @NotNull(message = "La moneda es obligatoria")
    private Moneda moneda;

    @DecimalMin(value = "0.0001", message = "El tipo de cambio debe ser mayor a cero")
    private BigDecimal tipoCambio;

    private LocalDate fechaVencimiento;

    @Valid
    @NotEmpty(message = "El comprobante debe tener al menos una linea")
    private List<LineaComprobanteRequest> lineas;
}

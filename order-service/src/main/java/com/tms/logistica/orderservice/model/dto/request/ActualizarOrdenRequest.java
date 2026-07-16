package com.tms.logistica.orderservice.model.dto.request;

import com.tms.logistica.orderservice.model.enums.TipoServicio;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ActualizarOrdenRequest {
    private String clienteNombre;
    private TipoServicio tipoServicio;
    private String origenDireccion;

    @Size(min = 6, max = 6, message = "El ubigeo de origen debe tener 6 digitos")
    private String origenUbigeo;

    private String destinoDireccion;

    @Size(min = 6, max = 6, message = "El ubigeo de destino debe tener 6 digitos")
    private String destinoUbigeo;

    private LocalDate fechaRecojo;
    private LocalDate fechaEntregaEstimada;

    @DecimalMin(value = "0.00", message = "La distancia no puede ser negativa")
    private BigDecimal distanciaKm;

    private String observaciones;
}

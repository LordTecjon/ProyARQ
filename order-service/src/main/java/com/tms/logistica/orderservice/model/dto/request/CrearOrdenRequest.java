package com.tms.logistica.orderservice.model.dto.request;

import com.tms.logistica.orderservice.model.enums.TipoServicio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CrearOrdenRequest {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String clienteNombre;

    @Email(message = "El correo de contacto no tiene un formato valido")
    private String correoContacto;

    @NotNull(message = "El tipo de servicio es obligatorio")
    private TipoServicio tipoServicio;

    @NotBlank(message = "La direccion de origen es obligatoria")
    private String origenDireccion;

    @NotBlank(message = "El ubigeo de origen es obligatorio")
    @Size(min = 6, max = 6, message = "El ubigeo de origen debe tener 6 digitos")
    private String origenUbigeo;

    @NotBlank(message = "La direccion de destino es obligatoria")
    private String destinoDireccion;

    @NotBlank(message = "El ubigeo de destino es obligatorio")
    @Size(min = 6, max = 6, message = "El ubigeo de destino debe tener 6 digitos")
    private String destinoUbigeo;

    @NotNull(message = "La fecha de recojo es obligatoria")
    private LocalDate fechaRecojo;

    private LocalDate fechaEntregaEstimada;

    @DecimalMin(value = "0.00", message = "La distancia no puede ser negativa")
    private BigDecimal distanciaKm;

    private Long vehiculoId;
    private Long conductorId;
    private String observaciones;

    @Valid
    @NotEmpty(message = "La orden debe tener al menos un item")
    private List<CrearOrdenDetalleRequest> detalles;
}

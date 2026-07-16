package com.tms.logistica.orderservice.model.dto.response;

import com.tms.logistica.orderservice.model.enums.EstadoOrden;
import com.tms.logistica.orderservice.model.enums.TipoServicio;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrdenResponse {
    private Long id;
    private String uuid;
    private String codigoOrden;
    private Long clienteId;
    private String clienteNombre;
    private TipoServicio tipoServicio;
    private String origenDireccion;
    private String origenUbigeo;
    private String destinoDireccion;
    private String destinoUbigeo;
    private LocalDate fechaRecojo;
    private LocalDate fechaEntregaEstimada;
    private BigDecimal distanciaKm;
    private BigDecimal pesoTotalKg;
    private Long vehiculoId;
    private Long conductorId;
    private EstadoOrden estado;
    private String observaciones;
    private LocalDateTime creadoEn;
    private LocalDateTime modificadoEn;
    private List<OrdenDetalleResponse> detalles;
    private List<TrazabilidadResponse> trazabilidad;
}

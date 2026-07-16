package com.tms.logistica.orderservice.service;

import com.tms.logistica.orderservice.model.dto.request.*;
import com.tms.logistica.orderservice.model.dto.response.OrdenResponse;
import com.tms.logistica.orderservice.model.dto.response.TrazabilidadResponse;
import com.tms.logistica.orderservice.model.enums.EstadoOrden;

import java.util.List;

public interface OrdenService {
    OrdenResponse crearOrden(CrearOrdenRequest request, String usuario);
    OrdenResponse obtenerPorCodigo(String codigoOrden);
    List<OrdenResponse> listarTodas();
    List<OrdenResponse> listarPorCliente(Long clienteId);
    List<OrdenResponse> listarPorEstado(EstadoOrden estado);
    OrdenResponse actualizarOrden(String codigoOrden, ActualizarOrdenRequest request, String usuario);
    OrdenResponse asignarVehiculo(String codigoOrden, AsignarVehiculoRequest request, String usuario);
    OrdenResponse asignarConductor(String codigoOrden, AsignarConductorRequest request, String usuario);
    OrdenResponse actualizarEstado(String codigoOrden, ActualizarEstadoOrdenRequest request, String usuario);
    OrdenResponse cerrar(String codigoOrden, String usuario);
    OrdenResponse anular(String codigoOrden, String motivo, String usuario);
    List<TrazabilidadResponse> consultarTrazabilidad(String codigoOrden);
}

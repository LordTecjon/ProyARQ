package com.tms.logistica.costservice.service;

import com.tms.logistica.costservice.model.dto.request.ActualizarEstadoCostoRequest;
import com.tms.logistica.costservice.model.dto.request.CalcularCostoRequest;
import com.tms.logistica.costservice.model.dto.response.CostoResponse;
import com.tms.logistica.costservice.model.enums.EstadoCosto;

import java.util.List;

public interface CostoService {
    CostoResponse calcularCosto(CalcularCostoRequest request, String usuario);
    CostoResponse obtenerPorUuid(String uuid);
    List<CostoResponse> listarTodos();
    List<CostoResponse> listarPorOrden(Long ordenId);
    List<CostoResponse> listarPorEstado(EstadoCosto estado);
    CostoResponse actualizarEstado(String uuid, ActualizarEstadoCostoRequest request);
}

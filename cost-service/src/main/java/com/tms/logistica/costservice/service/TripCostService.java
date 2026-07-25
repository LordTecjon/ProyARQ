package com.tms.logistica.costservice.service;

import com.tms.logistica.costservice.model.dto.request.CalcularCostoRequest;
import com.tms.logistica.costservice.model.dto.request.GastoRequest;
import com.tms.logistica.costservice.model.dto.request.IngresoViajeRequest;
import com.tms.logistica.costservice.model.dto.request.MontoCostoRequest;
import com.tms.logistica.costservice.model.dto.response.ComparacionCostoResponse;
import com.tms.logistica.costservice.model.dto.response.CostoResponse;
import com.tms.logistica.costservice.model.dto.response.GastoResponse;
import com.tms.logistica.costservice.model.dto.response.MargenResponse;
import com.tms.logistica.costservice.model.dto.response.ResumenCostoResponse;

import java.util.List;

public interface TripCostService {
    List<CostoResponse> listarCostos();
    CostoResponse cotizarCosto(CalcularCostoRequest request, String usuario);
    CostoResponse registrarCostoEstimado(Long ordenId, MontoCostoRequest request, String usuario);
    CostoResponse registrarCostoReal(Long ordenId, MontoCostoRequest request, String usuario);
    GastoResponse registrarCombustible(Long ordenId, GastoRequest request, String usuario);
    GastoResponse registrarPeajes(Long ordenId, GastoRequest request, String usuario);
    GastoResponse registrarViaticos(Long ordenId, GastoRequest request, String usuario);
    GastoResponse registrarOtrosGastos(Long ordenId, GastoRequest request, String usuario);
    ResumenCostoResponse consultarResumen(Long ordenId);
    ComparacionCostoResponse compararCostos(Long ordenId);
    MargenResponse consultarMargen(Long ordenId);
    MargenResponse registrarIngresoYConsultarMargen(Long ordenId, IngresoViajeRequest request);
}

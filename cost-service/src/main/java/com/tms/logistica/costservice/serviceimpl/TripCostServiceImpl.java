package com.tms.logistica.costservice.serviceimpl;

import com.tms.logistica.costservice.exception.CostoException;
import com.tms.logistica.costservice.model.dto.request.CalcularCostoRequest;
import com.tms.logistica.costservice.model.dto.request.GastoRequest;
import com.tms.logistica.costservice.model.dto.request.IngresoViajeRequest;
import com.tms.logistica.costservice.model.dto.request.MontoCostoRequest;
import com.tms.logistica.costservice.model.dto.response.ComparacionCostoResponse;
import com.tms.logistica.costservice.model.dto.response.CostoResponse;
import com.tms.logistica.costservice.model.dto.response.GastoResponse;
import com.tms.logistica.costservice.model.dto.response.MargenResponse;
import com.tms.logistica.costservice.model.dto.response.ResumenCostoResponse;
import com.tms.logistica.costservice.model.entity.CostoTransporte;
import com.tms.logistica.costservice.model.entity.GastoViaje;
import com.tms.logistica.costservice.model.enums.EstadoCosto;
import com.tms.logistica.costservice.model.enums.TipoGasto;
import com.tms.logistica.costservice.repository.CostoTransporteRepository;
import com.tms.logistica.costservice.service.CostCalculationService;
import com.tms.logistica.costservice.service.CostIntegrationClient;
import com.tms.logistica.costservice.service.ExpenseRegistryService;
import com.tms.logistica.costservice.service.TripCostService;
import com.tms.logistica.costservice.util.CalculadoraCosto;
import com.tms.logistica.costservice.util.CostoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripCostServiceImpl implements TripCostService {

    private final CostoTransporteRepository costoRepository;
    private final CostIntegrationClient integrationClient;
    private final CostCalculationService calculationService;
    private final ExpenseRegistryService expenseRegistryService;
    private final CalculadoraCosto calculadoraCosto;

    @Override
    @Transactional(readOnly = true)
    public List<CostoResponse> listarCostos() {
        return costoRepository.findAll().stream()
                .map(CostoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CostoResponse cotizarCosto(CalcularCostoRequest request, String usuario) {
        return CostoMapper.toResponse(calculadoraCosto.calcular(request, usuario));
    }

    @Override
    @Transactional
    public CostoResponse registrarCostoEstimado(Long ordenId, MontoCostoRequest request, String usuario) {
        CostoTransporte costo = obtenerOCrearCosto(ordenId, usuario);
        costo.setCostoEstimado(request.getMonto());
        costo.setObservaciones(request.getObservaciones());
        return CostoMapper.toResponse(costoRepository.save(costo));
    }

    @Override
    @Transactional
    public CostoResponse registrarCostoReal(Long ordenId, MontoCostoRequest request, String usuario) {
        CostoTransporte costo = obtenerOCrearCosto(ordenId, usuario);
        costo.setCostoRealManual(request.getMonto());
        costo.setObservaciones(request.getObservaciones());
        return CostoMapper.toResponse(costoRepository.save(costo));
    }

    @Override
    @Transactional
    public GastoResponse registrarCombustible(Long ordenId, GastoRequest request, String usuario) {
        return registrarGasto(ordenId, TipoGasto.COMBUSTIBLE, request, usuario);
    }

    @Override
    @Transactional
    public GastoResponse registrarPeajes(Long ordenId, GastoRequest request, String usuario) {
        return registrarGasto(ordenId, TipoGasto.PEAJE, request, usuario);
    }

    @Override
    @Transactional
    public GastoResponse registrarViaticos(Long ordenId, GastoRequest request, String usuario) {
        return registrarGasto(ordenId, TipoGasto.VIATICO, request, usuario);
    }

    @Override
    @Transactional
    public GastoResponse registrarOtrosGastos(Long ordenId, GastoRequest request, String usuario) {
        return registrarGasto(ordenId, TipoGasto.OTRO, request, usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCostoResponse consultarResumen(Long ordenId) {
        CostoTransporte costo = buscarPorOrden(ordenId);
        BigDecimal costoReal = calculationService.calcularCostoReal(costo);
        BigDecimal diferencia = costo.getCostoEstimado() == null ? null : costoReal.subtract(costo.getCostoEstimado());
        BigDecimal margen = costo.getIngresoViaje() == null ? null : costo.getIngresoViaje().subtract(costoReal);
        return ResumenCostoResponse.builder()
                .ordenId(ordenId)
                .costoEstimado(costo.getCostoEstimado())
                .costoReal(costoReal)
                .combustible(calculationService.sumarPorTipo(costo.getGastos(), TipoGasto.COMBUSTIBLE))
                .peajes(calculationService.sumarPorTipo(costo.getGastos(), TipoGasto.PEAJE))
                .viaticos(calculationService.sumarPorTipo(costo.getGastos(), TipoGasto.VIATICO))
                .otrosGastos(calculationService.sumarPorTipo(costo.getGastos(), TipoGasto.OTRO))
                .diferencia(diferencia)
                .ingreso(costo.getIngresoViaje())
                .margen(margen)
                .gastos(costo.getGastos().stream().map(CostoMapper::toGastoResponse).toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ComparacionCostoResponse compararCostos(Long ordenId) {
        CostoTransporte costo = buscarPorOrden(ordenId);
        return ComparacionCostoResponse.builder()
                .ordenId(ordenId)
                .costoEstimado(costo.getCostoEstimado())
                .costoReal(calculationService.calcularCostoReal(costo))
                .diferencia(calculationService.calcularDiferencia(costo))
                .porcentajeDesviacion(calculationService.calcularPorcentajeDesviacion(costo))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MargenResponse consultarMargen(Long ordenId) {
        CostoTransporte costo = buscarPorOrden(ordenId);
        if (costo.getIngresoViaje() == null) {
            BigDecimal ingreso = integrationClient.obtenerIngresoFacturado(ordenId);
            costo.setIngresoViaje(ingreso);
        }
        return margenResponse(costo);
    }

    @Override
    @Transactional
    public MargenResponse registrarIngresoYConsultarMargen(Long ordenId, IngresoViajeRequest request) {
        CostoTransporte costo = buscarPorOrden(ordenId);
        costo.setIngresoViaje(request.getIngreso());
        return margenResponse(costoRepository.save(costo));
    }

    private GastoResponse registrarGasto(Long ordenId, TipoGasto tipoGasto, GastoRequest request, String usuario) {
        CostoTransporte costo = obtenerOCrearCosto(ordenId, usuario);
        GastoViaje gasto = expenseRegistryService.crearGasto(costo, tipoGasto, request, usuario);
        costo.getGastos().add(gasto);
        costoRepository.save(costo);
        return CostoMapper.toGastoResponse(gasto);
    }

    private CostoTransporte obtenerOCrearCosto(Long ordenId, String usuario) {
        integrationClient.validarOrden(ordenId);
        return costoRepository.findFirstByOrdenId(ordenId)
                .orElseGet(() -> CostoTransporte.builder()
                        .ordenId(ordenId)
                        .estado(EstadoCosto.COTIZADO)
                        .creadoPor(usuario)
                        .build());
    }

    private CostoTransporte buscarPorOrden(Long ordenId) {
        return costoRepository.findFirstByOrdenId(ordenId)
                .orElseThrow(() -> new CostoException("COSTO_NOT_FOUND", "No existen costos para la orden " + ordenId));
    }

    private MargenResponse margenResponse(CostoTransporte costo) {
        return MargenResponse.builder()
                .ordenId(costo.getOrdenId())
                .ingreso(costo.getIngresoViaje())
                .costoReal(calculationService.calcularCostoReal(costo))
                .margen(calculationService.calcularMargen(costo))
                .porcentajeMargen(calculationService.calcularPorcentajeMargen(costo))
                .build();
    }
}

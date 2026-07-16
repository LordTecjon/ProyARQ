package com.tms.logistica.costservice.serviceimpl;

import com.tms.logistica.costservice.exception.CostoException;
import com.tms.logistica.costservice.model.dto.request.ActualizarEstadoCostoRequest;
import com.tms.logistica.costservice.model.dto.request.CalcularCostoRequest;
import com.tms.logistica.costservice.model.dto.response.CostoResponse;
import com.tms.logistica.costservice.model.entity.CostoTransporte;
import com.tms.logistica.costservice.model.enums.EstadoCosto;
import com.tms.logistica.costservice.repository.CostoTransporteRepository;
import com.tms.logistica.costservice.service.CostoService;
import com.tms.logistica.costservice.util.CalculadoraCosto;
import com.tms.logistica.costservice.util.CostoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CostoServiceImpl implements CostoService {

    private final CostoTransporteRepository costoRepository;
    private final CalculadoraCosto calculadoraCosto;

    @Override
    @Transactional
    public CostoResponse calcularCosto(CalcularCostoRequest request, String usuario) {
        CostoTransporte costo = calculadoraCosto.calcular(request, usuario);
        return CostoMapper.toResponse(costoRepository.save(costo));
    }

    @Override
    @Transactional(readOnly = true)
    public CostoResponse obtenerPorUuid(String uuid) {
        return CostoMapper.toResponse(buscarPorUuid(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostoResponse> listarTodos() {
        return costoRepository.findAll().stream().map(CostoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostoResponse> listarPorOrden(Long ordenId) {
        return costoRepository.findByOrdenId(ordenId).stream().map(CostoMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CostoResponse> listarPorEstado(EstadoCosto estado) {
        return costoRepository.findByEstado(estado).stream().map(CostoMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public CostoResponse actualizarEstado(String uuid, ActualizarEstadoCostoRequest request) {
        CostoTransporte costo = buscarPorUuid(uuid);
        if (costo.getEstado() != EstadoCosto.COTIZADO) {
            throw new CostoException("ESTADO_INVALIDO", "Solo se puede actualizar una cotizacion pendiente");
        }
        costo.setEstado(request.getEstado());
        if (request.getObservaciones() != null && !request.getObservaciones().isBlank()) {
            costo.setObservaciones(request.getObservaciones());
        }
        return CostoMapper.toResponse(costoRepository.save(costo));
    }

    private CostoTransporte buscarPorUuid(String uuid) {
        return costoRepository.findByUuid(uuid)
                .orElseThrow(() -> new CostoException("COSTO_NOT_FOUND", "Costo no encontrado: " + uuid));
    }
}

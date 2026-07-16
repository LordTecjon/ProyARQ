package com.tms.logistica.billingservice.serviceimpl;

import com.tms.logistica.billingservice.exception.BillingException;
import com.tms.logistica.billingservice.model.dto.request.RegistrarPagoRequest;
import com.tms.logistica.billingservice.model.dto.response.ComprobanteResponse;
import com.tms.logistica.billingservice.model.dto.response.PagoResponse;
import com.tms.logistica.billingservice.model.entity.Comprobante;
import com.tms.logistica.billingservice.model.entity.Pago;
import com.tms.logistica.billingservice.repository.ComprobanteRepository;
import com.tms.logistica.billingservice.repository.PagoRepository;
import com.tms.logistica.billingservice.service.ComprobanteEstadoManager;
import com.tms.logistica.billingservice.service.PagoService;
import com.tms.logistica.billingservice.util.ComprobanteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final ComprobanteRepository comprobanteRepository;
    private final PagoRepository pagoRepository;
    private final ComprobanteEstadoManager estadoManager;

    @Override
    @Transactional
    public ComprobanteResponse registrarPago(Long comprobanteId, RegistrarPagoRequest request, String usuario) {
        Comprobante comprobante = comprobanteRepository.findById(comprobanteId)
                .orElseThrow(() -> new BillingException("COMPROBANTE_NOT_FOUND",
                        "Comprobante no encontrado: " + comprobanteId));
        estadoManager.validarPagable(comprobante.getEstado());

        if (request.getMonto().compareTo(comprobante.getSaldoPendiente()) > 0) {
            throw new BillingException("PAGO_INVALIDO",
                    "El monto del pago excede el saldo pendiente (" + comprobante.getSaldoPendiente() + ")");
        }

        Pago pago = Pago.builder()
                .comprobante(comprobante)
                .monto(request.getMonto())
                .medioPago(request.getMedioPago())
                .fechaPago(request.getFechaPago())
                .numeroOperacion(request.getNumeroOperacion())
                .registradoPor(usuario)
                .build();
        comprobante.getPagos().add(pago);
        comprobante.setMontoPagado(comprobante.getMontoPagado().add(request.getMonto()));
        comprobante.setSaldoPendiente(comprobante.getTotal().subtract(comprobante.getMontoPagado()));

        return ComprobanteMapper.toResponse(comprobanteRepository.save(comprobante));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPagos(Long comprobanteId) {
        if (!comprobanteRepository.existsById(comprobanteId)) {
            throw new BillingException("COMPROBANTE_NOT_FOUND", "Comprobante no encontrado: " + comprobanteId);
        }
        return pagoRepository.findByComprobanteId(comprobanteId).stream()
                .map(ComprobanteMapper::toPagoResponse)
                .toList();
    }
}

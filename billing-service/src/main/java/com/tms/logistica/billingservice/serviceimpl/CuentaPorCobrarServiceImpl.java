package com.tms.logistica.billingservice.serviceimpl;

import com.tms.logistica.billingservice.model.dto.response.CuentaPorCobrarResponse;
import com.tms.logistica.billingservice.model.entity.Comprobante;
import com.tms.logistica.billingservice.model.enums.EstadoComprobante;
import com.tms.logistica.billingservice.repository.ComprobanteRepository;
import com.tms.logistica.billingservice.service.CuentaPorCobrarService;
import com.tms.logistica.billingservice.util.ComprobanteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaPorCobrarServiceImpl implements CuentaPorCobrarService {

    private final ComprobanteRepository comprobanteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CuentaPorCobrarResponse> listarPendientes(Long clienteId) {
        return comprobantesConSaldo().stream()
                .filter(c -> clienteId == null || clienteId.equals(c.getClienteId()))
                .map(ComprobanteMapper::toCuentaPorCobrar)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaPorCobrarResponse> listarVencidas() {
        LocalDate hoy = LocalDate.now();
        return comprobantesConSaldo().stream()
                .filter(c -> c.getFechaVencimiento() != null && hoy.isAfter(c.getFechaVencimiento()))
                .map(ComprobanteMapper::toCuentaPorCobrar)
                .toList();
    }

    private List<Comprobante> comprobantesConSaldo() {
        return comprobanteRepository.findAll().stream()
                .filter(c -> c.getEstado() == EstadoComprobante.ACEPTADO
                        || c.getEstado() == EstadoComprobante.ACEPTADO_OBS)
                .filter(c -> c.getSaldoPendiente() != null
                        && c.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }
}

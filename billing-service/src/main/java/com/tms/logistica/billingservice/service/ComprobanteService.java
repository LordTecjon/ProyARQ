package com.tms.logistica.billingservice.service;

import com.tms.logistica.billingservice.model.dto.request.AnularComprobanteRequest;
import com.tms.logistica.billingservice.model.dto.request.EmitirComprobanteRequest;
import com.tms.logistica.billingservice.model.dto.request.EmitirNotaCreditoRequest;
import com.tms.logistica.billingservice.model.dto.response.ComprobanteResponse;
import com.tms.logistica.billingservice.model.dto.response.HistoricoTributarioResponse;
import com.tms.logistica.billingservice.model.enums.EstadoComprobante;

import java.util.List;

public interface ComprobanteService {
    ComprobanteResponse emitir(EmitirComprobanteRequest request, String usuario);
    ComprobanteResponse obtener(Long id);
    List<ComprobanteResponse> listar(Long clienteId, EstadoComprobante estado);
    ComprobanteResponse reenviarASunat(Long id);
    ComprobanteResponse notificarCliente(Long id);
    ComprobanteResponse emitirNotaCredito(Long id, EmitirNotaCreditoRequest request, String usuario);
    ComprobanteResponse anular(Long id, AnularComprobanteRequest request, String usuario);
    HistoricoTributarioResponse historicoPorCliente(Long clienteId);
}

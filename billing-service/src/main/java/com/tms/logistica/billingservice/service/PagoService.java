package com.tms.logistica.billingservice.service;

import com.tms.logistica.billingservice.model.dto.request.RegistrarPagoRequest;
import com.tms.logistica.billingservice.model.dto.response.ComprobanteResponse;
import com.tms.logistica.billingservice.model.dto.response.PagoResponse;

import java.util.List;

public interface PagoService {
    ComprobanteResponse registrarPago(Long comprobanteId, RegistrarPagoRequest request, String usuario);
    List<PagoResponse> listarPagos(Long comprobanteId);
}

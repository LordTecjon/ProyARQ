package com.tms.logistica.billingservice.service;

import com.tms.logistica.billingservice.model.dto.response.CuentaPorCobrarResponse;

import java.util.List;

public interface CuentaPorCobrarService {
    List<CuentaPorCobrarResponse> listarPendientes(Long clienteId);
    List<CuentaPorCobrarResponse> listarVencidas();
}

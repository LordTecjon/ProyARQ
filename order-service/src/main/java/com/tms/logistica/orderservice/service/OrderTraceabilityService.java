package com.tms.logistica.orderservice.service;

import com.tms.logistica.orderservice.model.entity.OrdenTrazabilidad;
import com.tms.logistica.orderservice.model.entity.OrdenTransporte;
import com.tms.logistica.orderservice.model.enums.EstadoOrden;
import org.springframework.stereotype.Component;

@Component
public class OrderTraceabilityService {

    public void registrar(OrdenTransporte orden, EstadoOrden anterior, EstadoOrden nuevo,
                          String accion, String observaciones, String usuario) {
        orden.getTrazabilidad().add(OrdenTrazabilidad.builder()
                .orden(orden)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .accion(accion)
                .observaciones(observaciones)
                .usuario(usuario)
                .build());
    }
}

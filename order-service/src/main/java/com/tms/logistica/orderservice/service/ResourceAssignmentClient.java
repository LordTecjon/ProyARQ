package com.tms.logistica.orderservice.service;

import com.tms.logistica.orderservice.exception.OrdenException;
import org.springframework.stereotype.Component;

@Component
public class ResourceAssignmentClient {

    public void validarCliente(Long clienteId) {
        if (clienteId == null || clienteId <= 0) {
            throw new OrdenException("RECURSO_INVALIDO", "Cliente invalido");
        }
    }

    public void validarVehiculo(Long vehiculoId) {
        if (vehiculoId == null || vehiculoId <= 0) {
            throw new OrdenException("RECURSO_INVALIDO", "Vehiculo invalido o no disponible");
        }
    }

    public void validarConductor(Long conductorId) {
        if (conductorId == null || conductorId <= 0) {
            throw new OrdenException("RECURSO_INVALIDO", "Conductor invalido o no disponible");
        }
    }
}

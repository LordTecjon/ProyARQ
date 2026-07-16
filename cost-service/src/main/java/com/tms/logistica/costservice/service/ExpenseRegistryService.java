package com.tms.logistica.costservice.service;

import com.tms.logistica.costservice.model.dto.request.GastoRequest;
import com.tms.logistica.costservice.model.entity.CostoTransporte;
import com.tms.logistica.costservice.model.entity.GastoViaje;
import com.tms.logistica.costservice.model.enums.TipoGasto;
import org.springframework.stereotype.Component;

@Component
public class ExpenseRegistryService {

    public GastoViaje crearGasto(CostoTransporte costo, TipoGasto tipoGasto, GastoRequest request, String usuario) {
        return GastoViaje.builder()
                .costo(costo)
                .tipoGasto(tipoGasto)
                .monto(request.getMonto())
                .concepto(request.getConcepto())
                .observaciones(request.getObservaciones())
                .registradoPor(usuario)
                .build();
    }
}

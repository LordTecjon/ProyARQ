package com.tms.logistica.costservice.util;

import com.tms.logistica.costservice.model.dto.response.CostoResponse;
import com.tms.logistica.costservice.model.dto.response.GastoResponse;
import com.tms.logistica.costservice.model.entity.GastoViaje;
import com.tms.logistica.costservice.model.entity.CostoTransporte;

public final class CostoMapper {

    private CostoMapper() {
    }

    public static CostoResponse toResponse(CostoTransporte costo) {
        return CostoResponse.builder()
                .id(costo.getId())
                .uuid(costo.getUuid())
                .ordenId(costo.getOrdenId())
                .tipoServicio(costo.getTipoServicio())
                .distanciaKm(costo.getDistanciaKm())
                .pesoKg(costo.getPesoKg())
                .tarifaBase(costo.getTarifaBase())
                .costoDistancia(costo.getCostoDistancia())
                .costoPeso(costo.getCostoPeso())
                .recargoServicio(costo.getRecargoServicio())
                .subtotal(costo.getSubtotal())
                .igv(costo.getIgv())
                .total(costo.getTotal())
                .estado(costo.getEstado())
                .observaciones(costo.getObservaciones())
                .creadoEn(costo.getCreadoEn())
                .modificadoEn(costo.getModificadoEn())
                .build();
    }

    public static GastoResponse toGastoResponse(GastoViaje gasto) {
        return GastoResponse.builder()
                .id(gasto.getId())
                .tipoGasto(gasto.getTipoGasto())
                .monto(gasto.getMonto())
                .concepto(gasto.getConcepto())
                .observaciones(gasto.getObservaciones())
                .registradoPor(gasto.getRegistradoPor())
                .registradoEn(gasto.getRegistradoEn())
                .build();
    }
}

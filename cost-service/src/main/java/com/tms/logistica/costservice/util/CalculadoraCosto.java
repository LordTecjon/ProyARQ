package com.tms.logistica.costservice.util;

import com.tms.logistica.costservice.model.dto.request.CalcularCostoRequest;
import com.tms.logistica.costservice.model.entity.CostoTransporte;
import com.tms.logistica.costservice.model.enums.TipoServicio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CalculadoraCosto {

    private static final BigDecimal TARIFA_BASE = new BigDecimal("80.00");
    private static final BigDecimal TARIFA_KM = new BigDecimal("2.50");
    private static final BigDecimal TARIFA_KG = new BigDecimal("0.35");
    private static final BigDecimal IGV = new BigDecimal("0.18");

    public CostoTransporte calcular(CalcularCostoRequest request, String usuario) {
        BigDecimal costoDistancia = money(request.getDistanciaKm().multiply(TARIFA_KM));
        BigDecimal costoPeso = money(request.getPesoKg().multiply(TARIFA_KG));
        BigDecimal baseOperativa = TARIFA_BASE.add(costoDistancia).add(costoPeso);
        BigDecimal recargo = money(baseOperativa.multiply(factorRecargo(request.getTipoServicio())));
        BigDecimal subtotal = money(baseOperativa.add(recargo));
        BigDecimal igv = money(subtotal.multiply(IGV));
        BigDecimal total = money(subtotal.add(igv));

        return CostoTransporte.builder()
                .ordenId(request.getOrdenId())
                .tipoServicio(request.getTipoServicio())
                .distanciaKm(request.getDistanciaKm())
                .pesoKg(request.getPesoKg())
                .tarifaBase(TARIFA_BASE)
                .costoDistancia(costoDistancia)
                .costoPeso(costoPeso)
                .recargoServicio(recargo)
                .subtotal(subtotal)
                .igv(igv)
                .total(total)
                .observaciones(request.getObservaciones())
                .creadoPor(usuario)
                .build();
    }

    private BigDecimal factorRecargo(TipoServicio tipoServicio) {
        return switch (tipoServicio) {
            case LOCAL -> new BigDecimal("0.00");
            case NACIONAL -> new BigDecimal("0.08");
            case EXPRESS -> new BigDecimal("0.20");
            case REFRIGERADO -> new BigDecimal("0.25");
        };
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

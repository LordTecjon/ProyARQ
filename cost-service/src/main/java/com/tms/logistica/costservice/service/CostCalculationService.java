package com.tms.logistica.costservice.service;

import com.tms.logistica.costservice.exception.CostoException;
import com.tms.logistica.costservice.model.entity.CostoTransporte;
import com.tms.logistica.costservice.model.entity.GastoViaje;
import com.tms.logistica.costservice.model.enums.TipoGasto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class CostCalculationService {

    public BigDecimal calcularCostoReal(CostoTransporte costo) {
        if (costo.getCostoRealManual() != null) {
            return money(costo.getCostoRealManual());
        }
        return money(costo.getGastos().stream()
                .map(GastoViaje::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal sumarPorTipo(List<GastoViaje> gastos, TipoGasto tipoGasto) {
        return money(gastos.stream()
                .filter(gasto -> gasto.getTipoGasto() == tipoGasto)
                .map(GastoViaje::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal calcularDiferencia(CostoTransporte costo) {
        validarComparacion(costo);
        return money(calcularCostoReal(costo).subtract(costo.getCostoEstimado()));
    }

    public BigDecimal calcularPorcentajeDesviacion(CostoTransporte costo) {
        validarComparacion(costo);
        if (costo.getCostoEstimado().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return money(calcularDiferencia(costo)
                .multiply(BigDecimal.valueOf(100))
                .divide(costo.getCostoEstimado(), 2, RoundingMode.HALF_UP));
    }

    public BigDecimal calcularMargen(CostoTransporte costo) {
        validarMargen(costo);
        return money(costo.getIngresoViaje().subtract(calcularCostoReal(costo)));
    }

    public BigDecimal calcularPorcentajeMargen(CostoTransporte costo) {
        validarMargen(costo);
        if (costo.getIngresoViaje().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return money(calcularMargen(costo)
                .multiply(BigDecimal.valueOf(100))
                .divide(costo.getIngresoViaje(), 2, RoundingMode.HALF_UP));
    }

    private void validarComparacion(CostoTransporte costo) {
        if (costo.getCostoEstimado() == null || calcularCostoReal(costo) == null) {
            throw new CostoException("CALCULO_INCOMPLETO", "Debe existir costo estimado y costo real");
        }
    }

    private void validarMargen(CostoTransporte costo) {
        if (costo.getIngresoViaje() == null) {
            throw new CostoException("CALCULO_INCOMPLETO", "Debe existir un ingreso asociado al viaje");
        }
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

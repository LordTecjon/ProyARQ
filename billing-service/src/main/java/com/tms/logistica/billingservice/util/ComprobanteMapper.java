package com.tms.logistica.billingservice.util;

import com.tms.logistica.billingservice.model.dto.request.LineaComprobanteRequest;
import com.tms.logistica.billingservice.model.dto.response.ComprobanteResponse;
import com.tms.logistica.billingservice.model.dto.response.CuentaPorCobrarResponse;
import com.tms.logistica.billingservice.model.dto.response.LineaComprobanteResponse;
import com.tms.logistica.billingservice.model.dto.response.PagoResponse;
import com.tms.logistica.billingservice.model.entity.Comprobante;
import com.tms.logistica.billingservice.model.entity.LineaComprobante;
import com.tms.logistica.billingservice.model.entity.Pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class ComprobanteMapper {

    private ComprobanteMapper() {
    }

    public static LineaComprobante toLinea(LineaComprobanteRequest request, Comprobante comprobante) {
        return LineaComprobante.builder()
                .comprobante(comprobante)
                .descripcion(request.getDescripcion())
                .cantidad(request.getCantidad())
                .precioUnitario(request.getPrecioUnitario())
                .subtotalLinea(BigDecimal.ZERO)
                .otId(request.getOtId())
                .build();
    }

    public static ComprobanteResponse toResponse(Comprobante c) {
        return ComprobanteResponse.builder()
                .id(c.getId())
                .uuid(c.getUuid())
                .serie(c.getSerie())
                .correlativo(c.getCorrelativo())
                .tipo(c.getTipo())
                .estado(c.getEstado())
                .clienteId(c.getClienteId())
                .clienteNombre(c.getClienteNombre())
                .otId(c.getOtId())
                .moneda(c.getMoneda())
                .tipoCambio(c.getTipoCambio())
                .subtotal(c.getSubtotal())
                .igv(c.getIgv())
                .total(c.getTotal())
                .montoPagado(c.getMontoPagado())
                .saldoPendiente(c.getSaldoPendiente())
                .fechaEmision(c.getFechaEmision())
                .fechaVencimiento(c.getFechaVencimiento())
                .cdrRespuesta(c.getCdrRespuesta())
                .motivo(c.getMotivo())
                .comprobanteOrigenId(c.getComprobanteOrigenId())
                .creadoEn(c.getCreadoEn())
                .modificadoEn(c.getModificadoEn())
                .lineas(c.getLineas().stream().map(ComprobanteMapper::toLineaResponse).toList())
                .pagos(c.getPagos().stream().map(ComprobanteMapper::toPagoResponse).toList())
                .build();
    }

    public static LineaComprobanteResponse toLineaResponse(LineaComprobante l) {
        return LineaComprobanteResponse.builder()
                .id(l.getId())
                .descripcion(l.getDescripcion())
                .cantidad(l.getCantidad())
                .precioUnitario(l.getPrecioUnitario())
                .subtotalLinea(l.getSubtotalLinea())
                .otId(l.getOtId())
                .build();
    }

    public static PagoResponse toPagoResponse(Pago p) {
        return PagoResponse.builder()
                .id(p.getId())
                .monto(p.getMonto())
                .medioPago(p.getMedioPago())
                .fechaPago(p.getFechaPago())
                .numeroOperacion(p.getNumeroOperacion())
                .registradoPor(p.getRegistradoPor())
                .build();
    }

    public static CuentaPorCobrarResponse toCuentaPorCobrar(Comprobante c) {
        LocalDate hoy = LocalDate.now();
        long diasVencido = 0;
        boolean vencido = false;
        if (c.getFechaVencimiento() != null && hoy.isAfter(c.getFechaVencimiento())) {
            diasVencido = ChronoUnit.DAYS.between(c.getFechaVencimiento(), hoy);
            vencido = true;
        }
        return CuentaPorCobrarResponse.builder()
                .comprobanteId(c.getId())
                .serie(c.getSerie())
                .correlativo(c.getCorrelativo())
                .clienteId(c.getClienteId())
                .clienteNombre(c.getClienteNombre())
                .estado(c.getEstado())
                .total(c.getTotal())
                .montoPagado(c.getMontoPagado())
                .saldoPendiente(c.getSaldoPendiente())
                .fechaEmision(c.getFechaEmision())
                .fechaVencimiento(c.getFechaVencimiento())
                .diasVencido(diasVencido)
                .vencido(vencido)
                .build();
    }
}

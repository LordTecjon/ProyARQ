package com.tms.logistica.billingservice.util;

import com.tms.logistica.billingservice.model.enums.TipoComprobante;
import org.springframework.stereotype.Component;

/**
 * Genera la serie y el correlativo del comprobante segun su tipo.
 * Serie: FACTURA=F001, BOLETA=B001, NOTA_CREDITO=FC01.
 */
@Component
public class CorrelativoGenerator {

    public String serieDe(TipoComprobante tipo) {
        return switch (tipo) {
            case FACTURA -> "F001";
            case BOLETA -> "B001";
            case NOTA_CREDITO -> "FC01";
        };
    }

    public String correlativoDe(long secuencia) {
        return String.format("%08d", secuencia);
    }
}

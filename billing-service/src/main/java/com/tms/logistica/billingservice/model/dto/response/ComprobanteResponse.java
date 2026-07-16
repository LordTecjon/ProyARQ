package com.tms.logistica.billingservice.model.dto.response;

import com.tms.logistica.billingservice.model.enums.EstadoComprobante;
import com.tms.logistica.billingservice.model.enums.Moneda;
import com.tms.logistica.billingservice.model.enums.TipoComprobante;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ComprobanteResponse {
    private Long id;
    private String uuid;
    private String serie;
    private String correlativo;
    private TipoComprobante tipo;
    private EstadoComprobante estado;
    private Long clienteId;
    private String clienteNombre;
    private Long otId;
    private Moneda moneda;
    private BigDecimal tipoCambio;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private BigDecimal montoPagado;
    private BigDecimal saldoPendiente;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String cdrRespuesta;
    private String motivo;
    private Long comprobanteOrigenId;
    private LocalDateTime creadoEn;
    private LocalDateTime modificadoEn;
    private List<LineaComprobanteResponse> lineas;
    private List<PagoResponse> pagos;
}

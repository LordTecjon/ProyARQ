package com.tms.logistica.billingservice.serviceimpl;

import com.tms.logistica.billingservice.exception.BillingException;
import com.tms.logistica.billingservice.model.dto.request.AnularComprobanteRequest;
import com.tms.logistica.billingservice.model.dto.request.EmitirComprobanteRequest;
import com.tms.logistica.billingservice.model.dto.request.EmitirNotaCreditoRequest;
import com.tms.logistica.billingservice.model.dto.response.ComprobanteResponse;
import com.tms.logistica.billingservice.model.dto.response.HistoricoTributarioResponse;
import com.tms.logistica.billingservice.model.entity.Comprobante;
import com.tms.logistica.billingservice.model.entity.LineaComprobante;
import com.tms.logistica.billingservice.model.enums.EstadoComprobante;
import com.tms.logistica.billingservice.model.enums.TipoComprobante;
import com.tms.logistica.billingservice.repository.ComprobanteRepository;
import com.tms.logistica.billingservice.service.*;
import com.tms.logistica.billingservice.util.ComprobanteMapper;
import com.tms.logistica.billingservice.util.CorrelativoGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprobanteServiceImpl implements ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final CalculoTributarioService calculoTributarioService;
    private final ComprobanteEstadoManager estadoManager;
    private final ComprobanteXmlGenerator xmlGenerator;
    private final DigitalSignatureService firmaService;
    private final SunatGatewayClient sunatGatewayClient;
    private final NotificacionClienteService notificacionService;
    private final CorrelativoGenerator correlativoGenerator;

    @Override
    @Transactional
    public ComprobanteResponse emitir(EmitirComprobanteRequest request, String usuario) {
        Comprobante comprobante = Comprobante.builder()
                .tipo(request.getTipo())
                .clienteId(request.getClienteId())
                .clienteNombre(request.getClienteNombre())
                .otId(request.getOtId())
                .moneda(request.getMoneda())
                .tipoCambio(request.getTipoCambio())
                .fechaEmision(LocalDate.now())
                .fechaVencimiento(request.getFechaVencimiento())
                .montoPagado(BigDecimal.ZERO)
                .creadoPor(usuario)
                .build();

        asignarSerieYCorrelativo(comprobante);
        request.getLineas().forEach(l -> comprobante.getLineas().add(ComprobanteMapper.toLinea(l, comprobante)));
        calculoTributarioService.aplicarCalculos(comprobante);
        emitirYEnviar(comprobante);
        return ComprobanteMapper.toResponse(comprobanteRepository.save(comprobante));
    }

    @Override
    @Transactional(readOnly = true)
    public ComprobanteResponse obtener(Long id) {
        return ComprobanteMapper.toResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComprobanteResponse> listar(Long clienteId, EstadoComprobante estado) {
        List<Comprobante> comprobantes;
        if (clienteId != null) {
            comprobantes = comprobanteRepository.findByClienteId(clienteId);
        } else if (estado != null) {
            comprobantes = comprobanteRepository.findByEstado(estado);
        } else {
            comprobantes = comprobanteRepository.findAll();
        }
        return comprobantes.stream().map(ComprobanteMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ComprobanteResponse reenviarASunat(Long id) {
        Comprobante comprobante = buscar(id);
        estadoManager.validarReenviable(comprobante.getEstado());
        enviarASunat(comprobante);
        return ComprobanteMapper.toResponse(comprobanteRepository.save(comprobante));
    }

    @Override
    @Transactional
    public ComprobanteResponse notificarCliente(Long id) {
        Comprobante comprobante = buscar(id);
        if (comprobante.getEstado() != EstadoComprobante.ACEPTADO
                && comprobante.getEstado() != EstadoComprobante.ACEPTADO_OBS) {
            throw new BillingException("ESTADO_INVALIDO",
                    "Solo se notifican comprobantes aceptados por SUNAT");
        }
        notificacionService.notificar(comprobante);
        return ComprobanteMapper.toResponse(comprobante);
    }

    @Override
    @Transactional
    public ComprobanteResponse emitirNotaCredito(Long id, EmitirNotaCreditoRequest request, String usuario) {
        Comprobante origen = buscar(id);
        estadoManager.validarNotaCreditoAplicable(origen);

        Comprobante nota = Comprobante.builder()
                .tipo(TipoComprobante.NOTA_CREDITO)
                .clienteId(origen.getClienteId())
                .clienteNombre(origen.getClienteNombre())
                .otId(origen.getOtId())
                .moneda(origen.getMoneda())
                .tipoCambio(origen.getTipoCambio())
                .fechaEmision(LocalDate.now())
                .montoPagado(BigDecimal.ZERO)
                .motivo(request.getMotivo())
                .comprobanteOrigenId(origen.getId())
                .creadoPor(usuario)
                .build();

        asignarSerieYCorrelativo(nota);
        origen.getLineas().forEach(l -> nota.getLineas().add(LineaComprobante.builder()
                .comprobante(nota)
                .descripcion(l.getDescripcion())
                .cantidad(l.getCantidad())
                .precioUnitario(l.getPrecioUnitario())
                .subtotalLinea(BigDecimal.ZERO)
                .otId(l.getOtId())
                .build()));
        calculoTributarioService.aplicarCalculos(nota);
        emitirYEnviar(nota);
        return ComprobanteMapper.toResponse(comprobanteRepository.save(nota));
    }

    @Override
    @Transactional
    public ComprobanteResponse anular(Long id, AnularComprobanteRequest request, String usuario) {
        Comprobante comprobante = buscar(id);
        estadoManager.validarAnulable(comprobante.getEstado());
        comprobante.setEstado(EstadoComprobante.ANULADO);
        comprobante.setMotivo(request.getMotivo());
        return ComprobanteMapper.toResponse(comprobanteRepository.save(comprobante));
    }

    @Override
    @Transactional(readOnly = true)
    public HistoricoTributarioResponse historicoPorCliente(Long clienteId) {
        List<Comprobante> comprobantes = comprobanteRepository.findByClienteId(clienteId);
        BigDecimal totalFacturado = BigDecimal.ZERO;
        BigDecimal totalIgv = BigDecimal.ZERO;
        BigDecimal totalPagado = BigDecimal.ZERO;
        BigDecimal saldoPendiente = BigDecimal.ZERO;
        for (Comprobante c : comprobantes) {
            if (c.getEstado() == EstadoComprobante.ANULADO) {
                continue;
            }
            totalFacturado = totalFacturado.add(c.getTotal());
            totalIgv = totalIgv.add(c.getIgv());
            totalPagado = totalPagado.add(c.getMontoPagado());
            saldoPendiente = saldoPendiente.add(c.getSaldoPendiente());
        }
        return HistoricoTributarioResponse.builder()
                .clienteId(clienteId)
                .totalComprobantes(comprobantes.size())
                .totalFacturado(totalFacturado)
                .totalIgv(totalIgv)
                .totalPagado(totalPagado)
                .saldoPendiente(saldoPendiente)
                .comprobantes(comprobantes.stream().map(ComprobanteMapper::toResponse).toList())
                .build();
    }

    /** Genera y firma el XML, luego lo envia a SUNAT. */
    private void emitirYEnviar(Comprobante comprobante) {
        String xml = xmlGenerator.generar(comprobante);
        comprobante.setXmlFirmado(firmaService.firmar(xml));
        enviarASunat(comprobante);
        if (comprobante.getEstado() == EstadoComprobante.ACEPTADO
                || comprobante.getEstado() == EstadoComprobante.ACEPTADO_OBS) {
            notificacionService.notificar(comprobante);
        }
    }

    private void enviarASunat(Comprobante comprobante) {
        SunatGatewayClient.ResultadoEnvio resultado = sunatGatewayClient.enviar(comprobante);
        comprobante.setEstado(resultado.estado());
        comprobante.setCdrRespuesta(resultado.cdr());
    }

    private void asignarSerieYCorrelativo(Comprobante comprobante) {
        String serie = correlativoGenerator.serieDe(comprobante.getTipo());
        long secuencia = comprobanteRepository.countByTipo(comprobante.getTipo()) + 1;
        String correlativo = correlativoGenerator.correlativoDe(secuencia);
        while (comprobanteRepository.existsBySerieAndCorrelativo(serie, correlativo)) {
            secuencia++;
            correlativo = correlativoGenerator.correlativoDe(secuencia);
        }
        comprobante.setSerie(serie);
        comprobante.setCorrelativo(correlativo);
    }

    private Comprobante buscar(Long id) {
        return comprobanteRepository.findById(id)
                .orElseThrow(() -> new BillingException("COMPROBANTE_NOT_FOUND",
                        "Comprobante no encontrado: " + id));
    }
}

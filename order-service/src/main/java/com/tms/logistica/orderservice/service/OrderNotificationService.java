package com.tms.logistica.orderservice.service;

import com.tms.logistica.orderservice.model.dto.request.CrearOrdenRequest;
import com.tms.logistica.orderservice.model.dto.response.OrdenResponse;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderNotificationService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Async
    public void notificarOrdenCreada(OrdenResponse orden, CrearOrdenRequest request) {
        String destino = request.getCorreoContacto();
        if (destino == null || destino.isBlank()) {
            log.info("NOTIFICACION_OMITIDA - La orden {} no tiene correo de contacto", orden.getCodigoOrden());
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("NOTIFICACION_SIMULADA - Orden {} para {}. Configure SMTP para enviar correo real.", orden.getCodigoOrden(), destino);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(destino);
            helper.setSubject("Orden de transporte registrada - " + orden.getCodigoOrden());
            helper.setText(htmlOrden(orden), true);
            mailSender.send(message);
            log.info("NOTIFICACION_ENVIADA - Orden {} enviada a {}", orden.getCodigoOrden(), destino);
        } catch (Exception ex) {
            log.warn("NOTIFICACION_FALLIDA - La orden {} se creo, pero el correo no pudo enviarse: {}", orden.getCodigoOrden(), ex.getMessage());
        }
    }
    @Async
    public void notificarPagoRegistrado(OrdenResponse orden, String destino, String montoPagado, String comprobante) {
        if (destino == null || destino.isBlank()) {
            log.info("NOTIFICACION_PAGO_OMITIDA - La orden {} no tiene correo de contacto", orden.getCodigoOrden());
            return;
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("NOTIFICACION_PAGO_SIMULADA - Orden {} pagada para {}. Configure SMTP para enviar correo real.", orden.getCodigoOrden(), destino);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(destino);
            helper.setSubject("Pago registrado - " + orden.getCodigoOrden());
            helper.setText(htmlPago(orden, montoPagado, comprobante), true);
            mailSender.send(message);
            log.info("NOTIFICACION_PAGO_ENVIADA - Orden {} enviada a {}", orden.getCodigoOrden(), destino);
        } catch (Exception ex) {
            log.warn("NOTIFICACION_PAGO_FALLIDA - La orden {} fue pagada, pero el correo no pudo enviarse: {}", orden.getCodigoOrden(), ex.getMessage());
        }
    }

    private String htmlPago(OrdenResponse orden, String montoPagado, String comprobante) {
        return """
                <div style='font-family:Arial,sans-serif;background:#f3f6fa;padding:24px;color:#142033'>
                  <div style='max-width:640px;margin:auto;background:white;border-radius:12px;border:1px solid #d8e1eb;overflow:hidden'>
                    <div style='background:#14804a;color:white;padding:20px 24px'>
                      <h2 style='margin:0'>Pago registrado correctamente</h2>
                      <p style='margin:6px 0 0;color:#dcfce7'>Transport Management System</p>
                    </div>
                    <div style='padding:24px'>
                      <p>Hola, se registró el pago de la siguiente orden de transporte:</p>
                      <table style='width:100%%;border-collapse:collapse'>
                        <tr><td style='padding:10px;border-bottom:1px solid #e5edf5'><b>Código OT</b></td><td style='padding:10px;border-bottom:1px solid #e5edf5'>%s</td></tr>
                        <tr><td style='padding:10px;border-bottom:1px solid #e5edf5'><b>Cliente</b></td><td style='padding:10px;border-bottom:1px solid #e5edf5'>%s</td></tr>
                        <tr><td style='padding:10px;border-bottom:1px solid #e5edf5'><b>Comprobante</b></td><td style='padding:10px;border-bottom:1px solid #e5edf5'>%s</td></tr>
                        <tr><td style='padding:10px;border-bottom:1px solid #e5edf5'><b>Monto pagado</b></td><td style='padding:10px;border-bottom:1px solid #e5edf5'>S/ %s</td></tr>
                        <tr><td style='padding:10px'><b>Nuevo estado</b></td><td style='padding:10px'>PROGRAMADA</td></tr>
                      </table>
                    </div>
                  </div>
                </div>
                """.formatted(orden.getCodigoOrden(), orden.getClienteNombre(), comprobante, montoPagado);
    }
    private String htmlOrden(OrdenResponse orden) {
        return """
                <div style='font-family:Arial,sans-serif;background:#f3f6fa;padding:24px;color:#142033'>
                  <div style='max-width:640px;margin:auto;background:white;border-radius:12px;border:1px solid #d8e1eb;overflow:hidden'>
                    <div style='background:#0f3a5f;color:white;padding:20px 24px'>
                      <h2 style='margin:0'>Orden de transporte registrada</h2>
                      <p style='margin:6px 0 0;color:#d8eaff'>Transport Management System</p>
                    </div>
                    <div style='padding:24px'>
                      <p>Hola, se registró correctamente la siguiente orden:</p>
                      <table style='width:100%%;border-collapse:collapse'>
                        <tr><td style='padding:10px;border-bottom:1px solid #e5edf5'><b>Código OT</b></td><td style='padding:10px;border-bottom:1px solid #e5edf5'>%s</td></tr>
                        <tr><td style='padding:10px;border-bottom:1px solid #e5edf5'><b>Cliente</b></td><td style='padding:10px;border-bottom:1px solid #e5edf5'>%s</td></tr>
                        <tr><td style='padding:10px;border-bottom:1px solid #e5edf5'><b>Servicio</b></td><td style='padding:10px;border-bottom:1px solid #e5edf5'>%s</td></tr>
                        <tr><td style='padding:10px;border-bottom:1px solid #e5edf5'><b>Origen</b></td><td style='padding:10px;border-bottom:1px solid #e5edf5'>%s</td></tr>
                        <tr><td style='padding:10px;border-bottom:1px solid #e5edf5'><b>Destino</b></td><td style='padding:10px;border-bottom:1px solid #e5edf5'>%s</td></tr>
                        <tr><td style='padding:10px'><b>Estado</b></td><td style='padding:10px'>%s</td></tr>
                      </table>
                      <p style='margin-top:20px;color:#64748b;font-size:13px'>Este correo fue enviado automáticamente después de crear la orden.</p>
                    </div>
                  </div>
                </div>
                """.formatted(
                orden.getCodigoOrden(),
                orden.getClienteNombre(),
                orden.getTipoServicio(),
                orden.getOrigenDireccion(),
                orden.getDestinoDireccion(),
                orden.getEstado()
        );
    }
}
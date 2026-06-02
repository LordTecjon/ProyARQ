package com.tms.logistica.guideservice.service;

import com.itextpdf.html2pdf.HtmlConverter;
import com.tms.logistica.guideservice.model.entity.GuiaRemision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * PdfGuiaService — Genera el PDF de la Guía de Remisión Electrónica
 * con código QR de verificación SUNAT.
 *
 * RF3.5: El PDF incluye todos los datos de la guía y un QR
 * apuntando al portal de verificación de sunat.
 *
 * usa html + conversión a bytes como representación del PDF.
 * en producción se reemplaza por iText o jasperReports.
 */
@Component
@Slf4j
public class PdfGuiaService {

    private static final String QR_BASE_URL =
            "https://e-beta.sunat.gob.pe/ol-ti-itconsulta/consulta";

    /**
     * genera el contenido html del pdf de la guia.
     * retorna los bytes del archivo.
     */

    public byte[] generarPdf(GuiaRemision guia) {
        log.info("generando pdf para guia {}-{}", guia.getSerie(),
                guia.getCorrelativo());

        String qrUrl = String.format("%s?ruc=%s&tipo=09&serie=%s&numero=%s",
                QR_BASE_URL, guia.getRemitenteRuc(), guia.getSerie(), guia.getCorrelativo());

        String html = construirHtml(guia, qrUrl);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, baos);
        return baos.toByteArray();
    }

    /**
     * nombre del archivo segun formato sunat: GRE-{serie}-{correlativo}.pdf
     */
    public String nombreArchivo(GuiaRemision guia) {
        return String.format("GRE-%s-%s.pdf", guia.getSerie(), guia.getCorrelativo());
    }

    // Construcción del HTML
    private String construirHtml(GuiaRemision guia, String qrUrl) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaEmision = guia.getFechaInicio().format(fmt);
        String fechaCreacion = guia.getCreadoEn() != null
                ? guia.getCreadoEn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "-";

        StringBuilder detalles = new StringBuilder();
        guia.getDetalles().forEach(d -> detalles.append(String.format("""
                <tr>
                    <td>%d</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td style="text-align:right">%s</td>
                    <td style="text-align:right">%s kg</td>
                </tr>
                """,
                d.getItem(), d.getDescripcion(), d.getUnidadMedida(),
                d.getCantidad().toPlainString(), d.getPesoBrutoKg().toPlainString())));

        return String.format("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8"/>
                    <title>GRE-%s-%s</title>
                    <style>
                        body { font-family: Arial, sans-serif; font-size: 11px; margin: 20px; color: #222; }
                        .header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 10px; margin-bottom: 15px; }
                        .header h1 { font-size: 16px; margin: 0; }
                        .header h2 { font-size: 13px; margin: 4px 0; color: #555; }
                        .numero { font-size: 14px; font-weight: bold; color: #c00; }
                        .section { margin-bottom: 12px; }
                        .section-title { font-weight: bold; background: #f0f0f0; padding: 3px 6px; margin-bottom: 5px; }
                        .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
                        .field label { font-weight: bold; display: block; color: #555; }
                        table { width: 100%%; border-collapse: collapse; }
                        th { background: #333; color: white; padding: 5px; text-align: left; }
                        td { padding: 4px 5px; border-bottom: 1px solid #ddd; }
                        .estado { display: inline-block; padding: 3px 10px; border-radius: 3px; font-weight: bold; }
                        .ACEPTADA { background: #d4edda; color: #155724; }
                        .RECHAZADA { background: #f8d7da; color: #721c24; }
                        .PENDIENTE { background: #fff3cd; color: #856404; }
                        .qr-section { text-align: center; margin-top: 20px; border-top: 1px dashed #999; padding-top: 10px; }
                        .footer { font-size: 9px; color: #999; text-align: center; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>GUÍA DE REMISIÓN ELECTRÓNICA</h1>
                        <h2>%s</h2>
                        <div class="numero">%s - %s</div>
                        <div>Estado: <span class="estado %s">%s</span></div>
                    </div>

                    <div class="section">
                        <div class="section-title">DATOS DEL TRASLADO</div>
                        <div class="grid">
                            <div class="field"><label>Fecha de emisión</label>%s</div>
                            <div class="field"><label>Fecha de inicio</label>%s</div>
                            <div class="field"><label>Motivo de traslado</label>%s</div>
                            <div class="field"><label>Modalidad</label>%s</div>
                        </div>
                    </div>

                    <div class="section">
                        <div class="section-title">REMITENTE</div>
                        <div class="grid">
                            <div class="field"><label>RUC</label>%s</div>
                            <div class="field"><label>Razón Social</label>%s</div>
                            <div class="field"><label>Dirección</label>%s</div>
                            <div class="field"><label>Ubigeo</label>%s</div>
                        </div>
                    </div>

                    <div class="section">
                        <div class="section-title">DESTINATARIO</div>
                        <div class="grid">
                            <div class="field"><label>RUC</label>%s</div>
                            <div class="field"><label>Razón Social</label>%s</div>
                            <div class="field"><label>Dirección destino</label>%s</div>
                            <div class="field"><label>Ubigeo destino</label>%s</div>
                        </div>
                    </div>

                    <div class="section">
                        <div class="section-title">TRANSPORTISTA</div>
                        <div class="grid">
                            <div class="field"><label>Placa</label>%s</div>
                            <div class="field"><label>Conductor</label>%s</div>
                            <div class="field"><label>DNI Conductor</label>%s</div>
                            <div class="field"><label>Licencia</label>%s</div>
                        </div>
                    </div>

                    <div class="section">
                        <div class="section-title">BIENES TRANSPORTADOS</div>
                        <table>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Descripción</th>
                                    <th>U.M.</th>
                                    <th>Cantidad</th>
                                    <th>Peso Bruto</th>
                                </tr>
                            </thead>
                            <tbody>%s</tbody>
                        </table>
                    </div>

                    <div class="qr-section">
                        <p><strong>Verificar en SUNAT:</strong></p>
                        <p style="font-size:9px; word-break:break-all;">%s</p>
                        <p style="font-size:9px;">CDR: %s — %s</p>
                    </div>

                    <div class="footer">
                        Documento generado el %s — Sistema TMS Guide Service
                    </div>
                </body>
                </html>
                """,
                guia.getSerie(), guia.getCorrelativo(),
                guia.getRemitenteRazon(),
                guia.getSerie(), guia.getCorrelativo(),
                guia.getEstado().name(), guia.getEstado().name(),
                fechaCreacion, fechaEmision,
                guia.getMotivoTraslado(),
                "01".equals(guia.getModalidad()) ? "01 - Público" : "02 - Privado",
                guia.getRemitenteRuc(), guia.getRemitenteRazon(),
                guia.getRemitenteDir(), guia.getRemitenteUbigeo(),
                guia.getDestinatarioRuc(), guia.getDestinatarioRazon(),
                guia.getDestinoDir(), guia.getDestinoUbigeo(),
                guia.getVehiculoPlaca(), guia.getConductorNombre(),
                guia.getConductorDni(), guia.getConductorLicencia(),
                detalles,
                qrUrl,
                guia.getCdrCodigo() != null ? guia.getCdrCodigo() : "-",
                guia.getCdrDescripcion() != null ? guia.getCdrDescripcion() : "-",
                fechaCreacion
        );
    }
}

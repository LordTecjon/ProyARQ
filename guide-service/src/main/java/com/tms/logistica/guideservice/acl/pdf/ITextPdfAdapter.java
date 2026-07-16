package com.tms.logistica.guideservice.acl.pdf;

import com.itextpdf.html2pdf.HtmlConverter;
import com.tms.logistica.guideservice.domain.port.DocumentoPdfPort;
import com.tms.logistica.guideservice.model.entity.GuiaRemision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * ITextPdfAdapter — Adaptador ACL para generación de PDF con iText html2pdf
 *
 * Implementa el puerto DocumentoPdfPort del dominio utilizando
 * la librería com.itextpdf:html2pdf como motor de renderizado PDF.
 *
 * PATRÓN: Anti-Corruption Layer (adaptador de salida)
 *
 * El dominio (GuiaService) solo conoce DocumentoPdfPort y trabaja con byte[].
 * No sabe nada de iText, HTML, ni detalles del formato del documento.
 * Si se cambia la librería de PDF (a JasperReports, Apache PDFBox, etc.),
 * solo se crea un nuevo adaptador sin tocar GuiaService.
 *
 * El PDF generado incluye:
 *   - Datos del traslado (motivo, modalidad, fechas)
 *   - Remitente y destinatario con RUC, razón social, dirección y ubigeo
 *   - Datos del transportista (placa, conductor, licencia)
 *   - Tabla de bienes transportados (descripción, U.M., cantidad, peso)
 *   - Enlace de verificación SUNAT con código CDR
 *   - Footer con fecha de generación
 */
@Component
@Slf4j
public class ITextPdfAdapter implements DocumentoPdfPort {

    private static final String QR_BASE_URL =
            "https://e-beta.sunat.gob.pe/ol-ti-itconsulta/consulta";

    /**
     * Genera el PDF de la GRE y retorna sus bytes.
     *
     * @param guia entidad de la GRE en estado ACEPTADA
     * @return bytes del archivo PDF
     */
    @Override
    public byte[] generar(GuiaRemision guia) {
        log.info("[ACL-PDF] Generando PDF para guía {}-{}",
                guia.getSerie(), guia.getCorrelativo());

        String qrUrl = String.format("%s?ruc=%s&tipo=09&serie=%s&numero=%s",
                QR_BASE_URL, guia.getRemitenteRuc(), guia.getSerie(), guia.getCorrelativo());

        String html = construirHtml(guia, qrUrl);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, baos);

        log.info("[ACL-PDF] PDF generado exitosamente — {} bytes", baos.size());
        return baos.toByteArray();
    }

    /**
     * Retorna el nombre del archivo PDF según el formato SUNAT.
     * Ejemplo: "GRE-T001-00000001.pdf"
     */
    @Override
    public String nombreArchivo(GuiaRemision guia) {
        return String.format("GRE-%s-%s.pdf", guia.getSerie(), guia.getCorrelativo());
    }

    // ── Construcción del HTML ───────────────────────────────────────────────

    private String construirHtml(GuiaRemision guia, String qrUrl) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaEmision  = guia.getFechaInicio().format(fmt);
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
                guia.getCdrCodigo()      != null ? guia.getCdrCodigo()      : "-",
                guia.getCdrDescripcion() != null ? guia.getCdrDescripcion() : "-",
                fechaCreacion
        );
    }
}

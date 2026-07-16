package com.tms.logistica.guideservice.domain.port;

import com.tms.logistica.guideservice.model.entity.GuiaRemision;

/**
 * DocumentoPdfPort — Puerto de salida del dominio hacia generación de documentos PDF
 *
 * Define el contrato que el dominio necesita para generar el PDF oficial
 * de una Guía de Remisión Electrónica aceptada por SUNAT.
 *
 * PATRÓN: Anti-Corruption Layer (puerto de salida / output port)
 *
 * El dominio (GuiaService) solo conoce esta interfaz. No sabe nada de:
 *   - Qué librería se usa (iTextPDF, JasperReports, Apache PDFBox, etc.)
 *   - Cómo se construye el HTML/template del documento
 *   - Detalles de formato SUNAT en el PDF
 *
 * La implementación concreta (ITextPdfAdapter en acl/pdf/) encapsula
 * todos los detalles de generación con iText html2pdf.
 *
 * El método generar() retorna byte[] para que GuiaService y GuiaController
 * puedan servirlo directamente en el response HTTP sin depender
 * de rutas de archivo del sistema operativo.
 */
public interface DocumentoPdfPort {

    /**
     * Genera el PDF de la guía y retorna sus bytes.
     *
     * @param guia entidad de la GRE en estado ACEPTADA
     * @return array de bytes del archivo PDF generado
     */
    byte[] generar(GuiaRemision guia);

    /**
     * Retorna el nombre del archivo PDF según el formato SUNAT.
     * Ejemplo: "GRE-T001-00000001.pdf"
     *
     * @param guia entidad de la GRE
     * @return nombre del archivo listo para el header Content-Disposition
     */
    String nombreArchivo(GuiaRemision guia);
}

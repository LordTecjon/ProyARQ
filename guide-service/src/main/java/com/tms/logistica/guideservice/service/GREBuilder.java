package com.tms.logistica.guideservice.service;

import com.tms.logistica.guideservice.model.entity.GuiaRemision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * GREBuilder - Constructor del XML de la Guia de Remision Electronica
 *
 * Responsabilidad: generar el xml de la gre segun el esquema xsd oficial
 * de sunat. Esta clase esta separada de SunatGateway
 * de forma intencional, aplicando la tactica de modificabilidad:
 *   - Si SUNAT cambia el esquema xsd, solo se modifica grebuilder
 *   - Si se cambia de ose, solo se modifica SunatGateway
 *   - Ninguno de los dos cambios afecta al otro componente
 *
 * Estado actual: implementacion de prueba para el ambiente beta.
 * En produccion se generaria el xml completo con firma digital pkcs#7
 * usando el certificado digital de la empresa emisora.
 */
@Component
@Slf4j
public class GREBuilder {
    /**
     * construye el xml de la gre a partir de los datos de la entidad
     *
     * El xml sigue el estandar UBL 2.1 (Universal Business Language)
     * que sunat exige para la emision de guias de remision electronicas
     *
     * Limitacion actual: el xml generado es minimo y no incluye firma digital
     * Para el ambiente beta de apisperu, el ose realiza la firma por nosotros
     * En produccion directa con sunat se necesitaria firmar con el certificado
     * digital de la empresa antes del envio.
     *
     * @param "guia" Entidad con todos los datos de la guia ya persistida
     * @return String con el xml de la gre listo para enviar al ose
     */
    public String construir(GuiaRemision guia) {
        log.debug("construyendo xml para guía {}-{}", guia.getSerie(),
                guia.getCorrelativo());

        // en produccion aqui se genera el xml completo + firma digital.
        // por ahora devuelve un xml minimo para pruebas locales.
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <DespatchAdvice xmlns="urn:oasis:names:specification:ubl:schema:xsd:DespatchAdvice-2">
                  <ID>%s-%s</ID>
                  <IssueDate>%s</IssueDate>
                  <OrderReference>
                    <ID>%s</ID>
                  </OrderReference>
                  <DespatchSupplierParty>
                    <Party>
                      <PartyIdentification><ID schemeID="6">%s</ID></PartyIdentification>
                      <PartyName><Name>%s</Name></PartyName>
                    </Party>
                  </DespatchSupplierParty>
                  <DeliveryCustomerParty>
                    <Party>
                      <PartyIdentification><ID schemeID="6">%s</ID></PartyIdentification>
                      <PartyName><Name>%s</Name></PartyName>
                    </Party>
                  </DeliveryCustomerParty>
                </DespatchAdvice>
                """.formatted(
                guia.getSerie(), guia.getCorrelativo(), // ID del documento
                guia.getFechaInicio(), // fecha de inicio del traslado
                guia.getOrdenId(), // referencia a la orden de transporte
                guia.getRemitenteRuc(), guia.getRemitenteRazon(), // datos
                // del emisor
                guia.getDestinatarioRuc(), guia.getDestinatarioRazon()
                // datos del receptor
        );
    }
}

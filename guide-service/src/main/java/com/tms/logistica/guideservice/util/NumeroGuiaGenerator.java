package com.tms.logistica.guideservice.util;

import com.tms.logistica.guideservice.repository.GuiaRemisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * NumeroGuiaGenerator - Generador de numeros de guia correlativos
 *
 * Genera el siguiente numero de serie y correlativo para una guia de remision
 * en el formato exigido por sunat
 * serie de 4 caracteres + correlativo de 8 digitos.
 *
 * Ejemplo de secuencia generada para la serie T001:
 *   T001-00000001, T001-00000002, T001-00000003, etc
 *
 * El metodo es "synchronized" para garantizar unicidad en entornos
 * concurrentes:
 * si dos solicitudes llegan al mismo tiempo, solo una puede ejecutar el metodo
 * a la vez, evitando que se asigne el mismo numero a dos guias distintas.
 *
 * Limite: cada serie soporta hasta 99,999,999 guias. Si se supera,
 * se lanza IllegalStateException para que el administrador cree una nueva serie.
 */
@Component
@RequiredArgsConstructor
public class NumeroGuiaGenerator {

    private final GuiaRemisionRepository guiaRepo;
    /**
     * Calcula y devuelve el siguiente numero de guia para la serie indicada.
     * El metodo es thread-safe gracias a synchronized.
     *
     * @param "serie" Codigo de serie (ej: "T001" para transporte privado)
     * @return Array de dos elementos: [serie, correlativo] (ej: ["T001", "00000003"])
     * @throws "IllegalStateException" si se superan los 99,999,999 documentos de la serie
     */
    public synchronized String[] siguiente(String serie) {
        String ultimo = guiaRepo.findMaxCorrelativoBySerie(serie).orElse("00000000");
        int siguiente = Integer.parseInt(ultimo) + 1;
        if (siguiente > 99_999_999) {
            throw new IllegalStateException("Serie " + serie + " agotada");
        }
        return new String[]{ serie, String.format("%08d", siguiente) };
    }
}

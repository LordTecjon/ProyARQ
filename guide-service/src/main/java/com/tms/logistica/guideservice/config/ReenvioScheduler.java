package com.tms.logistica.guideservice.config;

import com.tms.logistica.guideservice.service.GuiaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * scheduler de reenvio automatico
 * ejecuta cada 5 minutos y reintenta las guías en cola
 * que no pudieron enviarse a sunat timeout u error de red
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ReenvioScheduler {

    private final GuiaService guiaService;

    @Scheduled(fixedDelay = 300_000) // cada 5 minutos
    public void procesarColaReenvio() {
        log.info("ejecutando scheduler de reenvio automatico a sunat");
        try {
            guiaService.procesarColaReenvio();
        } catch (Exception ex) {
            log.error("error en scheduler de reenvio", ex);
        }
    }
}

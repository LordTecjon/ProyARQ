package com.tms.logistica.guideservice.model.dto.request;

import com.tms.logistica.guideservice.model.enums.EstadoGuia;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class FiltroGuiaRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDesde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaHasta;

    private EstadoGuia estado;

    private Long ordenId;

    private int pagina = 0;

    private int tamanio = 20;
}

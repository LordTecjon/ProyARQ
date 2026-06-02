package com.tms.logistica.guideservice.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginaResponse<T> {

    private List<T> contenido;
    private int paginaActual;
    private int tamanio;
    private long totalElementos;
    private int totalPaginas;
    private boolean ultima;
}

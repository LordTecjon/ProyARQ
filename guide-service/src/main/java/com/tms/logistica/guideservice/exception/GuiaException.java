package com.tms.logistica.guideservice.exception;

public class GuiaException extends RuntimeException {

    private final String codigo;

    public GuiaException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    // ── Fábricas semánticas ────────────────────────────────────

    public static GuiaException noEncontrada(String uuid) {
        return new GuiaException("GUIA_NOT_FOUND",
                "no se encontró la guía con UUID: " + uuid);
    }

    public static GuiaException estadoInvalido(String estado, String accion) {
        return new GuiaException("ESTADO_INVALIDO",
                "no se puede ejecutar '" + accion + "' sobre una guía en " +
                        "estado " + estado);
    }

    public static GuiaException validacionSunat(String detalle) {
        return new GuiaException("VALIDACION_SUNAT", detalle);
    }

    public static GuiaException ordenInvalida(Long ordenId) {
        return new GuiaException("ORDEN_INVALIDA",
                "la orden " + ordenId + " no existe o no está disponible para" +
                        " emitir guía");
    }
}

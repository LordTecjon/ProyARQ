package com.tms.logistica.authservice.domain.enums;

public enum AuditEventType {
    LOGIN,
    LOGOUT,
    LOGIN_FALLIDO,
    BLOQUEO_CUENTA,
    CAMBIO_PASSWORD,
    RECUPERACION_PASSWORD,
    CAMBIO_ROL,
    CREACION_USUARIO,
    INACTIVACION_USUARIO,
    ASIGNACION_PERMISO
}

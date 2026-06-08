package com.tms.logistica.orderservice.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CodigoOrdenGenerator {

    public String generar(Long secuencia) {
        return "ORD-" + LocalDate.now().getYear() + "-" + String.format("%06d", secuencia);
    }
}

package com.tms.logistica.customerservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCustomerRequest {

    @Size(max = 300)
    private String direccionFiscal;

    @Size(max = 20)
    private String telefono;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100)
    private String correo;

    @Size(max = 150)
    private String personaContacto;

    /** Usuario que realiza el cambio (para auditoría) */
    private String modificadoPor;
}

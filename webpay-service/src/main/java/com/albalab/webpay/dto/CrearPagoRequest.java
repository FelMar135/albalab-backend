package com.albalab.webpay.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearPagoRequest(
        @NotBlank(message = "La orden de compra es obligatoria")
        @Size(max = 26, message = "La orden de compra no puede superar los 26 caracteres")
        String ordenCompra,

        @NotBlank(message = "El id de sesion es obligatorio")
        @Size(max = 61, message = "El id de sesion no puede superar los 61 caracteres")
        String idSesion,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "1.0", message = "El monto debe ser mayor o igual a 1")
        BigDecimal monto,

        @NotBlank(message = "La URL de retorno es obligatoria")
        @Size(max = 500, message = "La URL de retorno no puede superar los 500 caracteres")
        String urlRetorno
) {
}

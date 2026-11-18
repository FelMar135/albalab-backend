package com.albalab.webpay.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReembolsoRequest(
        @NotBlank(message = "El token de Webpay es obligatorio")
        String token,

        @NotNull(message = "El monto a reembolsar es obligatorio")
        @DecimalMin(value = "1.0", message = "El monto a reembolsar debe ser mayor o igual a 1")
        BigDecimal monto
) {
}

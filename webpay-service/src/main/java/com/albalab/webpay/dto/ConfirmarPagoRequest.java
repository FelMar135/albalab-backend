package com.albalab.webpay.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmarPagoRequest(
        @NotBlank(message = "El token de Webpay es obligatorio")
        String token
) {
}

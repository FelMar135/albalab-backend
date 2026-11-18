package com.albalab.webpay.dto;

public record CrearPagoResponse(
        Long id,
        String ordenCompra,
        String token,
        String url,
        String estado
) {
}

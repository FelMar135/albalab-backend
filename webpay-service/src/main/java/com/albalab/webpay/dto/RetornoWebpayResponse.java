package com.albalab.webpay.dto;

public record RetornoWebpayResponse(
        String estado,
        String mensaje,
        PagoResponse pago
) {
}

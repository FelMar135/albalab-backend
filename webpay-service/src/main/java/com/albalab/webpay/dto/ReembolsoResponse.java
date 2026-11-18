package com.albalab.webpay.dto;

import java.math.BigDecimal;

public record ReembolsoResponse(
        String token,
        String estado,
        Integer codigoRespuesta,
        BigDecimal montoReembolsado,
        String tipo,
        String codigoAutorizacion
) {
}

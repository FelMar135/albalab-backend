package com.albalab.webpay.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.albalab.webpay.model.TransaccionWebpay;

public record PagoResponse(
        Long id,
        String ordenCompra,
        String idSesion,
        BigDecimal monto,
        String urlRetorno,
        String token,
        String estado,
        String codigoAutorizacion,
        String tipoPago,
        Integer codigoRespuesta,
        BigDecimal montoFinal,
        String ultimosDigitosTarjeta,
        String fechaTransaccion,
        Instant fechaCreacion,
        Instant fechaActualizacion
) {
    public static PagoResponse from(TransaccionWebpay transaccion) {
        return new PagoResponse(
                transaccion.getId(),
                transaccion.getOrdenCompra(),
                transaccion.getIdSesion(),
                transaccion.getMonto(),
                transaccion.getUrlRetorno(),
                transaccion.getToken(),
                transaccion.getEstado().name(),
                transaccion.getCodigoAutorizacion(),
                transaccion.getTipoPago(),
                transaccion.getCodigoRespuesta(),
                transaccion.getMontoFinal(),
                transaccion.getUltimosDigitosTarjeta(),
                transaccion.getFechaTransaccion(),
                transaccion.getFechaCreacion(),
                transaccion.getFechaActualizacion()
        );
    }
}

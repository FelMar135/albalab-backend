package com.albalab.webpay.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import com.albalab.webpay.model.EstadoTransaccion;
import com.albalab.webpay.model.TransaccionWebpay;

class WebpayDtoTest {

    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void debeAceptarUnaCreacionDePagoValida() {
        assertValido(new CrearPagoRequest(
                "ORD-1001", "SES-1001", new BigDecimal("10000"), "http://retorno"));
    }

    @Test
    void debeRechazarUnaCreacionDePagoIncompleta() {
        assertInvalido(new CrearPagoRequest(
                "", "", BigDecimal.ZERO, ""),
                "ordenCompra", "idSesion", "monto", "urlRetorno");
    }

    @Test
    void debeAceptarUnaConfirmacionYUnReembolsoValidos() {
        assertValido(new ConfirmarPagoRequest("token-123"));
        assertValido(new ReembolsoRequest("token-123", new BigDecimal("10000")));
    }

    @Test
    void debeRechazarConfirmacionYReembolsoInvalidos() {
        assertInvalido(new ConfirmarPagoRequest(" "), "token");
        assertInvalido(new ReembolsoRequest("", BigDecimal.ZERO), "token", "monto");
    }

    @Test
    void debeMapearUnaTransaccionAPagoResponse() {
        TransaccionWebpay transaccion = new TransaccionWebpay();
        transaccion.setId(1L);
        transaccion.setOrdenCompra("ORD-1001");
        transaccion.setIdSesion("SES-1001");
        transaccion.setMonto(new BigDecimal("10000.00"));
        transaccion.setUrlRetorno("http://retorno");
        transaccion.setToken("token-123");
        transaccion.setEstado(EstadoTransaccion.APROBADA);
        transaccion.setCodigoAutorizacion("AUTH123");
        transaccion.setTipoPago("TD");
        transaccion.setCodigoRespuesta(0);
        transaccion.setMontoFinal(new BigDecimal("10000.00"));
        transaccion.setUltimosDigitosTarjeta("1234");
        transaccion.setFechaTransaccion("2026-01-01T10:00:00");
        transaccion.setFechaCreacion(Instant.parse("2026-01-01T10:00:00Z"));
        transaccion.setFechaActualizacion(Instant.parse("2026-01-01T10:01:00Z"));

        PagoResponse respuesta = PagoResponse.from(transaccion);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.ordenCompra()).isEqualTo("ORD-1001");
        assertThat(respuesta.estado()).isEqualTo("APROBADA");
        assertThat(respuesta.codigoAutorizacion()).isEqualTo("AUTH123");
        assertThat(respuesta.montoFinal()).isEqualByComparingTo("10000.00");
        assertThat(respuesta.ultimosDigitosTarjeta()).isEqualTo("1234");
        assertThat(respuesta.fechaCreacion()).isEqualTo(Instant.parse("2026-01-01T10:00:00Z"));
    }

    @Test
    void debeConstruirLasRespuestasSimples() {
        CrearPagoResponse crear = new CrearPagoResponse(
                1L, "ORD-1001", "token-123", "https://webpay", "INICIADA");
        ReembolsoResponse reembolso = new ReembolsoResponse(
                "token-123", "REEMBOLSADA", 0, new BigDecimal("10000"), "REEMBOLSO", "AUTH123");

        assertThat(crear.estado()).isEqualTo("INICIADA");
        assertThat(reembolso.montoReembolsado()).isEqualByComparingTo("10000");
    }

    private static void assertValido(Object dto) {
        assertThat(VALIDATOR.validate(dto)).isEmpty();
    }

    private static void assertInvalido(Object dto, String... campos) {
        assertThat(VALIDATOR.validate(dto))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(campos);
    }
}

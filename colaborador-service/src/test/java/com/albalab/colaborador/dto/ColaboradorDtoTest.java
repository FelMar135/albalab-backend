package com.albalab.colaborador.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import com.albalab.colaborador.model.Colaborador;

class ColaboradorDtoTest {

    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void debeAceptarUnaColaboracionValida() {
        assertValido(new ColaboradorRequest(
                "Felipe", "https://ejemplo.cl/foto.png", 5000, false, "ORD-1001"));
    }

    @Test
    void debeRechazarNombreVacio() {
        assertInvalido(new ColaboradorRequest(
                " ", null, 5000, false, null), "nombre");
    }

    @Test
    void debeRechazarMontoNuloOCero() {
        assertInvalido(new ColaboradorRequest("Felipe", null, null, false, null),
                "montoDonacion");
        assertInvalido(new ColaboradorRequest("Felipe", null, 0, false, null),
                "montoDonacion");
    }

    @Test
    void debeMapearUnaColaboracionARespuesta() {
        Colaborador colaborador = colaborador(false);

        ColaboradorResponse respuesta = ColaboradorResponse.from(colaborador);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.usuarioId()).isEqualTo(7L);
        assertThat(respuesta.usuarioEmail()).isEqualTo("usuario@albalab.cl");
        assertThat(respuesta.nombre()).isEqualTo("Felipe");
        assertThat(respuesta.fotoUri()).isEqualTo("https://ejemplo.cl/foto.png");
        assertThat(respuesta.montoDonacion()).isEqualTo(5000);
        assertThat(respuesta.esAnonimo()).isFalse();
        assertThat(respuesta.estado()).isEqualTo("APROBADA");
        assertThat(respuesta.webpayOrden()).isEqualTo("ORD-1001");
    }

    @Test
    void debeOcultarLosDatosDelDonanteAnonimoEnLaRespuestaPublica() {
        ColaboradorPublicoResponse respuesta =
                ColaboradorPublicoResponse.from(colaborador(true));

        assertThat(respuesta.nombre()).isEqualTo("Anonimo");
        assertThat(respuesta.fotoUri()).isNull();
        assertThat(respuesta.esAnonimo()).isTrue();
    }

    @Test
    void debeMantenerLosDatosDelDonanteNoAnonimoEnLaRespuestaPublica() {
        ColaboradorPublicoResponse respuesta =
                ColaboradorPublicoResponse.from(colaborador(false));

        assertThat(respuesta.nombre()).isEqualTo("Felipe");
        assertThat(respuesta.fotoUri()).isEqualTo("https://ejemplo.cl/foto.png");
        assertThat(respuesta.esAnonimo()).isFalse();
    }

    private Colaborador colaborador(boolean anonimo) {
        Colaborador colaborador = new Colaborador();
        colaborador.setId(1L);
        colaborador.setUsuarioId(7L);
        colaborador.setUsuarioEmail("usuario@albalab.cl");
        colaborador.setNombre("Felipe");
        colaborador.setFotoUri("https://ejemplo.cl/foto.png");
        colaborador.setMontoDonacion(5000);
        colaborador.setEsAnonimo(anonimo);
        colaborador.setFecha(LocalDateTime.parse("2026-01-01T10:00:00"));
        colaborador.setEstado("APROBADA");
        colaborador.setWebpayOrden("ORD-1001");
        return colaborador;
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

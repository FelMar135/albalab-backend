package com.albalab.usuario.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import com.albalab.usuario.model.Usuario;

class UsuarioDtoTest {

    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void debeAceptarUnRegistroValido() {
        assertValido(new RegistroRequest("nuevo@albalab.cl", "Usuario1234"));
    }

    @Test
    void debeRechazarUnRegistroConCorreoYContrasenaInvalidos() {
        assertInvalido(new RegistroRequest("correo-invalido", "corta"), "email", "password");
    }

    @Test
    void debeAceptarUnLoginValido() {
        assertValido(new LoginRequest("usuario@albalab.cl", "Usuario1234"));
    }

    @Test
    void debeRechazarUnLoginSinCorreoNiContrasena() {
        assertInvalido(new LoginRequest("", ""), "email", "password");
    }

    @Test
    void debeAceptarUnaActualizacionSinContrasena() {
        assertValido(new ActualizarUsuarioRequest("usuario@albalab.cl", null));
    }

    @Test
    void debeRechazarUnaActualizacionConContrasenaCorta() {
        assertInvalido(new ActualizarUsuarioRequest("usuario@albalab.cl", "corta"), "password");
    }

    @Test
    void debeMapearUnUsuarioARespuesta() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@albalab.cl");
        usuario.setRole("USER");
        usuario.setFechaCreacion(Instant.parse("2026-01-01T00:00:00Z"));

        UsuarioResponse respuesta = UsuarioResponse.from(usuario);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.email()).isEqualTo("usuario@albalab.cl");
        assertThat(respuesta.role()).isEqualTo("USER");
        assertThat(respuesta.fechaCreacion()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void debeConstruirLasRespuestasSimples() {
        LoginResponse login = new LoginResponse("ok", "token", "USER", "usuario@albalab.cl", 28800);

        assertThat(login.status()).isEqualTo("ok");
        assertThat(login.token()).isEqualTo("token");
        assertThat(new MensajeResponse("registrado").mensaje()).isEqualTo("registrado");
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

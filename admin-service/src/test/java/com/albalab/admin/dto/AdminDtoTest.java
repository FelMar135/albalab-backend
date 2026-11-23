package com.albalab.admin.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import com.albalab.admin.model.Administrador;

class AdminDtoTest {

    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void debeAceptarUnRegistroValido() {
        assertValido(new RegistroAdminRequest("nuevo-admin@albalab.cl", "Admin1234"));
    }

    @Test
    void debeRechazarUnRegistroConCorreoYContrasenaInvalidos() {
        assertInvalido(new RegistroAdminRequest("correo-invalido", "corta"),
                "email", "password");
    }

    @Test
    void debeAceptarUnLoginValido() {
        assertValido(new LoginAdminRequest("admin@albalab.cl", "Admin1234"));
    }

    @Test
    void debeRechazarUnLoginSinCorreoNiContrasena() {
        assertInvalido(new LoginAdminRequest("", ""), "email", "password");
    }

    @Test
    void debeAceptarUnaActualizacionSinContrasena() {
        assertValido(new ActualizarAdminRequest("admin@albalab.cl", null));
    }

    @Test
    void debeRechazarUnaActualizacionConContrasenaCorta() {
        assertInvalido(new ActualizarAdminRequest("admin@albalab.cl", "corta"), "password");
    }

    @Test
    void debeMapearUnAdministradorARespuesta() {
        Administrador administrador = new Administrador();
        administrador.setId(1L);
        administrador.setEmail("admin@albalab.cl");
        administrador.setRole("ADMIN");
        administrador.setFechaCreacion(Instant.parse("2026-01-01T00:00:00Z"));

        AdminResponse respuesta = AdminResponse.from(administrador);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.email()).isEqualTo("admin@albalab.cl");
        assertThat(respuesta.role()).isEqualTo("ADMIN");
        assertThat(respuesta.fechaCreacion()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void debeConstruirLasRespuestasSimples() {
        LoginAdminResponse login = new LoginAdminResponse(
                "ok", "token-admin", "ADMIN", "admin@albalab.cl", 28800);

        assertThat(login.status()).isEqualTo("ok");
        assertThat(login.token()).isEqualTo("token-admin");
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

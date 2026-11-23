package com.albalab.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.albalab.admin.dto.ActualizarAdminRequest;
import com.albalab.admin.dto.AdminResponse;
import com.albalab.admin.dto.LoginAdminRequest;
import com.albalab.admin.dto.LoginAdminResponse;
import com.albalab.admin.dto.RegistroAdminRequest;
import com.albalab.admin.exception.CorreoDuplicadoException;
import com.albalab.admin.exception.CredencialesInvalidasException;
import com.albalab.admin.exception.RecursoNoEncontradoException;
import com.albalab.admin.model.Administrador;
import com.albalab.admin.repository.AdministradorRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AdminService adminService;

    @Test
    void debeRegistrarAdministradorNormalizandoElCorreo() {
        when(administradorRepository.existsByEmailIgnoreCase("nuevo@albalab.cl"))
                .thenReturn(false);
        when(passwordEncoder.encode("Admin1234")).thenReturn("hash-admin");

        var respuesta = adminService.registrar(
                new RegistroAdminRequest("  Nuevo@AlbaLab.CL ", "Admin1234"));

        ArgumentCaptor<Administrador> captor = ArgumentCaptor.forClass(Administrador.class);
        verify(administradorRepository).save(captor.capture());

        Administrador guardado = captor.getValue();
        assertThat(respuesta.mensaje()).isEqualTo("Administrador registrado con exito");
        assertThat(guardado.getEmail()).isEqualTo("nuevo@albalab.cl");
        assertThat(guardado.getPassword()).isEqualTo("hash-admin");
        assertThat(guardado.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void debeRechazarRegistroConCorreoDuplicado() {
        when(administradorRepository.existsByEmailIgnoreCase("duplicado@albalab.cl"))
                .thenReturn(true);

        assertThatThrownBy(() -> adminService.registrar(
                new RegistroAdminRequest("duplicado@albalab.cl", "Admin1234")))
                .isInstanceOf(CorreoDuplicadoException.class)
                .hasMessage("El correo ya esta registrado como administrador");

        verify(administradorRepository, never()).save(any(Administrador.class));
    }

    @Test
    void debeIniciarSesionComoAdministrador() {
        Administrador administrador = administrador(1L, "admin@albalab.cl");
        when(administradorRepository.findByEmailIgnoreCase("admin@albalab.cl"))
                .thenReturn(Optional.of(administrador));
        when(passwordEncoder.matches("Admin1234", "hash-admin")).thenReturn(true);
        when(jwtService.generarToken("admin@albalab.cl", "ADMIN")).thenReturn("token-admin");
        when(jwtService.getExpirationSeconds()).thenReturn(28800L);

        LoginAdminResponse respuesta = adminService.login(
                new LoginAdminRequest("  ADMIN@ALBALAB.CL ", "Admin1234"));

        assertThat(respuesta.status()).isEqualTo("ok");
        assertThat(respuesta.token()).isEqualTo("token-admin");
        assertThat(respuesta.role()).isEqualTo("ADMIN");
        assertThat(respuesta.email()).isEqualTo("admin@albalab.cl");
        assertThat(respuesta.expiresInSeconds()).isEqualTo(28800L);
    }

    @Test
    void debeRechazarLoginSiElAdministradorNoExiste() {
        when(administradorRepository.findByEmailIgnoreCase("desconocido@albalab.cl"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.login(
                new LoginAdminRequest("desconocido@albalab.cl", "Admin1234")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales incorrectas");
    }

    @Test
    void debeRechazarLoginSiLaContrasenaNoCoincide() {
        Administrador administrador = administrador(1L, "admin@albalab.cl");
        when(administradorRepository.findByEmailIgnoreCase("admin@albalab.cl"))
                .thenReturn(Optional.of(administrador));
        when(passwordEncoder.matches("incorrecta", "hash-admin")).thenReturn(false);

        assertThatThrownBy(() -> adminService.login(
                new LoginAdminRequest("admin@albalab.cl", "incorrecta")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales incorrectas");
    }

    @Test
    void debeListarAdministradores() {
        when(administradorRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(administrador(1L, "admin@albalab.cl")));

        List<AdminResponse> respuesta = adminService.listar();

        assertThat(respuesta).hasSize(1);
        assertThat(respuesta.get(0).email()).isEqualTo("admin@albalab.cl");
    }

    @Test
    void debeFallarAlBuscarAdministradorInexistente() {
        when(administradorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Administrador no encontrado con id 99");
    }

    @Test
    void debeActualizarCorreoYContrasena() {
        Administrador administrador = administrador(1L, "anterior@albalab.cl");
        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));
        when(administradorRepository.existsByEmailIgnoreCase("nuevo@albalab.cl"))
                .thenReturn(false);
        when(passwordEncoder.encode("Nueva1234")).thenReturn("nuevo-hash");
        when(administradorRepository.save(administrador)).thenReturn(administrador);

        AdminResponse respuesta = adminService.actualizar(
                1L, new ActualizarAdminRequest("  Nuevo@AlbaLab.CL ", "Nueva1234"));

        assertThat(respuesta.email()).isEqualTo("nuevo@albalab.cl");
        assertThat(administrador.getPassword()).isEqualTo("nuevo-hash");
    }

    @Test
    void debeRechazarActualizacionConCorreoDuplicado() {
        Administrador administrador = administrador(1L, "anterior@albalab.cl");
        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));
        when(administradorRepository.existsByEmailIgnoreCase("ocupado@albalab.cl"))
                .thenReturn(true);

        assertThatThrownBy(() -> adminService.actualizar(
                1L, new ActualizarAdminRequest("ocupado@albalab.cl", null)))
                .isInstanceOf(CorreoDuplicadoException.class)
                .hasMessage("El correo ya esta registrado como administrador");

        verify(administradorRepository, never()).save(any(Administrador.class));
    }

    @Test
    void debeMantenerLaContrasenaSiNoSeEnviaUnaNueva() {
        Administrador administrador = administrador(1L, "admin@albalab.cl");
        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));
        when(administradorRepository.save(administrador)).thenReturn(administrador);

        adminService.actualizar(1L, new ActualizarAdminRequest("admin@albalab.cl", null));

        assertThat(administrador.getPassword()).isEqualTo("hash-admin");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void debeEliminarAdministrador() {
        Administrador administrador = administrador(1L, "admin@albalab.cl");
        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));

        adminService.eliminar(1L);

        verify(administradorRepository).delete(administrador);
    }

    private Administrador administrador(Long id, String email) {
        Administrador administrador = new Administrador();
        administrador.setId(id);
        administrador.setEmail(email);
        administrador.setPassword("hash-admin");
        administrador.setRole("ADMIN");
        administrador.setFechaCreacion(Instant.parse("2026-01-01T00:00:00Z"));
        return administrador;
    }
}

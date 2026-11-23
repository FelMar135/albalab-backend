package com.albalab.usuario.service;

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

import com.albalab.usuario.dto.ActualizarUsuarioRequest;
import com.albalab.usuario.dto.LoginRequest;
import com.albalab.usuario.dto.LoginResponse;
import com.albalab.usuario.dto.RegistroRequest;
import com.albalab.usuario.dto.UsuarioResponse;
import com.albalab.usuario.exception.CorreoDuplicadoException;
import com.albalab.usuario.exception.CredencialesInvalidasException;
import com.albalab.usuario.exception.RecursoNoEncontradoException;
import com.albalab.usuario.model.Usuario;
import com.albalab.usuario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void debeRegistrarUsuarioNormalizandoElCorreo() {
        when(usuarioRepository.existsByEmailIgnoreCase("nuevo@albalab.cl")).thenReturn(false);
        when(passwordEncoder.encode("Usuario1234")).thenReturn("hash");

        var respuesta = usuarioService.registrar(
                new RegistroRequest("  Nuevo@AlbaLab.CL  ", "Usuario1234"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario guardado = captor.getValue();
        assertThat(respuesta.mensaje()).isEqualTo("Usuario registrado con exito");
        assertThat(guardado.getEmail()).isEqualTo("nuevo@albalab.cl");
        assertThat(guardado.getPassword()).isEqualTo("hash");
        assertThat(guardado.getRole()).isEqualTo("USER");
    }

    @Test
    void debeRechazarRegistroConCorreoDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase("duplicado@albalab.cl")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(
                new RegistroRequest("duplicado@albalab.cl", "Usuario1234")))
                .isInstanceOf(CorreoDuplicadoException.class)
                .hasMessage("El correo ya esta registrado");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void debeIniciarSesionYGenerarToken() {
        Usuario usuario = usuario(1L, "usuario@albalab.cl", "USER");
        when(usuarioRepository.findByEmailIgnoreCase("usuario@albalab.cl"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Usuario1234", "hash")).thenReturn(true);
        when(jwtService.generarToken(1L, "usuario@albalab.cl", "USER")).thenReturn("token-123");
        when(jwtService.getExpirationSeconds()).thenReturn(28800L);

        LoginResponse respuesta = usuarioService.login(
                new LoginRequest("  USUARIO@ALBALAB.CL ", "Usuario1234"));

        assertThat(respuesta.status()).isEqualTo("ok");
        assertThat(respuesta.token()).isEqualTo("token-123");
        assertThat(respuesta.role()).isEqualTo("USER");
        assertThat(respuesta.email()).isEqualTo("usuario@albalab.cl");
        assertThat(respuesta.expiresInSeconds()).isEqualTo(28800L);
    }

    @Test
    void debeRechazarLoginSiElUsuarioNoExiste() {
        when(usuarioRepository.findByEmailIgnoreCase("desconocido@albalab.cl"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.login(
                new LoginRequest("desconocido@albalab.cl", "Usuario1234")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales incorrectas");
    }

    @Test
    void debeRechazarLoginSiLaContrasenaNoCoincide() {
        Usuario usuario = usuario(1L, "usuario@albalab.cl", "USER");
        when(usuarioRepository.findByEmailIgnoreCase("usuario@albalab.cl"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("incorrecta", "hash")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.login(
                new LoginRequest("usuario@albalab.cl", "incorrecta")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales incorrectas");
    }

    @Test
    void debeListarUsuarios() {
        when(usuarioRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(usuario(1L, "usuario@albalab.cl", "USER")));

        List<UsuarioResponse> respuesta = usuarioService.listar();

        assertThat(respuesta).hasSize(1);
        assertThat(respuesta.get(0).email()).isEqualTo("usuario@albalab.cl");
    }

    @Test
    void debeFallarAlBuscarUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Usuario no encontrado con id 99");
    }

    @Test
    void debeActualizarCorreoYContrasena() {
        Usuario usuario = usuario(1L, "anterior@albalab.cl", "USER");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCase("nuevo@albalab.cl")).thenReturn(false);
        when(passwordEncoder.encode("Nueva1234")).thenReturn("nuevo-hash");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponse respuesta = usuarioService.actualizar(
                1L, new ActualizarUsuarioRequest("  Nuevo@AlbaLab.CL ", "Nueva1234"));

        assertThat(respuesta.email()).isEqualTo("nuevo@albalab.cl");
        assertThat(usuario.getPassword()).isEqualTo("nuevo-hash");
    }

    @Test
    void debeRechazarActualizacionConCorreoDuplicado() {
        Usuario usuario = usuario(1L, "anterior@albalab.cl", "USER");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCase("ocupado@albalab.cl")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.actualizar(
                1L, new ActualizarUsuarioRequest("ocupado@albalab.cl", null)))
                .isInstanceOf(CorreoDuplicadoException.class)
                .hasMessage("El correo ya esta registrado");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void debeMantenerLaContrasenaSiNoSeEnviaUnaNueva() {
        Usuario usuario = usuario(1L, "usuario@albalab.cl", "USER");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        usuarioService.actualizar(1L, new ActualizarUsuarioRequest("usuario@albalab.cl", null));

        assertThat(usuario.getPassword()).isEqualTo("hash");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void debeEliminarUsuario() {
        Usuario usuario = usuario(1L, "usuario@albalab.cl", "USER");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.eliminar(1L);

        verify(usuarioRepository).delete(usuario);
    }

    private Usuario usuario(Long id, String email, String role) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        usuario.setPassword("hash");
        usuario.setRole(role);
        usuario.setFechaCreacion(Instant.parse("2026-01-01T00:00:00Z"));
        return usuario;
    }
}

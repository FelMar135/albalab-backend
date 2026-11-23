package com.albalab.usuario.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.albalab.usuario.config.SecurityConfig;
import com.albalab.usuario.dto.ActualizarUsuarioRequest;
import com.albalab.usuario.dto.UsuarioResponse;
import com.albalab.usuario.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    private final UsuarioResponse usuario = new UsuarioResponse(
            1L, "usuario@albalab.cl", "USER", Instant.parse("2026-01-01T00:00:00Z"));

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void debeListarUsuarios() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("usuario@albalab.cl"));
    }

    @Test
    void debeObtenerPerfilDesdeElJwt() throws Exception {
        when(usuarioService.obtenerPorEmail("usuario@albalab.cl")).thenReturn(usuario);
        autenticar(Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("usuario@albalab.cl")
                .build());

        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("usuario@albalab.cl"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void debeObtenerUsuarioPorId() throws Exception {
        when(usuarioService.obtener(1L)).thenReturn(usuario);

        mockMvc.perform(get("/api/usuarios/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void debeActualizarUsuario() throws Exception {
        when(usuarioService.actualizar(eq(1L), any(ActualizarUsuarioRequest.class)))
                .thenReturn(usuario);

        mockMvc.perform(put("/api/usuarios/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"usuario@albalab.cl","password":"Nueva1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("usuario@albalab.cl"));
    }

    @Test
    void debeEliminarUsuario() throws Exception {
        doNothing().when(usuarioService).eliminar(1L);

        mockMvc.perform(delete("/api/usuarios/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(usuarioService).eliminar(1L);
    }

    private void autenticar(Jwt jwt) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new JwtAuthenticationToken(jwt));
        SecurityContextHolder.setContext(context);
    }
}

package com.albalab.colaborador.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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

import com.albalab.colaborador.config.SecurityConfig;
import com.albalab.colaborador.dto.ColaboradorPublicoResponse;
import com.albalab.colaborador.dto.ColaboradorRequest;
import com.albalab.colaborador.dto.ColaboradorResponse;
import com.albalab.colaborador.service.ColaboradorService;

@WebMvcTest(ColaboradorController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class ColaboradorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ColaboradorService colaboradorService;

    private final ColaboradorResponse colaborador = new ColaboradorResponse(
            1L, 7L, "usuario@albalab.cl", "Felipe", "https://ejemplo.cl/foto.png",
            5000, false, LocalDateTime.parse("2026-01-01T10:00:00"),
            "APROBADA", "ORD-1001");

    private final ColaboradorPublicoResponse publico = new ColaboradorPublicoResponse(
            2L, "Anonimo", null, true, LocalDateTime.parse("2026-01-01T10:00:00"));

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void debeListarColaboradoresComoAdmin() throws Exception {
        when(colaboradorService.listarTodos(any(Jwt.class)))
                .thenReturn(List.of(colaborador));
        autenticar(jwtAdmin());

        mockMvc.perform(get("/api/colaboradores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Felipe"));
    }

    @Test
    void debeListarTodosDesdeRutaAdmin() throws Exception {
        when(colaboradorService.listarTodos(any(Jwt.class)))
                .thenReturn(List.of(colaborador));
        autenticar(jwtAdmin());

        mockMvc.perform(get("/api/colaboradores/admin/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].montoDonacion").value(5000));
    }

    @Test
    void debeListarMisColaboraciones() throws Exception {
        when(colaboradorService.listarMios(any(Jwt.class)))
                .thenReturn(List.of(colaborador));
        autenticar(jwtUsuario());

        mockMvc.perform(get("/api/colaboradores/mios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioEmail").value("usuario@albalab.cl"));
    }

    @Test
    void debeListarColaboracionesPublicas() throws Exception {
        when(colaboradorService.listarPublicos()).thenReturn(List.of(publico));

        mockMvc.perform(get("/api/colaboradores/publicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Anonimo"))
                .andExpect(jsonPath("$[0].esAnonimo").value(true));
    }

    @Test
    void debeObtenerColaboracion() throws Exception {
        when(colaboradorService.obtener(any(Jwt.class), eq(1L)))
                .thenReturn(colaborador);
        autenticar(jwtUsuario());

        mockMvc.perform(get("/api/colaboradores/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Felipe"))
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    @Test
    void debeCrearColaboracion() throws Exception {
        when(colaboradorService.crear(any(Jwt.class), any(ColaboradorRequest.class)))
                .thenReturn(colaborador);
        autenticar(jwtUsuario());

        mockMvc.perform(post("/api/colaboradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Felipe","fotoUri":"https://ejemplo.cl/foto.png",
                                 "montoDonacion":5000,"esAnonimo":false,"webpayOrden":"ORD-1001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Felipe"))
                .andExpect(jsonPath("$.montoDonacion").value(5000));
    }

    @Test
    void debeActualizarColaboracionComoAdmin() throws Exception {
        when(colaboradorService.actualizar(any(Jwt.class), eq(1L),
                any(ColaboradorRequest.class))).thenReturn(colaborador);
        autenticar(jwtAdmin());

        mockMvc.perform(put("/api/colaboradores/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Felipe","montoDonacion":5000,
                                 "esAnonimo":false,"webpayOrden":"ORD-1001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.webpayOrden").value("ORD-1001"));
    }

    @Test
    void debeEliminarColaboracionComoAdmin() throws Exception {
        doNothing().when(colaboradorService).eliminar(any(Jwt.class), eq(1L));
        autenticar(jwtAdmin());

        mockMvc.perform(delete("/api/colaboradores/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    private void autenticar(Jwt jwt) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new JwtAuthenticationToken(jwt));
        SecurityContextHolder.setContext(context);
    }

    private Jwt jwtAdmin() {
        return Jwt.withTokenValue("token-admin")
                .header("alg", "none")
                .subject("admin@albalab.cl")
                .claim("roles", List.of("ADMIN"))
                .build();
    }

    private Jwt jwtUsuario() {
        return Jwt.withTokenValue("token-usuario")
                .header("alg", "none")
                .subject("usuario@albalab.cl")
                .claim("usuarioId", 7L)
                .build();
    }
}

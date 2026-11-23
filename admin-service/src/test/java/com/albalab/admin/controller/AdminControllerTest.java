package com.albalab.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.albalab.admin.config.SecurityConfig;
import com.albalab.admin.dto.ActualizarAdminRequest;
import com.albalab.admin.dto.AdminResponse;
import com.albalab.admin.dto.MensajeResponse;
import com.albalab.admin.dto.RegistroAdminRequest;
import com.albalab.admin.service.AdminService;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    private final AdminResponse admin = new AdminResponse(
            1L, "admin@albalab.cl", "ADMIN", Instant.parse("2026-01-01T00:00:00Z"));

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void debeListarAdministradores() throws Exception {
        when(adminService.listar()).thenReturn(List.of(admin));

        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("admin@albalab.cl"));
    }

    @Test
    void debeObtenerPerfilDesdeElJwt() throws Exception {
        when(adminService.obtenerPorEmail("admin@albalab.cl")).thenReturn(admin);
        autenticar(Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("admin@albalab.cl")
                .build());

        mockMvc.perform(get("/api/admin/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@albalab.cl"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void debeObtenerAdminPorId() throws Exception {
        when(adminService.obtener(1L)).thenReturn(admin);

        mockMvc.perform(get("/api/admin/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void debeCrearAdmin() throws Exception {
        when(adminService.registrar(any(RegistroAdminRequest.class)))
                .thenReturn(new MensajeResponse("Administrador registrado con exito"));

        mockMvc.perform(post("/api/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nuevo-admin@albalab.cl","password":"Admin1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje")
                        .value("Administrador registrado con exito"));
    }

    @Test
    void debeActualizarAdmin() throws Exception {
        when(adminService.actualizar(eq(1L), any(ActualizarAdminRequest.class)))
                .thenReturn(admin);

        mockMvc.perform(put("/api/admin/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@albalab.cl","password":"Nueva1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@albalab.cl"));
    }

    @Test
    void debeEliminarAdmin() throws Exception {
        doNothing().when(adminService).eliminar(1L);

        mockMvc.perform(delete("/api/admin/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(adminService).eliminar(1L);
    }

    private void autenticar(Jwt jwt) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new JwtAuthenticationToken(jwt));
        SecurityContextHolder.setContext(context);
    }
}

package com.albalab.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.albalab.admin.dto.LoginAdminRequest;
import com.albalab.admin.dto.LoginAdminResponse;
import com.albalab.admin.dto.MensajeResponse;
import com.albalab.admin.dto.RegistroAdminRequest;
import com.albalab.admin.service.AdminService;

@WebMvcTest(AdminAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @Test
    void debeIniciarSesionComoAdmin() throws Exception {
        when(adminService.login(any(LoginAdminRequest.class)))
                .thenReturn(new LoginAdminResponse("ok", "token-admin", "ADMIN",
                        "admin@albalab.cl", 28800));

        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@albalab.cl","password":"Admin1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.token").value("token-admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void debeRegistrarAdministrador() throws Exception {
        when(adminService.registrar(any(RegistroAdminRequest.class)))
                .thenReturn(new MensajeResponse("Administrador registrado con exito"));

        mockMvc.perform(post("/api/admin/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nuevo-admin@albalab.cl","password":"Admin1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje")
                        .value("Administrador registrado con exito"));
    }

    @Test
    void debeRechazarLoginConDatosInvalidos() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"correo-invalido","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Datos invalidos"))
                .andExpect(jsonPath("$.errores").isArray());
    }
}

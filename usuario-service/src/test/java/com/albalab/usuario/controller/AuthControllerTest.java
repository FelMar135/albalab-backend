package com.albalab.usuario.controller;

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

import com.albalab.usuario.dto.LoginRequest;
import com.albalab.usuario.dto.LoginResponse;
import com.albalab.usuario.dto.MensajeResponse;
import com.albalab.usuario.dto.RegistroRequest;
import com.albalab.usuario.service.UsuarioService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void debeRegistrarUsuario() throws Exception {
        when(usuarioService.registrar(any(RegistroRequest.class)))
                .thenReturn(new MensajeResponse("Usuario registrado con exito"));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nuevo@albalab.cl","password":"Usuario1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Usuario registrado con exito"));
    }

    @Test
    void debeIniciarSesion() throws Exception {
        when(usuarioService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("ok", "token-de-prueba", "USER",
                        "usuario@albalab.cl", 28800));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"usuario@albalab.cl","password":"Usuario1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.token").value("token-de-prueba"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.email").value("usuario@albalab.cl"));
    }

    @Test
    void debeRechazarLoginConDatosInvalidos() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"no-es-email","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Datos invalidos"))
                .andExpect(jsonPath("$.errores").isArray());
    }
}

package com.albalab.historial.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.albalab.historial.dto.CompraRequest;
import com.albalab.historial.model.Compra;
import com.albalab.historial.service.CompraService;

@WebMvcTest(CompraController.class)
@AutoConfigureMockMvc(addFilters = false)
class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompraService compraService;

    private final Authentication autenticacion =
            new UsernamePasswordAuthenticationToken("usuario@albalab.cl", null, List.of());

    @Test
    void debeGuardarLaCompraDelUsuarioAutenticado() throws Exception {
        Compra compra = new Compra(
                "usuario@albalab.cl", 1L, 30000, "Polera Albalab x2");
        when(compraService.guardar(eq("usuario@albalab.cl"), any(CompraRequest.class)))
                .thenReturn(compra);

        mockMvc.perform(post("/api/historial/compras")
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productos":"Polera Albalab x2","total":30000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioEmail").value("usuario@albalab.cl"))
                .andExpect(jsonPath("$.productos").value("Polera Albalab x2"))
                .andExpect(jsonPath("$.total").value(30000));

        verify(compraService).guardar(
                eq("usuario@albalab.cl"),
                argThat(request -> "Polera Albalab x2".equals(request.getProductos())
                        && Integer.valueOf(30000).equals(request.getTotal())));
    }

    @Test
    void debeListarLasComprasDelUsuarioAutenticado() throws Exception {
        when(compraService.listar("usuario@albalab.cl")).thenReturn(List.of(
                new Compra("usuario@albalab.cl", 2000L, 30000, "Polera Albalab x2"),
                new Compra("usuario@albalab.cl", 1000L, 15000, "Taza Albalab")
        ));

        mockMvc.perform(get("/api/historial/compras")
                        .principal(autenticacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fecha").value(2000L))
                .andExpect(jsonPath("$[0].productos").value("Polera Albalab x2"))
                .andExpect(jsonPath("$[1].total").value(15000));

        verify(compraService).listar("usuario@albalab.cl");
    }
}

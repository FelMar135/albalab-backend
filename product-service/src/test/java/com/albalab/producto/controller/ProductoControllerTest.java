package com.albalab.producto.controller;

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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.albalab.producto.dto.ProductoRequest;
import com.albalab.producto.dto.ProductoResponse;
import com.albalab.producto.service.ProductoService;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    private final ProductoResponse producto = new ProductoResponse(
            1L, "Polera Albalab", 15000, "Polera oficial", "https://ejemplo.cl/polera.png", true);

    @Test
    void debeListarProductos() throws Exception {
        when(productoService.listar()).thenReturn(List.of(producto));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Polera Albalab"));
    }

    @Test
    void debeObtenerProductoPorId() throws Exception {
        when(productoService.obtener(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precio").value(15000));
    }

    @Test
    void debeCrearProducto() throws Exception {
        when(productoService.crear(any(ProductoRequest.class))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Polera Albalab","precio":15000,
                                 "descripcion":"Polera oficial","imagenUrl":"https://ejemplo.cl/polera.png",
                                 "disponible":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Polera Albalab"))
                .andExpect(jsonPath("$.disponible").value(true));
    }

    @Test
    void debeActualizarProducto() throws Exception {
        when(productoService.actualizar(eq(1L), any(ProductoRequest.class)))
                .thenReturn(producto);

        mockMvc.perform(put("/api/productos/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Polera Albalab","precio":15000,
                                 "descripcion":"Polera oficial","disponible":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Polera Albalab"));
    }

    @Test
    void debeEliminarProducto() throws Exception {
        doNothing().when(productoService).eliminar(1L);

        mockMvc.perform(delete("/api/productos/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void debeRechazarProductoInvalido() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"","precio":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Datos invalidos"))
                .andExpect(jsonPath("$.errores").isArray());
    }
}

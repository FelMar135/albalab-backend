package com.albalab.webpay.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.albalab.webpay.dto.ConfirmarPagoRequest;
import com.albalab.webpay.dto.CrearPagoRequest;
import com.albalab.webpay.dto.CrearPagoResponse;
import com.albalab.webpay.dto.PagoResponse;
import com.albalab.webpay.dto.ReembolsoRequest;
import com.albalab.webpay.dto.ReembolsoResponse;
import com.albalab.webpay.dto.RetornoWebpayResponse;
import com.albalab.webpay.service.WebpayService;

@WebMvcTest(WebpayController.class)
@AutoConfigureMockMvc(addFilters = false)
class WebpayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WebpayService webpayService;

    private final PagoResponse pago = new PagoResponse(
            1L, "ORD-1001", "SES-1001", new BigDecimal("10000"),
            "http://localhost:9090/api/webpay/retorno", "token-123", "APROBADA",
            "AUTH123", "TD", 0, new BigDecimal("10000"), "1234",
            "2026-01-01T10:00:00", Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T10:00:00Z"));

    @Test
    void debeCrearPago() throws Exception {
        when(webpayService.crear(any(CrearPagoRequest.class)))
                .thenReturn(new CrearPagoResponse(1L, "ORD-1001", "token-123",
                        "https://webpay3gint.transbank.cl/webpayserver/initTransaction",
                        "INICIADA"));

        mockMvc.perform(post("/api/webpay/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ordenCompra":"ORD-1001","idSesion":"SES-1001",
                                 "monto":10000,"urlRetorno":"http://localhost:9090/api/webpay/retorno"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordenCompra").value("ORD-1001"))
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.estado").value("INICIADA"));
    }

    @Test
    void debeConfirmarPago() throws Exception {
        when(webpayService.confirmar(any(ConfirmarPagoRequest.class))).thenReturn(pago);

        mockMvc.perform(post("/api/webpay/confirmar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"token-123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.codigoAutorizacion").value("AUTH123"));
    }

    @Test
    void debeConsultarEstadoPorToken() throws Exception {
        when(webpayService.consultarEstado("token-123")).thenReturn(pago);

        mockMvc.perform(get("/api/webpay/estado/{token}", "token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordenCompra").value("ORD-1001"))
                .andExpect(jsonPath("$.montoFinal").value(10000));
    }

    @Test
    void debeReembolsarPago() throws Exception {
        when(webpayService.reembolsar(any(ReembolsoRequest.class)))
                .thenReturn(new ReembolsoResponse("token-123", "REEMBOLSADA", 0,
                        new BigDecimal("10000"), "REEMBOLSO", "AUTH123"));

        mockMvc.perform(post("/api/webpay/reembolsar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"token-123","monto":10000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REEMBOLSADA"))
                .andExpect(jsonPath("$.montoReembolsado").value(10000));
    }

    @Test
    void debeProcesarRetornoPorPost() throws Exception {
        when(webpayService.procesarRetorno(anyMap()))
                .thenReturn(new RetornoWebpayResponse("APROBADA", "Pago confirmado", pago));

        mockMvc.perform(post("/api/webpay/retorno")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("token_ws", "token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.pago.ordenCompra").value("ORD-1001"));
    }

    @Test
    void debeProcesarRetornoPorGet() throws Exception {
        when(webpayService.procesarRetorno(anyMap()))
                .thenReturn(new RetornoWebpayResponse("APROBADA", "Pago confirmado", pago));

        mockMvc.perform(get("/api/webpay/retorno")
                        .param("token_ws", "token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pago confirmado"));
    }

    @Test
    void debeListarPagos() throws Exception {
        when(webpayService.listar()).thenReturn(List.of(pago));

        mockMvc.perform(get("/api/webpay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].ordenCompra").value("ORD-1001"));
    }
}

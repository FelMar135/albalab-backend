package com.albalab.webpay.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.albalab.webpay.dto.ConfirmarPagoRequest;
import com.albalab.webpay.dto.CrearPagoRequest;
import com.albalab.webpay.dto.ReembolsoRequest;
import com.albalab.webpay.exception.OrdenDuplicadaException;
import com.albalab.webpay.exception.RecursoNoEncontradoException;
import com.albalab.webpay.exception.WebpayException;
import com.albalab.webpay.model.EstadoTransaccion;
import com.albalab.webpay.model.TransaccionWebpay;
import com.albalab.webpay.repository.TransaccionWebpayRepository;

import cl.transbank.model.CardDetail;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionRefundResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionStatusResponse;

@ExtendWith(MockitoExtension.class)
class WebpayServiceTest {

    @Mock
    private TransaccionWebpayRepository transaccionRepository;

    @Mock
    private WebpayPlus.Transaction transaction;

    private WebpayService webpayService;

    @BeforeEach
    void setUp() {
        webpayService = new WebpayService(transaccionRepository, "", "", "TEST");
        ReflectionTestUtils.setField(webpayService, "transaction", transaction);
    }

    @Test
    void debeCrearPagoEnWebpay() throws Exception {
        when(transaccionRepository.existsByOrdenCompra("ORD-1001")).thenReturn(false);
        configurarGuardado();
        when(transaction.create("ORD-1001", "SES-1001", 10000.0, "http://retorno"))
                .thenReturn(new WebpayPlusTransactionCreateResponse(
                        "token-123", "https://webpay3gint.transbank.cl/webpayserver/initTransaction"));

        var respuesta = webpayService.crear(new CrearPagoRequest(
                "ORD-1001", "SES-1001", new BigDecimal("10000"), "http://retorno"));

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.ordenCompra()).isEqualTo("ORD-1001");
        assertThat(respuesta.token()).isEqualTo("token-123");
        assertThat(respuesta.estado()).isEqualTo("INICIADA");
        verify(transaccionRepository, times(2)).save(any(TransaccionWebpay.class));
    }

    @Test
    void debeRechazarOrdenDeCompraDuplicada() {
        when(transaccionRepository.existsByOrdenCompra("ORD-1001")).thenReturn(true);

        assertThatThrownBy(() -> webpayService.crear(new CrearPagoRequest(
                "ORD-1001", "SES-1001", new BigDecimal("10000"), "http://retorno")))
                .isInstanceOf(OrdenDuplicadaException.class)
                .hasMessage("Ya existe una transaccion con la orden de compra ORD-1001");

        verify(transaccionRepository, never()).save(any(TransaccionWebpay.class));
        verifyNoInteractions(transaction);
    }

    @Test
    void debeMarcarErrorSiWebpayFallaAlCrear() throws Exception {
        when(transaccionRepository.existsByOrdenCompra("ORD-1001")).thenReturn(false);
        configurarGuardado();
        when(transaction.create(anyString(), anyString(), anyDouble(), anyString()))
                .thenThrow(new RuntimeException("fallo de red"));

        assertThatThrownBy(() -> webpayService.crear(new CrearPagoRequest(
                "ORD-1001", "SES-1001", new BigDecimal("10000"), "http://retorno")))
                .isInstanceOf(WebpayException.class)
                .hasMessage("No se pudo crear la transaccion en Webpay");

        ArgumentCaptor<TransaccionWebpay> captor = ArgumentCaptor.forClass(TransaccionWebpay.class);
        verify(transaccionRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getEstado())
                .isEqualTo(EstadoTransaccion.ERROR);
    }

    @Test
    void debeConfirmarPagoAprobado() throws Exception {
        TransaccionWebpay transaccion = transaccion(EstadoTransaccion.INICIADA);
        when(transaccionRepository.findByToken("token-123")).thenReturn(Optional.of(transaccion));
        configurarGuardado();

        WebpayPlusTransactionCommitResponse commit = new WebpayPlusTransactionCommitResponse();
        commit.setResponseCode((byte) 0);
        commit.setAuthorizationCode("AUTH123");
        commit.setPaymentTypeCode("TD");
        commit.setAmount(10000);
        commit.setTransactionDate("2026-01-01T10:00:00");
        commit.setCardDetail(new CardDetail("1234"));
        when(transaction.commit("token-123")).thenReturn(commit);

        var respuesta = webpayService.confirmar(new ConfirmarPagoRequest("token-123"));

        assertThat(respuesta.estado()).isEqualTo("APROBADA");
        assertThat(respuesta.codigoAutorizacion()).isEqualTo("AUTH123");
        assertThat(respuesta.tipoPago()).isEqualTo("TD");
        assertThat(respuesta.ultimosDigitosTarjeta()).isEqualTo("1234");
        assertThat(respuesta.montoFinal()).isEqualByComparingTo("10000.00");
        assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.APROBADA);
    }

    @Test
    void debeDevolverEstadoFinalSinConsultarWebpay() {
        when(transaccionRepository.findByToken("token-123"))
                .thenReturn(Optional.of(transaccion(EstadoTransaccion.APROBADA)));

        var respuesta = webpayService.confirmar("token-123");

        assertThat(respuesta.estado()).isEqualTo("APROBADA");
        verifyNoInteractions(transaction);
        verify(transaccionRepository, never()).save(any(TransaccionWebpay.class));
    }

    @Test
    void debeFallarSiNoExisteLaTransaccion() {
        when(transaccionRepository.findByToken("token-inexistente"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> webpayService.consultarEstado("token-inexistente"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Transaccion Webpay no encontrada para el token indicado");
    }

    @Test
    void debeConsultarYActualizarEstadoDesdeWebpay() throws Exception {
        TransaccionWebpay transaccion = transaccion(EstadoTransaccion.INICIADA);
        when(transaccionRepository.findByToken("token-123")).thenReturn(Optional.of(transaccion));
        configurarGuardado();

        WebpayPlusTransactionStatusResponse status = new WebpayPlusTransactionStatusResponse();
        status.setStatus("AUTHORIZED");
        status.setResponseCode((byte) 0);
        status.setAuthorizationCode("AUTH123");
        status.setPaymentTypeCode("TD");
        status.setAmount(10000);
        status.setTransactionDate("2026-01-01T10:00:00");
        status.setCardDetail(new CardDetail("1234"));
        when(transaction.status("token-123")).thenReturn(status);

        var respuesta = webpayService.consultarEstado("token-123");

        assertThat(respuesta.estado()).isEqualTo("APROBADA");
        assertThat(respuesta.codigoAutorizacion()).isEqualTo("AUTH123");
        assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.APROBADA);
    }

    @Test
    void debeMantenerEstadoLocalSiFallaLaConsultaAWebpay() throws Exception {
        TransaccionWebpay transaccion = transaccion(EstadoTransaccion.INICIADA);
        when(transaccionRepository.findByToken("token-123")).thenReturn(Optional.of(transaccion));
        when(transaction.status("token-123")).thenThrow(new RuntimeException("sin conexion"));

        var respuesta = webpayService.consultarEstado("token-123");

        assertThat(respuesta.estado()).isEqualTo("INICIADA");
        verify(transaccionRepository, never()).save(any(TransaccionWebpay.class));
    }

    @Test
    void debeRechazarReembolsoDeTransaccionNoAprobada() {
        when(transaccionRepository.findByToken("token-123"))
                .thenReturn(Optional.of(transaccion(EstadoTransaccion.INICIADA)));

        assertThatThrownBy(() -> webpayService.reembolsar(
                new ReembolsoRequest("token-123", new BigDecimal("10000"))))
                .isInstanceOf(WebpayException.class)
                .hasMessage("Solo se pueden reembolsar transacciones aprobadas");

        verifyNoInteractions(transaction);
        verify(transaccionRepository, never()).save(any(TransaccionWebpay.class));
    }

    @Test
    void debeReembolsarUnaTransaccionAprobada() throws Exception {
        TransaccionWebpay transaccion = transaccion(EstadoTransaccion.APROBADA);
        when(transaccionRepository.findByToken("token-123")).thenReturn(Optional.of(transaccion));
        configurarGuardado();

        WebpayPlusTransactionRefundResponse refund = new WebpayPlusTransactionRefundResponse();
        refund.setResponseCode((byte) 0);
        refund.setNullifiedAmount(10000);
        refund.setType("REEMBOLSO");
        refund.setAuthorizationCode("AUTH123");
        when(transaction.refund("token-123", 10000.0)).thenReturn(refund);

        var respuesta = webpayService.reembolsar(
                new ReembolsoRequest("token-123", new BigDecimal("10000")));

        assertThat(respuesta.estado()).isEqualTo("REEMBOLSADA");
        assertThat(respuesta.codigoRespuesta()).isEqualTo(0);
        assertThat(respuesta.montoReembolsado()).isEqualByComparingTo("10000.00");
        assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.REEMBOLSADA);
    }

    @Test
    void debeProcesarRetornoConTokenWs() {
        when(transaccionRepository.findByToken("token-123"))
                .thenReturn(Optional.of(transaccion(EstadoTransaccion.APROBADA)));

        var respuesta = webpayService.procesarRetorno(Map.of("token_ws", "token-123"));

        assertThat(respuesta.estado()).isEqualTo("APROBADA");
        assertThat(respuesta.mensaje()).isEqualTo("Pago aprobado por Webpay");
        assertThat(respuesta.pago()).isNotNull();
        verifyNoInteractions(transaction);
    }

    @Test
    void debeMarcarComoAbortadoElRetornoConTbkToken() {
        TransaccionWebpay transaccion = transaccion(EstadoTransaccion.INICIADA);
        when(transaccionRepository.findByToken("token-123")).thenReturn(Optional.of(transaccion));
        configurarGuardado();

        var respuesta = webpayService.procesarRetorno(Map.of("TBK_TOKEN", "token-123"));

        assertThat(respuesta.estado()).isEqualTo("ABORTADA");
        assertThat(respuesta.mensaje()).isEqualTo("Pago abortado por el usuario");
        assertThat(respuesta.pago()).isNotNull();
        assertThat(transaccion.getEstado()).isEqualTo(EstadoTransaccion.ABORTADA);
        verify(transaccionRepository).save(transaccion);
    }

    @Test
    void debeInformarRetornoAbortadoSinTransaccionAsociada() {
        when(transaccionRepository.findByToken("token-123")).thenReturn(Optional.empty());

        var respuesta = webpayService.procesarRetorno(Map.of("TBK_TOKEN", "token-123"));

        assertThat(respuesta.estado()).isEqualTo("ABORTADA");
        assertThat(respuesta.pago()).isNull();
        verify(transaccionRepository, never()).save(any(TransaccionWebpay.class));
    }

    @Test
    void debeRechazarRetornoSinTokens() {
        assertThatThrownBy(() -> webpayService.procesarRetorno(Map.of()))
                .isInstanceOf(WebpayException.class)
                .hasMessage("No se recibio token_ws ni TBK_TOKEN en el retorno de Webpay");
    }

    @Test
    void debeListarPagos() {
        when(transaccionRepository.findAllByOrderByIdDesc())
                .thenReturn(List.of(transaccion(EstadoTransaccion.APROBADA)));

        var respuesta = webpayService.listar();

        assertThat(respuesta).hasSize(1);
        assertThat(respuesta.get(0).estado()).isEqualTo("APROBADA");
    }

    private void configurarGuardado() {
        when(transaccionRepository.save(any(TransaccionWebpay.class))).thenAnswer(invocation -> {
            TransaccionWebpay transaccion = invocation.getArgument(0);
            if (transaccion.getId() == null) {
                transaccion.setId(1L);
            }
            return transaccion;
        });
    }

    private TransaccionWebpay transaccion(EstadoTransaccion estado) {
        TransaccionWebpay transaccion = new TransaccionWebpay();
        transaccion.setId(1L);
        transaccion.setOrdenCompra("ORD-1001");
        transaccion.setIdSesion("SES-1001");
        transaccion.setMonto(new BigDecimal("10000.00"));
        transaccion.setUrlRetorno("http://retorno");
        transaccion.setToken("token-123");
        transaccion.setEstado(estado);
        transaccion.setFechaCreacion(Instant.parse("2026-01-01T10:00:00Z"));
        return transaccion;
    }
}

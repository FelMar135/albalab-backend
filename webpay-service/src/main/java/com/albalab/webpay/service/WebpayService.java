package com.albalab.webpay.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.albalab.webpay.dto.ConfirmarPagoRequest;
import com.albalab.webpay.dto.CrearPagoRequest;
import com.albalab.webpay.dto.CrearPagoResponse;
import com.albalab.webpay.dto.PagoResponse;
import com.albalab.webpay.dto.ReembolsoRequest;
import com.albalab.webpay.dto.ReembolsoResponse;
import com.albalab.webpay.dto.RetornoWebpayResponse;
import com.albalab.webpay.exception.OrdenDuplicadaException;
import com.albalab.webpay.exception.RecursoNoEncontradoException;
import com.albalab.webpay.exception.WebpayException;
import com.albalab.webpay.model.EstadoTransaccion;
import com.albalab.webpay.model.TransaccionWebpay;
import com.albalab.webpay.repository.TransaccionWebpayRepository;

import cl.transbank.common.IntegrationApiKeys;
import cl.transbank.common.IntegrationCommerceCodes;
import cl.transbank.common.IntegrationType;
import cl.transbank.webpay.common.WebpayOptions;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionRefundResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionStatusResponse;

@Service
public class WebpayService {

    private static final Logger log = LoggerFactory.getLogger(WebpayService.class);

    private final TransaccionWebpayRepository transaccionRepository;
    private final WebpayPlus.Transaction transaction;

    public WebpayService(
            TransaccionWebpayRepository transaccionRepository,
            @Value("${webpay.commerce-code:}") String commerceCode,
            @Value("${webpay.api-key:}") String apiKey,
            @Value("${webpay.environment:TEST}") String environment
    ) {
        this.transaccionRepository = transaccionRepository;

        String codigoComercio = (commerceCode == null || commerceCode.isBlank())
                ? IntegrationCommerceCodes.WEBPAY_PLUS
                : commerceCode;
        String llaveApi = (apiKey == null || apiKey.isBlank())
                ? IntegrationApiKeys.WEBPAY
                : apiKey;
        IntegrationType tipoIntegracion = "LIVE".equalsIgnoreCase(environment)
                ? IntegrationType.LIVE
                : IntegrationType.TEST;

        this.transaction = new WebpayPlus.Transaction(
                new WebpayOptions(codigoComercio, llaveApi, tipoIntegracion)
        );
    }

    @Transactional
    public CrearPagoResponse crear(CrearPagoRequest request) {
        if (transaccionRepository.existsByOrdenCompra(request.ordenCompra())) {
            throw new OrdenDuplicadaException(
                    "Ya existe una transaccion con la orden de compra " + request.ordenCompra()
            );
        }

        BigDecimal monto = request.monto().setScale(2, RoundingMode.HALF_UP);

        TransaccionWebpay transaccion = new TransaccionWebpay();
        transaccion.setOrdenCompra(request.ordenCompra());
        transaccion.setIdSesion(request.idSesion());
        transaccion.setMonto(monto);
        transaccion.setUrlRetorno(request.urlRetorno());
        transaccion.setEstado(EstadoTransaccion.INICIADA);
        transaccion = transaccionRepository.save(transaccion);

        try {
            WebpayPlusTransactionCreateResponse response = transaction.create(
                    request.ordenCompra(),
                    request.idSesion(),
                    monto.doubleValue(),
                    request.urlRetorno()
            );

            transaccion.setToken(response.getToken());
            transaccion.setFechaActualizacion(Instant.now());
            transaccion = transaccionRepository.save(transaccion);

            return new CrearPagoResponse(
                    transaccion.getId(),
                    transaccion.getOrdenCompra(),
                    response.getToken(),
                    response.getUrl(),
                    transaccion.getEstado().name()
            );
        } catch (Exception ex) {
            log.error("Error creando transaccion Webpay para orden {}", request.ordenCompra(), ex);
            transaccion.setEstado(EstadoTransaccion.ERROR);
            transaccionRepository.save(transaccion);
            throw new WebpayException("No se pudo crear la transaccion en Webpay", ex);
        }
    }

    @Transactional
    public PagoResponse confirmar(ConfirmarPagoRequest request) {
        return confirmar(request.token());
    }

    @Transactional
    public PagoResponse confirmar(String token) {
        TransaccionWebpay transaccion = buscarPorToken(token);

        if (esEstadoFinal(transaccion.getEstado())) {
            return PagoResponse.from(transaccion);
        }

        try {
            WebpayPlusTransactionCommitResponse response = transaction.commit(token);
            aplicarRespuesta(transaccion, response);
            return PagoResponse.from(transaccionRepository.save(transaccion));
        } catch (Exception ex) {
            log.error("Error confirmando transaccion Webpay con token {}", token, ex);
            transaccion.setEstado(EstadoTransaccion.ERROR);
            transaccion.setFechaActualizacion(Instant.now());
            transaccionRepository.save(transaccion);
            throw new WebpayException("No se pudo confirmar la transaccion en Webpay", ex);
        }
    }

    @Transactional
    public PagoResponse consultarEstado(String token) {
        TransaccionWebpay transaccion = buscarPorToken(token);

        if (esEstadoFinal(transaccion.getEstado())) {
            return PagoResponse.from(transaccion);
        }

        try {
            WebpayPlusTransactionStatusResponse response = transaction.status(token);
            aplicarEstado(transaccion, response);
            return PagoResponse.from(transaccionRepository.save(transaccion));
        } catch (Exception ex) {
            log.warn("No se pudo consultar Webpay para token {}. Se devuelve el estado local.", token, ex);
            return PagoResponse.from(transaccion);
        }
    }

    @Transactional
    public ReembolsoResponse reembolsar(ReembolsoRequest request) {
        TransaccionWebpay transaccion = buscarPorToken(request.token());

        if (transaccion.getEstado() != EstadoTransaccion.APROBADA) {
            throw new WebpayException("Solo se pueden reembolsar transacciones aprobadas");
        }

        try {
            WebpayPlusTransactionRefundResponse response = transaction.refund(
                    request.token(),
                    request.monto().doubleValue()
            );

            if (response.getResponseCode() == 0) {
                transaccion.setEstado(EstadoTransaccion.REEMBOLSADA);
            }
            transaccion.setFechaActualizacion(Instant.now());
            transaccionRepository.save(transaccion);

            return new ReembolsoResponse(
                    request.token(),
                    transaccion.getEstado().name(),
                    (int) response.getResponseCode(),
                    BigDecimal.valueOf(response.getNullifiedAmount()).setScale(2, RoundingMode.HALF_UP),
                    response.getType(),
                    response.getAuthorizationCode()
            );
        } catch (Exception ex) {
            log.error("Error reembolsando transaccion Webpay con token {}", request.token(), ex);
            throw new WebpayException("No se pudo reembolsar la transaccion en Webpay", ex);
        }
    }

    @Transactional
    public RetornoWebpayResponse procesarRetorno(Map<String, String> parametros) {
        String tokenWs = parametros.get("token_ws");

        if (tokenWs != null && !tokenWs.isBlank()) {
            PagoResponse pago = confirmar(tokenWs);
            String mensaje = "APROBADA".equals(pago.estado())
                    ? "Pago aprobado por Webpay"
                    : "Pago rechazado por Webpay";
            return new RetornoWebpayResponse(pago.estado(), mensaje, pago);
        }

        String tokenAbortado = parametros.get("TBK_TOKEN");
        if (tokenAbortado != null && !tokenAbortado.isBlank()) {
            TransaccionWebpay transaccion = transaccionRepository.findByToken(tokenAbortado).orElse(null);
            if (transaccion != null) {
                transaccion.setEstado(EstadoTransaccion.ABORTADA);
                transaccion.setFechaActualizacion(Instant.now());
                transaccionRepository.save(transaccion);
                return new RetornoWebpayResponse(
                        transaccion.getEstado().name(),
                        "Pago abortado por el usuario",
                        PagoResponse.from(transaccion)
                );
            }
            return new RetornoWebpayResponse(
                    EstadoTransaccion.ABORTADA.name(),
                    "Pago abortado por el usuario",
                    null
            );
        }

        throw new WebpayException("No se recibio token_ws ni TBK_TOKEN en el retorno de Webpay");
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return transaccionRepository.findAllByOrderByIdDesc()
                .stream()
                .map(PagoResponse::from)
                .toList();
    }

    private void aplicarRespuesta(
            TransaccionWebpay transaccion,
            WebpayPlusTransactionCommitResponse response
    ) {
        transaccion.setCodigoRespuesta((int) response.getResponseCode());
        transaccion.setEstado(response.getResponseCode() == 0
                ? EstadoTransaccion.APROBADA
                : EstadoTransaccion.RECHAZADA);
        transaccion.setCodigoAutorizacion(response.getAuthorizationCode());
        transaccion.setTipoPago(response.getPaymentTypeCode());
        transaccion.setMontoFinal(BigDecimal.valueOf(response.getAmount()).setScale(2, RoundingMode.HALF_UP));
        transaccion.setFechaTransaccion(response.getTransactionDate());
        transaccion.setUltimosDigitosTarjeta(obtenerTarjeta(response));
        transaccion.setFechaActualizacion(Instant.now());
    }

    private void aplicarEstado(
            TransaccionWebpay transaccion,
            WebpayPlusTransactionStatusResponse response
    ) {
        String estadoWebpay = response.getStatus();
        if (estadoWebpay != null) {
            switch (estadoWebpay.toUpperCase()) {
                case "AUTHORIZED" -> transaccion.setEstado(EstadoTransaccion.APROBADA);
                case "FAILED" -> transaccion.setEstado(EstadoTransaccion.RECHAZADA);
                case "NULLIFIED", "PARTIALLY_NULLIFIED" -> transaccion.setEstado(EstadoTransaccion.REEMBOLSADA);
                default -> {
                    // INITIALIZED u otros estados intermedios se mantienen como INICIADA.
                }
            }
        }

        transaccion.setCodigoRespuesta((int) response.getResponseCode());
        if (response.getAuthorizationCode() != null) {
            transaccion.setCodigoAutorizacion(response.getAuthorizationCode());
        }
        if (response.getPaymentTypeCode() != null) {
            transaccion.setTipoPago(response.getPaymentTypeCode());
        }
        if (response.getAmount() > 0) {
            transaccion.setMontoFinal(
                    BigDecimal.valueOf(response.getAmount()).setScale(2, RoundingMode.HALF_UP)
            );
        }
        if (response.getTransactionDate() != null) {
            transaccion.setFechaTransaccion(response.getTransactionDate());
        }
        String tarjeta = obtenerTarjeta(response);
        if (tarjeta != null) {
            transaccion.setUltimosDigitosTarjeta(tarjeta);
        }
        transaccion.setFechaActualizacion(Instant.now());
    }

    private String obtenerTarjeta(WebpayPlusTransactionStatusResponse response) {
        if (response.getCardDetail() == null) {
            return null;
        }
        return response.getCardDetail().getCardNumber();
    }

    private boolean esEstadoFinal(EstadoTransaccion estado) {
        return estado == EstadoTransaccion.APROBADA
                || estado == EstadoTransaccion.RECHAZADA
                || estado == EstadoTransaccion.ABORTADA
                || estado == EstadoTransaccion.REEMBOLSADA;
    }

    private TransaccionWebpay buscarPorToken(String token) {
        return transaccionRepository.findByToken(token)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Transaccion Webpay no encontrada para el token indicado"
                ));
    }
}

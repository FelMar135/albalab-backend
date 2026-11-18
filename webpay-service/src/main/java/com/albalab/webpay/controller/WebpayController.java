package com.albalab.webpay.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.albalab.webpay.dto.ConfirmarPagoRequest;
import com.albalab.webpay.dto.CrearPagoRequest;
import com.albalab.webpay.dto.CrearPagoResponse;
import com.albalab.webpay.dto.PagoResponse;
import com.albalab.webpay.dto.ReembolsoRequest;
import com.albalab.webpay.dto.ReembolsoResponse;
import com.albalab.webpay.dto.RetornoWebpayResponse;
import com.albalab.webpay.service.WebpayService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/webpay")
public class WebpayController {

    private final WebpayService webpayService;

    public WebpayController(WebpayService webpayService) {
        this.webpayService = webpayService;
    }

    @PostMapping("/crear")
    public ResponseEntity<CrearPagoResponse> crear(@Valid @RequestBody CrearPagoRequest request) {
        return ResponseEntity.ok(webpayService.crear(request));
    }

    @PostMapping("/confirmar")
    public ResponseEntity<PagoResponse> confirmar(@Valid @RequestBody ConfirmarPagoRequest request) {
        return ResponseEntity.ok(webpayService.confirmar(request));
    }

    @GetMapping("/estado/{token}")
    public ResponseEntity<PagoResponse> estado(@PathVariable String token) {
        return ResponseEntity.ok(webpayService.consultarEstado(token));
    }

    @PostMapping("/reembolsar")
    public ResponseEntity<ReembolsoResponse> reembolsar(@Valid @RequestBody ReembolsoRequest request) {
        return ResponseEntity.ok(webpayService.reembolsar(request));
    }

    /**
     * URL de retorno que se configura en Webpay. Transbank envia aqui el
     * parametro token_ws mediante POST (application/x-www-form-urlencoded).
     */
    @PostMapping("/retorno")
    public ResponseEntity<RetornoWebpayResponse> retornoPost(
            @RequestParam Map<String, String> parametros
    ) {
        return ResponseEntity.ok(webpayService.procesarRetorno(parametros));
    }

    /**
     * Alias GET para pruebas manuales y clientes que redirigen por query string.
     */
    @GetMapping("/retorno")
    public ResponseEntity<RetornoWebpayResponse> retornoGet(
            @RequestParam Map<String, String> parametros
    ) {
        return ResponseEntity.ok(webpayService.procesarRetorno(parametros));
    }

    @GetMapping
    public ResponseEntity<List<PagoResponse>> listar() {
        return ResponseEntity.ok(webpayService.listar());
    }
}

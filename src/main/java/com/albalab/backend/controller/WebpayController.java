package com.albalab.backend.controller;

import com.albalab.backend.model.WebpayRequest;
import com.albalab.backend.service.WebpayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webpay")
public class WebpayController {

    @Autowired
    private WebpayService webpayService;

    @PostMapping("/crear")
    public ResponseEntity<?> crearPago(@RequestBody WebpayRequest request) {
        try {
            var response = webpayService.crearTransaccion(
                    request.getOrdenCompra(),
                    request.getIdSesion(),
                    request.getMonto(),
                    request.getUrlRetorno()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
package com.albalab.historial.controller;

import com.albalab.historial.dto.CompraRequest;
import com.albalab.historial.model.Compra;
import com.albalab.historial.service.CompraService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/historial")
public class CompraController {

    private final CompraService service;

    public CompraController(CompraService service) {
        this.service = service;
    }

    @PostMapping("/compras")
    public ResponseEntity<Compra> guardar(
            Authentication authentication,
            @RequestBody CompraRequest request
    ) {
        return ResponseEntity.ok(service.guardar(authentication.getName(), request));
    }

    @GetMapping("/compras")
    public ResponseEntity<List<Compra>> listar(
            Authentication authentication
    ) {
        return ResponseEntity.ok(service.listar(authentication.getName()));
    }
}

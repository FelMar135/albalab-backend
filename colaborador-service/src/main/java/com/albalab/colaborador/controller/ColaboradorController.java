package com.albalab.colaborador.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.albalab.colaborador.dto.ColaboradorRequest;
import com.albalab.colaborador.dto.ColaboradorResponse;
import com.albalab.colaborador.service.ColaboradorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {

    private final ColaboradorService colaboradorService;

    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @GetMapping
    public ResponseEntity<List<ColaboradorResponse>> listar() {
        return ResponseEntity.ok(colaboradorService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColaboradorResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(colaboradorService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ColaboradorResponse> crear(@Valid @RequestBody ColaboradorRequest request) {
        return ResponseEntity.ok(colaboradorService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColaboradorResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ColaboradorRequest request
    ) {
        return ResponseEntity.ok(colaboradorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        colaboradorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

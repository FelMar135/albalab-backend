package com.albalab.colaborador.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.albalab.colaborador.dto.ColaboradorPublicoResponse;
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
    public ResponseEntity<List<ColaboradorResponse>> listar(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(colaboradorService.listarTodos(jwt));
    }

    @GetMapping("/admin/todos")
    public ResponseEntity<List<ColaboradorResponse>> listarTodos(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(colaboradorService.listarTodos(jwt));
    }

    @GetMapping("/mios")
    public ResponseEntity<List<ColaboradorResponse>> listarMios(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(colaboradorService.listarMios(jwt));
    }

    @GetMapping("/publicos")
    public ResponseEntity<List<ColaboradorPublicoResponse>> listarPublicos() {
        return ResponseEntity.ok(colaboradorService.listarPublicos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColaboradorResponse> obtener(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(colaboradorService.obtener(jwt, id));
    }

    @PostMapping
    public ResponseEntity<ColaboradorResponse> crear(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ColaboradorRequest request
    ) {
        return ResponseEntity.ok(colaboradorService.crear(jwt, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColaboradorResponse> actualizar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id,
            @Valid @RequestBody ColaboradorRequest request
    ) {
        return ResponseEntity.ok(colaboradorService.actualizar(jwt, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id
    ) {
        colaboradorService.eliminar(jwt, id);
        return ResponseEntity.noContent().build();
    }
}

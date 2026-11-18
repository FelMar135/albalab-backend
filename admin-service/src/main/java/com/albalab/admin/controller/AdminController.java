package com.albalab.admin.controller;

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

import com.albalab.admin.dto.ActualizarAdminRequest;
import com.albalab.admin.dto.AdminResponse;
import com.albalab.admin.dto.MensajeResponse;
import com.albalab.admin.dto.RegistroAdminRequest;
import com.albalab.admin.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<AdminResponse>> listar() {
        return ResponseEntity.ok(adminService.listar());
    }

    @GetMapping("/me")
    public ResponseEntity<AdminResponse> perfil(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(adminService.obtenerPorEmail(jwt.getSubject()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<MensajeResponse> crear(@Valid @RequestBody RegistroAdminRequest request) {
        return ResponseEntity.ok(adminService.registrar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarAdminRequest request
    ) {
        return ResponseEntity.ok(adminService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        adminService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

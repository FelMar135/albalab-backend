package com.albalab.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.albalab.admin.dto.LoginAdminRequest;
import com.albalab.admin.dto.LoginAdminResponse;
import com.albalab.admin.dto.MensajeResponse;
import com.albalab.admin.dto.RegistroAdminRequest;
import com.albalab.admin.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminService adminService;

    public AdminAuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginAdminResponse> login(@Valid @RequestBody LoginAdminRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }

    @PostMapping("/registro")
    public ResponseEntity<MensajeResponse> registrar(@Valid @RequestBody RegistroAdminRequest request) {
        return ResponseEntity.ok(adminService.registrar(request));
    }
}

package com.albalab.admin.dto;

import java.time.Instant;

import com.albalab.admin.model.Administrador;

public record AdminResponse(
        Long id,
        String email,
        String role,
        Instant fechaCreacion
) {
    public static AdminResponse from(Administrador administrador) {
        return new AdminResponse(
                administrador.getId(),
                administrador.getEmail(),
                administrador.getRole(),
                administrador.getFechaCreacion()
        );
    }
}

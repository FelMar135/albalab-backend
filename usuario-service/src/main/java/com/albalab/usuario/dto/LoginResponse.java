package com.albalab.usuario.dto;

public record LoginResponse(
        String status,
        String token,
        String role,
        String email,
        long expiresInSeconds
) {
}

package com.albalab.admin.dto;

public record LoginAdminResponse(
        String status,
        String token,
        String role,
        String email,
        long expiresInSeconds
) {
}

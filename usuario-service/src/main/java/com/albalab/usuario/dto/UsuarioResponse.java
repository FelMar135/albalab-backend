package com.albalab.usuario.dto;

import java.time.Instant;

import com.albalab.usuario.model.Usuario;

public record UsuarioResponse(
        Long id,
        String email,
        String role,
        Instant fechaCreacion
) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole(),
                usuario.getFechaCreacion()
        );
    }
}

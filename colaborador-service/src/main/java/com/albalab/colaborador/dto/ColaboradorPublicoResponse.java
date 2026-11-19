package com.albalab.colaborador.dto;

import java.time.LocalDateTime;

import com.albalab.colaborador.model.Colaborador;

public record ColaboradorPublicoResponse(
        Long id,
        String nombre,
        String fotoUri,
        boolean esAnonimo,
        LocalDateTime fecha
) {
    public static ColaboradorPublicoResponse from(Colaborador colaborador) {
        boolean anonimo = colaborador.isEsAnonimo();

        return new ColaboradorPublicoResponse(
                colaborador.getId(),
                anonimo ? "Anonimo" : colaborador.getNombre(),
                anonimo ? null : colaborador.getFotoUri(),
                anonimo,
                colaborador.getFecha()
        );
    }
}

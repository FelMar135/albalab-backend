package com.albalab.colaborador.dto;

import com.albalab.colaborador.model.Colaborador;

public record ColaboradorResponse(
        Long id,
        String nombre,
        String fotoUri,
        Integer montoDonacion
) {
    public static ColaboradorResponse from(Colaborador colaborador) {
        return new ColaboradorResponse(
                colaborador.getId(),
                colaborador.getNombre(),
                colaborador.getFotoUri(),
                colaborador.getMontoDonacion()
        );
    }
}

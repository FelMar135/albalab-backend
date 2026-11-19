package com.albalab.colaborador.dto;

import java.time.LocalDateTime;

import com.albalab.colaborador.model.Colaborador;

public record ColaboradorResponse(
        Long id,
        Long usuarioId,
        String usuarioEmail,
        String nombre,
        String fotoUri,
        Integer montoDonacion,
        boolean esAnonimo,
        LocalDateTime fecha,
        String estado,
        String webpayOrden
) {
    public static ColaboradorResponse from(Colaborador colaborador) {
        return new ColaboradorResponse(
                colaborador.getId(),
                colaborador.getUsuarioId(),
                colaborador.getUsuarioEmail(),
                colaborador.getNombre(),
                colaborador.getFotoUri(),
                colaborador.getMontoDonacion(),
                colaborador.isEsAnonimo(),
                colaborador.getFecha(),
                colaborador.getEstado(),
                colaborador.getWebpayOrden()
        );
    }
}

package com.albalab.colaborador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ColaboradorRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,

        @Size(max = 500, message = "La URL de la foto no puede superar los 500 caracteres")
        String fotoUri,

        @PositiveOrZero(message = "El monto de donacion no puede ser negativo")
        Integer montoDonacion
) {
}

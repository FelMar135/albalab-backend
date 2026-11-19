package com.albalab.colaborador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ColaboradorRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,

        @Size(max = 500, message = "La URL de la foto no puede superar los 500 caracteres")
        String fotoUri,

        @NotNull(message = "El monto de donacion es obligatorio")
        @Positive(message = "El monto de donacion debe ser mayor a cero")
        Integer montoDonacion,

        Boolean esAnonimo,

        @Size(max = 100, message = "La orden de Webpay no puede superar los 100 caracteres")
        String webpayOrden
) {
}

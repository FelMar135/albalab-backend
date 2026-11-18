package com.albalab.producto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor a cero")
        Integer precio,

        @Size(max = 1000, message = "La descripcion no puede superar los 1000 caracteres")
        String descripcion,

        @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres")
        String imagenUrl,

        Boolean disponible
) {
}
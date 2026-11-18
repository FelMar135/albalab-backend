package com.albalab.producto.dto;

import com.albalab.producto.model.Producto;

public record ProductoResponse(
        Long id,
        String nombre,
        Integer precio,
        String descripcion,
        String imagenUrl,
        Boolean disponible
) {
    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getDescripcion(),
                producto.getImagenUrl(),
                producto.getDisponible()
        );
    }
}
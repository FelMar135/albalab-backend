package com.albalab.producto.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.albalab.producto.dto.ProductoRequest;
import com.albalab.producto.dto.ProductoResponse;
import com.albalab.producto.exception.RecursoNoEncontradoException;
import com.albalab.producto.model.Producto;
import com.albalab.producto.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findAllByOrderByIdAsc()
                .stream()
                .map(ProductoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.from(buscarPorId(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto();
        producto.setFechaCreacion(LocalDateTime.now());
        aplicarCambios(producto, request);
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarPorId(id);
        aplicarCambios(producto, request);
        return ProductoResponse.from(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        productoRepository.delete(producto);
    }

    private void aplicarCambios(Producto producto, ProductoRequest request) {
        producto.setNombre(request.nombre().trim());
        producto.setPrecio(request.precio());
        producto.setDescripcion(request.descripcion());
        producto.setImagenUrl(request.imagenUrl());
        producto.setDisponible(request.disponible() == null ? true : request.disponible());
    }

    private Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado con id " + id
                ));
    }
}
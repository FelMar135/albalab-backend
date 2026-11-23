package com.albalab.historial.service;

import com.albalab.historial.dto.CompraRequest;
import com.albalab.historial.model.Compra;
import com.albalab.historial.repository.CompraRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompraService {

    private final CompraRepository repository;

    public CompraService(CompraRepository repository) {
        this.repository = repository;
    }

    public Compra guardar(String usuarioEmail, CompraRequest request) {
        Compra compra = new Compra(
                usuarioEmail,
                System.currentTimeMillis(),
                request.getTotal(),
                request.getProductos()
        );
        return repository.save(compra);
    }

    public List<Compra> listar(String usuarioEmail) {
        return repository.findByUsuarioEmailOrderByFechaDesc(usuarioEmail);
    }
}

package com.albalab.historial.repository;

import com.albalab.historial.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuarioEmailOrderByFechaDesc(String usuarioEmail);
}

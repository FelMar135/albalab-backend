package com.albalab.producto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.albalab.producto.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findAllByOrderByIdAsc();
}

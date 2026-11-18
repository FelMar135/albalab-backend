package com.albalab.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.albalab.admin.model.Administrador;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    Optional<Administrador> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Administrador> findAllByOrderByIdAsc();
}

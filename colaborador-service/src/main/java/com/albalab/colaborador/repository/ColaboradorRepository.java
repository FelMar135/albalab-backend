package com.albalab.colaborador.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.albalab.colaborador.model.Colaborador;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {

    List<Colaborador> findAllByOrderByIdAsc();
}

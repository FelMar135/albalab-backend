package com.albalab.colaborador.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.albalab.colaborador.model.Colaborador;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {

    List<Colaborador> findAllByOrderByFechaDescIdDesc();

    List<Colaborador> findByEstadoOrderByFechaDescIdDesc(String estado);

    Optional<Colaborador> findByWebpayOrden(String webpayOrden);

    @Query("""
            SELECT c
            FROM Colaborador c
            WHERE c.usuarioId = :usuarioId
               OR LOWER(c.usuarioEmail) = LOWER(:usuarioEmail)
            ORDER BY c.fecha DESC, c.id DESC
            """)
    List<Colaborador> buscarPorUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("usuarioEmail") String usuarioEmail
    );
}

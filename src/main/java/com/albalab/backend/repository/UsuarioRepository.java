package com.albalab.backend.repository;

import com.albalab.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Función mágica que buscará si un correo ya existe en la base de datos
    Optional<Usuario> findByEmail(String email);
}
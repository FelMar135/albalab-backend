package com.albalab.backend.service;

import com.albalab.backend.model.Usuario;
import com.albalab.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean registrar(Usuario nuevoUsuario) {
        if (usuarioRepository.findByEmail(nuevoUsuario.getEmail()).isPresent()) {
            return false; // El correo ya existe
        }
        
        String hash = passwordEncoder.encode(nuevoUsuario.getPassword());
        nuevoUsuario.setPassword(hash);
        usuarioRepository.save(nuevoUsuario);
        return true;
    }

    public boolean login(String email, String password) {
        Optional<Usuario> usuarioEnBD = usuarioRepository.findByEmail(email);
        
        return usuarioEnBD.isPresent() && 
               passwordEncoder.matches(password, usuarioEnBD.get().getPassword());
    }
}
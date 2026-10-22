package com.albalab.backend.controller;

import com.albalab.backend.model.Usuario;
import com.albalab.backend.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor para inyectar la base de datos y el motor de encriptación
    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. RUTA DE REGISTRO: http://localhost:9091/api/auth/registro
    @PostMapping("/registro")
    public ResponseEntity<String> registrar(@RequestBody Usuario nuevoUsuario) {
        // Verificar si el correo ya existe
        if (usuarioRepository.findByEmail(nuevoUsuario.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: El correo ya está registrado");
        }
        
        // Encriptar (hashear) la contraseña antes de guardarla en MySQL
        String hash = passwordEncoder.encode(nuevoUsuario.getPassword());
        nuevoUsuario.setPassword(hash);
        
        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.ok("Usuario registrado con éxito");
    }

    // 2. RUTA DE LOGIN: http://localhost:9091/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Usuario loginData) {
        // Buscar al usuario por su correo
        Optional<Usuario> usuarioEnBD = usuarioRepository.findByEmail(loginData.getEmail());
        
        if (usuarioEnBD.isPresent()) {
            // Comparamos la contraseña escrita en la app con la encriptada en la base de datos
            if (passwordEncoder.matches(loginData.getPassword(), usuarioEnBD.get().getPassword())) {
                return ResponseEntity.ok("Login exitoso");
            }
        }
        
        return ResponseEntity.status(401).body("Credenciales incorrectas");
    }
}
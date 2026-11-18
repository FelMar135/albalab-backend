package com.albalab.usuario.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.albalab.usuario.dto.ActualizarUsuarioRequest;
import com.albalab.usuario.dto.LoginRequest;
import com.albalab.usuario.dto.LoginResponse;
import com.albalab.usuario.dto.MensajeResponse;
import com.albalab.usuario.dto.RegistroRequest;
import com.albalab.usuario.dto.UsuarioResponse;
import com.albalab.usuario.exception.CorreoDuplicadoException;
import com.albalab.usuario.exception.CredencialesInvalidasException;
import com.albalab.usuario.exception.RecursoNoEncontradoException;
import com.albalab.usuario.model.Usuario;
import com.albalab.usuario.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public MensajeResponse registrar(RegistroRequest request) {
        String email = normalizarEmail(request.email());

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new CorreoDuplicadoException("El correo ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRole("USER");
        usuarioRepository.save(usuario);

        return new MensajeResponse("Usuario registrado con exito");
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(request.email()))
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("Credenciales incorrectas");
        }

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRole());
        return new LoginResponse(
                "ok",
                token,
                usuario.getRole(),
                usuario.getEmail(),
                jwtService.getExpirationSeconds()
        );
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByOrderByIdAsc()
                .stream()
                .map(UsuarioResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.from(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = buscarPorId(id);
        String email = normalizarEmail(request.email());

        if (!usuario.getEmail().equalsIgnoreCase(email)
                && usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new CorreoDuplicadoException("El correo ya esta registrado");
        }

        usuario.setEmail(email);
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id " + id));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

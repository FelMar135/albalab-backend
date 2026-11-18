package com.albalab.admin.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.albalab.admin.dto.ActualizarAdminRequest;
import com.albalab.admin.dto.AdminResponse;
import com.albalab.admin.dto.LoginAdminRequest;
import com.albalab.admin.dto.LoginAdminResponse;
import com.albalab.admin.dto.MensajeResponse;
import com.albalab.admin.dto.RegistroAdminRequest;
import com.albalab.admin.exception.CorreoDuplicadoException;
import com.albalab.admin.exception.CredencialesInvalidasException;
import com.albalab.admin.exception.RecursoNoEncontradoException;
import com.albalab.admin.model.Administrador;
import com.albalab.admin.repository.AdministradorRepository;

@Service
public class AdminService {

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminService(
            AdministradorRepository administradorRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public MensajeResponse registrar(RegistroAdminRequest request) {
        String email = normalizarEmail(request.email());

        if (administradorRepository.existsByEmailIgnoreCase(email)) {
            throw new CorreoDuplicadoException("El correo ya esta registrado como administrador");
        }

        Administrador administrador = new Administrador();
        administrador.setEmail(email);
        administrador.setPassword(passwordEncoder.encode(request.password()));
        administrador.setRole("ADMIN");
        administradorRepository.save(administrador);

        return new MensajeResponse("Administrador registrado con exito");
    }

    @Transactional(readOnly = true)
    public LoginAdminResponse login(LoginAdminRequest request) {
        Administrador administrador = administradorRepository
                .findByEmailIgnoreCase(normalizarEmail(request.email()))
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(request.password(), administrador.getPassword())) {
            throw new CredencialesInvalidasException("Credenciales incorrectas");
        }

        String token = jwtService.generarToken(administrador.getEmail(), administrador.getRole());
        return new LoginAdminResponse(
                "ok",
                token,
                administrador.getRole(),
                administrador.getEmail(),
                jwtService.getExpirationSeconds()
        );
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> listar() {
        return administradorRepository.findAllByOrderByIdAsc()
                .stream()
                .map(AdminResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminResponse obtener(Long id) {
        return AdminResponse.from(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public AdminResponse obtenerPorEmail(String email) {
        Administrador administrador = administradorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado"));
        return AdminResponse.from(administrador);
    }

    @Transactional
    public AdminResponse actualizar(Long id, ActualizarAdminRequest request) {
        Administrador administrador = buscarPorId(id);
        String email = normalizarEmail(request.email());

        if (!administrador.getEmail().equalsIgnoreCase(email)
                && administradorRepository.existsByEmailIgnoreCase(email)) {
            throw new CorreoDuplicadoException("El correo ya esta registrado como administrador");
        }

        administrador.setEmail(email);
        if (request.password() != null && !request.password().isBlank()) {
            administrador.setPassword(passwordEncoder.encode(request.password()));
        }

        return AdminResponse.from(administradorRepository.save(administrador));
    }

    @Transactional
    public void eliminar(Long id) {
        Administrador administrador = buscarPorId(id);
        administradorRepository.delete(administrador);
    }

    private Administrador buscarPorId(Long id) {
        return administradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Administrador no encontrado con id " + id
                ));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

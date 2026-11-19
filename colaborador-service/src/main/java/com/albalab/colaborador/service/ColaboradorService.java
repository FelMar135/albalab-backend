package com.albalab.colaborador.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.albalab.colaborador.dto.ColaboradorPublicoResponse;
import com.albalab.colaborador.dto.ColaboradorRequest;
import com.albalab.colaborador.dto.ColaboradorResponse;
import com.albalab.colaborador.exception.AccesoDenegadoException;
import com.albalab.colaborador.exception.ConflictoException;
import com.albalab.colaborador.exception.RecursoNoEncontradoException;
import com.albalab.colaborador.model.Colaborador;
import com.albalab.colaborador.repository.ColaboradorRepository;

@Service
public class ColaboradorService {

    private static final String ESTADO_APROBADA = "APROBADA";

    private final ColaboradorRepository colaboradorRepository;

    public ColaboradorService(ColaboradorRepository colaboradorRepository) {
        this.colaboradorRepository = colaboradorRepository;
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listarTodos(Jwt jwt) {
        validarAdmin(jwt);

        return colaboradorRepository.findAllByOrderByFechaDescIdDesc()
                .stream()
                .map(ColaboradorResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listarMios(Jwt jwt) {
        String email = email(jwt);
        Long usuarioId = usuarioId(jwt);

        return colaboradorRepository.buscarPorUsuario(usuarioId, email)
                .stream()
                .map(ColaboradorResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ColaboradorPublicoResponse> listarPublicos() {
        return colaboradorRepository.findByEstadoOrderByFechaDescIdDesc(ESTADO_APROBADA)
                .stream()
                .map(ColaboradorPublicoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ColaboradorResponse obtener(Jwt jwt, Long id) {
        Colaborador colaborador = buscarPorId(id);

        if (!esAdmin(jwt) && !esPropietario(colaborador, jwt)) {
            throw new AccesoDenegadoException("No tienes acceso a esta colaboracion");
        }

        return ColaboradorResponse.from(colaborador);
    }

    @Transactional
    public ColaboradorResponse crear(Jwt jwt, ColaboradorRequest request) {
        String email = email(jwt);
        Long usuarioId = usuarioId(jwt);
        String webpayOrden = normalizarTexto(request.webpayOrden());

        if (webpayOrden != null) {
            Optional<Colaborador> existente = colaboradorRepository.findByWebpayOrden(webpayOrden);

            if (existente.isPresent()) {
                Colaborador colaborador = existente.get();

                if (esPropietario(colaborador, jwt)) {
                    return ColaboradorResponse.from(colaborador);
                }

                throw new ConflictoException("La orden de Webpay ya fue registrada");
            }
        }

        Colaborador colaborador = new Colaborador();
        colaborador.setUsuarioId(usuarioId);
        colaborador.setUsuarioEmail(email);
        colaborador.setFecha(LocalDateTime.now());
        colaborador.setEstado(ESTADO_APROBADA);
        aplicarCambios(colaborador, request);

        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    @Transactional
    public ColaboradorResponse actualizar(Jwt jwt, Long id, ColaboradorRequest request) {
        validarAdmin(jwt);

        Colaborador colaborador = buscarPorId(id);
        aplicarCambios(colaborador, request);

        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    @Transactional
    public void eliminar(Jwt jwt, Long id) {
        validarAdmin(jwt);

        Colaborador colaborador = buscarPorId(id);
        colaboradorRepository.delete(colaborador);
    }

    private void aplicarCambios(Colaborador colaborador, ColaboradorRequest request) {
        boolean esAnonimo = Boolean.TRUE.equals(request.esAnonimo());

        colaborador.setNombre(request.nombre().trim());
        colaborador.setMontoDonacion(request.montoDonacion());
        colaborador.setEsAnonimo(esAnonimo);
        colaborador.setFotoUri(esAnonimo ? null : normalizarTexto(request.fotoUri()));

        if (request.webpayOrden() != null) {
            colaborador.setWebpayOrden(normalizarTexto(request.webpayOrden()));
        }
    }

    private Colaborador buscarPorId(Long id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Colaborador no encontrado con id " + id
                ));
    }

    private boolean esPropietario(Colaborador colaborador, Jwt jwt) {
        Long usuarioId = usuarioId(jwt);

        if (usuarioId != null
                && colaborador.getUsuarioId() != null
                && usuarioId.equals(colaborador.getUsuarioId())) {
            return true;
        }

        String email = email(jwt);
        String emailColaborador = colaborador.getUsuarioEmail();

        return emailColaborador != null
                && emailColaborador.equalsIgnoreCase(email);
    }

    private void validarAdmin(Jwt jwt) {
        if (!esAdmin(jwt)) {
            throw new AccesoDenegadoException("Se requiere rol ADMIN para esta operacion");
        }
    }

    private boolean esAdmin(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        String scope = jwt.getClaimAsString("scope");

        return (roles != null && roles.stream().anyMatch("ADMIN"::equalsIgnoreCase))
                || "ADMIN".equalsIgnoreCase(scope);
    }

    private Long usuarioId(Jwt jwt) {
        Object valor = jwt.getClaim("usuarioId");

        if (valor instanceof Number numero) {
            return numero.longValue();
        }

        if (valor instanceof String texto && !texto.isBlank()) {
            try {
                return Long.parseLong(texto);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private String email(Jwt jwt) {
        String email = jwt.getSubject();

        if (email == null || email.isBlank()) {
            throw new AccesoDenegadoException("No se pudo identificar al usuario autenticado");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return null;
        }

        String limpio = texto.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}

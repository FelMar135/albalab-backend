package com.albalab.colaborador.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import com.albalab.colaborador.dto.ColaboradorRequest;
import com.albalab.colaborador.dto.ColaboradorResponse;
import com.albalab.colaborador.exception.AccesoDenegadoException;
import com.albalab.colaborador.exception.ConflictoException;
import com.albalab.colaborador.model.Colaborador;
import com.albalab.colaborador.repository.ColaboradorRepository;

@ExtendWith(MockitoExtension.class)
class ColaboradorServiceTest {

    @Mock
    private ColaboradorRepository colaboradorRepository;

    @InjectMocks
    private ColaboradorService colaboradorService;

    @Test
    void debeListarTodosLosColaboradoresComoAdmin() {
        when(colaboradorRepository.findAllByOrderByFechaDescIdDesc())
                .thenReturn(List.of(colaborador(1L, 7L, "usuario@albalab.cl", false)));

        List<ColaboradorResponse> respuesta = colaboradorService.listarTodos(jwtAdmin());

        assertThat(respuesta).hasSize(1);
        assertThat(respuesta.get(0).nombre()).isEqualTo("Felipe");
    }

    @Test
    void debeRechazarListadoGeneralSiNoEsAdmin() {
        assertThatThrownBy(() -> colaboradorService.listarTodos(jwtUsuario()))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessage("Se requiere rol ADMIN para esta operacion");
    }

    @Test
    void debeListarLasColaboracionesDelUsuario() {
        when(colaboradorRepository.buscarPorUsuario(7L, "usuario@albalab.cl"))
                .thenReturn(List.of(colaborador(1L, 7L, "usuario@albalab.cl", false)));

        List<ColaboradorResponse> respuesta = colaboradorService.listarMios(jwtUsuario());

        assertThat(respuesta).hasSize(1);
        verify(colaboradorRepository).buscarPorUsuario(7L, "usuario@albalab.cl");
    }

    @Test
    void debeListarSoloColaboracionesPublicasAprobadas() {
        when(colaboradorRepository.findByEstadoOrderByFechaDescIdDesc("APROBADA"))
                .thenReturn(List.of(colaborador(2L, 8L, "anonimo@albalab.cl", true)));

        var respuesta = colaboradorService.listarPublicos();

        assertThat(respuesta).hasSize(1);
        assertThat(respuesta.get(0).nombre()).isEqualTo("Anonimo");
        assertThat(respuesta.get(0).fotoUri()).isNull();
    }

    @Test
    void debeObtenerColaboracionSiEsElPropietario() {
        when(colaboradorRepository.findById(1L))
                .thenReturn(Optional.of(colaborador(1L, 7L, "usuario@albalab.cl", false)));

        ColaboradorResponse respuesta = colaboradorService.obtener(jwtUsuario(), 1L);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.usuarioId()).isEqualTo(7L);
    }

    @Test
    void debeRechazarColaboracionDeOtroUsuario() {
        when(colaboradorRepository.findById(1L))
                .thenReturn(Optional.of(colaborador(1L, 7L, "usuario@albalab.cl", false)));

        assertThatThrownBy(() -> colaboradorService.obtener(jwtOtroUsuario(), 1L))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessage("No tienes acceso a esta colaboracion");
    }

    @Test
    void debePermitirObtenerCualquierColaboracionAlAdmin() {
        when(colaboradorRepository.findById(1L))
                .thenReturn(Optional.of(colaborador(1L, 7L, "usuario@albalab.cl", false)));

        ColaboradorResponse respuesta = colaboradorService.obtener(jwtAdmin(), 1L);

        assertThat(respuesta.id()).isEqualTo(1L);
    }

    @Test
    void debeCrearColaboracionNormalizandoLosTextos() {
        when(colaboradorRepository.findByWebpayOrden("ORD-1001")).thenReturn(Optional.empty());
        when(colaboradorRepository.save(any(Colaborador.class))).thenAnswer(invocation -> {
            Colaborador colaborador = invocation.getArgument(0);
            colaborador.setId(10L);
            return colaborador;
        });

        ColaboradorResponse respuesta = colaboradorService.crear(jwtUsuario(),
                new ColaboradorRequest("  Felipe ", " https://ejemplo.cl/foto.png ",
                        5000, false, " ORD-1001 "));

        ArgumentCaptor<Colaborador> captor = ArgumentCaptor.forClass(Colaborador.class);
        verify(colaboradorRepository).save(captor.capture());
        Colaborador guardado = captor.getValue();

        assertThat(respuesta.id()).isEqualTo(10L);
        assertThat(guardado.getUsuarioId()).isEqualTo(7L);
        assertThat(guardado.getUsuarioEmail()).isEqualTo("usuario@albalab.cl");
        assertThat(guardado.getNombre()).isEqualTo("Felipe");
        assertThat(guardado.getFotoUri()).isEqualTo("https://ejemplo.cl/foto.png");
        assertThat(guardado.getMontoDonacion()).isEqualTo(5000);
        assertThat(guardado.getEstado()).isEqualTo("APROBADA");
        assertThat(guardado.getWebpayOrden()).isEqualTo("ORD-1001");
        assertThat(guardado.getFecha()).isNotNull();
    }

    @Test
    void debeOcultarLaFotoSiLaColaboracionEsAnonima() {
        when(colaboradorRepository.save(any(Colaborador.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        colaboradorService.crear(jwtUsuario(), new ColaboradorRequest(
                "Anonimo", "https://ejemplo.cl/foto.png", 5000, true, null));

        ArgumentCaptor<Colaborador> captor = ArgumentCaptor.forClass(Colaborador.class);
        verify(colaboradorRepository).save(captor.capture());

        assertThat(captor.getValue().isEsAnonimo()).isTrue();
        assertThat(captor.getValue().getFotoUri()).isNull();
    }

    @Test
    void debeReutilizarUnaOrdenWebpayDelMismoUsuario() {
        Colaborador existente = colaborador(1L, 7L, "usuario@albalab.cl", false);
        when(colaboradorRepository.findByWebpayOrden("ORD-1001"))
                .thenReturn(Optional.of(existente));

        ColaboradorResponse respuesta = colaboradorService.crear(jwtUsuario(),
                new ColaboradorRequest("Felipe", null, 5000, false, "ORD-1001"));

        assertThat(respuesta.id()).isEqualTo(1L);
        verify(colaboradorRepository, never()).save(any(Colaborador.class));
    }

    @Test
    void debeRechazarUnaOrdenWebpayDeOtroUsuario() {
        Colaborador existente = colaborador(1L, 8L, "otro@albalab.cl", false);
        when(colaboradorRepository.findByWebpayOrden("ORD-1001"))
                .thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> colaboradorService.crear(jwtUsuario(),
                new ColaboradorRequest("Felipe", null, 5000, false, "ORD-1001")))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("La orden de Webpay ya fue registrada");
    }

    @Test
    void debeActualizarColaboracionComoAdmin() {
        Colaborador existente = colaborador(1L, 7L, "usuario@albalab.cl", false);
        when(colaboradorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(colaboradorRepository.save(existente)).thenReturn(existente);

        ColaboradorResponse respuesta = colaboradorService.actualizar(jwtAdmin(), 1L,
                new ColaboradorRequest("  Nuevo Nombre ", null, 8000, false, "ORD-1001"));

        assertThat(respuesta.nombre()).isEqualTo("Nuevo Nombre");
        assertThat(existente.getMontoDonacion()).isEqualTo(8000);
    }

    @Test
    void debeRechazarActualizacionSiNoEsAdmin() {
        assertThatThrownBy(() -> colaboradorService.actualizar(jwtUsuario(), 1L,
                new ColaboradorRequest("Felipe", null, 5000, false, "ORD-1001")))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessage("Se requiere rol ADMIN para esta operacion");
    }

    @Test
    void debeEliminarColaboracionComoAdmin() {
        Colaborador existente = colaborador(1L, 7L, "usuario@albalab.cl", false);
        when(colaboradorRepository.findById(1L)).thenReturn(Optional.of(existente));

        colaboradorService.eliminar(jwtAdmin(), 1L);

        verify(colaboradorRepository).delete(existente);
    }

    @Test
    void debeRechazarEliminacionSiNoEsAdmin() {
        assertThatThrownBy(() -> colaboradorService.eliminar(jwtUsuario(), 1L))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessage("Se requiere rol ADMIN para esta operacion");
    }

    private Colaborador colaborador(Long id, Long usuarioId, String email, boolean anonimo) {
        Colaborador colaborador = new Colaborador();
        colaborador.setId(id);
        colaborador.setUsuarioId(usuarioId);
        colaborador.setUsuarioEmail(email);
        colaborador.setNombre(anonimo ? "Donante" : "Felipe");
        colaborador.setFotoUri("https://ejemplo.cl/foto.png");
        colaborador.setMontoDonacion(5000);
        colaborador.setEsAnonimo(anonimo);
        colaborador.setFecha(LocalDateTime.parse("2026-01-01T10:00:00"));
        colaborador.setEstado("APROBADA");
        colaborador.setWebpayOrden("ORD-1001");
        return colaborador;
    }

    private Jwt jwtUsuario() {
        return Jwt.withTokenValue("token-usuario")
                .header("alg", "none")
                .subject("Usuario@AlbaLab.CL")
                .claim("usuarioId", 7L)
                .build();
    }

    private Jwt jwtOtroUsuario() {
        return Jwt.withTokenValue("token-otro")
                .header("alg", "none")
                .subject("otro@albalab.cl")
                .claim("usuarioId", 8L)
                .build();
    }

    private Jwt jwtAdmin() {
        return Jwt.withTokenValue("token-admin")
                .header("alg", "none")
                .subject("admin@albalab.cl")
                .claim("roles", List.of("ADMIN"))
                .build();
    }
}

package com.albalab.colaborador.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.albalab.colaborador.dto.ColaboradorRequest;
import com.albalab.colaborador.dto.ColaboradorResponse;
import com.albalab.colaborador.exception.RecursoNoEncontradoException;
import com.albalab.colaborador.model.Colaborador;
import com.albalab.colaborador.repository.ColaboradorRepository;

@Service
public class ColaboradorService {

    private final ColaboradorRepository colaboradorRepository;

    public ColaboradorService(ColaboradorRepository colaboradorRepository) {
        this.colaboradorRepository = colaboradorRepository;
    }

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listar() {
        return colaboradorRepository.findAllByOrderByIdAsc()
                .stream()
                .map(ColaboradorResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ColaboradorResponse obtener(Long id) {
        return ColaboradorResponse.from(buscarPorId(id));
    }

    @Transactional
    public ColaboradorResponse crear(ColaboradorRequest request) {
        Colaborador colaborador = new Colaborador();
        aplicarCambios(colaborador, request);
        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    @Transactional
    public ColaboradorResponse actualizar(Long id, ColaboradorRequest request) {
        Colaborador colaborador = buscarPorId(id);
        aplicarCambios(colaborador, request);
        return ColaboradorResponse.from(colaboradorRepository.save(colaborador));
    }

    @Transactional
    public void eliminar(Long id) {
        Colaborador colaborador = buscarPorId(id);
        colaboradorRepository.delete(colaborador);
    }

    private void aplicarCambios(Colaborador colaborador, ColaboradorRequest request) {
        colaborador.setNombre(request.nombre().trim());
        colaborador.setFotoUri(request.fotoUri());
        colaborador.setMontoDonacion(request.montoDonacion());
    }

    private Colaborador buscarPorId(Long id) {
        return colaboradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Colaborador no encontrado con id " + id
                ));
    }
}

package com.albalab.backend.service;

import com.albalab.backend.model.Colaborador;
import com.albalab.backend.repository.ColaboradorRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ColaboradorService {

    private final ColaboradorRepository colaboradorRepository;

    public ColaboradorService(ColaboradorRepository colaboradorRepository) {
        this.colaboradorRepository = colaboradorRepository;
    }

    public List<Colaborador> obtenerTodos() {
        return colaboradorRepository.findAll();
    }

    public Colaborador crearColaborador(Colaborador colaborador) {
        return colaboradorRepository.save(colaborador);
    }
}
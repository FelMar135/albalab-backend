package com.albalab.backend.controller;

import com.albalab.backend.model.Colaborador;
import com.albalab.backend.service.ColaboradorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {

    private final ColaboradorService colaboradorService;

    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @GetMapping
    public ResponseEntity<List<Colaborador>> obtenerTodos() {
        List<Colaborador> lista = colaboradorService.obtenerTodos();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<Colaborador> crearColaborador(@RequestBody Colaborador nuevoColaborador) {
        Colaborador guardado = colaboradorService.crearColaborador(nuevoColaborador);
        return ResponseEntity.ok(guardado);
    }
}
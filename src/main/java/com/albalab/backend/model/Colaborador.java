package com.albalab.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "colaboradores")
public class Colaborador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String fotoUri;
    private Integer montoDonacion;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getFotoUri() { return fotoUri; }
    public void setFotoUri(String fotoUri) { this.fotoUri = fotoUri; }
    public Integer getMontoDonacion() { return montoDonacion; }
    public void setMontoDonacion(Integer montoDonacion) { this.montoDonacion = montoDonacion; }
}
package com.albalab.colaborador.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "colaboradores")
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_email", length = 150)
    private String usuarioEmail;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "foto_uri", length = 500)
    private String fotoUri;

    @Column(name = "monto_donacion")
    private Integer montoDonacion;

    @Column(name = "es_anonimo", nullable = false)
    private boolean esAnonimo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "webpay_orden", length = 100)
    private String webpayOrden;

    @PrePersist
    void prePersist() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (estado == null || estado.isBlank()) {
            estado = "APROBADA";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFotoUri() {
        return fotoUri;
    }

    public void setFotoUri(String fotoUri) {
        this.fotoUri = fotoUri;
    }

    public Integer getMontoDonacion() {
        return montoDonacion;
    }

    public void setMontoDonacion(Integer montoDonacion) {
        this.montoDonacion = montoDonacion;
    }

    public boolean isEsAnonimo() {
        return esAnonimo;
    }

    public void setEsAnonimo(boolean esAnonimo) {
        this.esAnonimo = esAnonimo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getWebpayOrden() {
        return webpayOrden;
    }

    public void setWebpayOrden(String webpayOrden) {
        this.webpayOrden = webpayOrden;
    }
}

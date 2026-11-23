package com.albalab.historial.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "compras_v2")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuarioEmail;
    private Long fecha;
    private Integer total;
    private String productos;

    public Compra() {
    }

    public Compra(String usuarioEmail, Long fecha, Integer total, String productos) {
        this.usuarioEmail = usuarioEmail;
        this.fecha = fecha;
        this.total = total;
        this.productos = productos;
    }

    public Long getId() { return id; }
    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }
    public Long getFecha() { return fecha; }
    public void setFecha(Long fecha) { this.fecha = fecha; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public String getProductos() { return productos; }
    public void setProductos(String productos) { this.productos = productos; }
}

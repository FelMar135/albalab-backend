package com.albalab.historial.dto;

public class CompraRequest {
    private String productos;
    private Integer total;

    public String getProductos() { return productos; }
    public void setProductos(String productos) { this.productos = productos; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
}

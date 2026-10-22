package com.albalab.backend.model;

public class WebpayRequest {
    private String ordenCompra;
    private String idSesion;
    private double monto;
    private String urlRetorno; // La URL a la que Webpay redirige tras el pago

    // Genera los Getters y Setters
    public String getOrdenCompra() { return ordenCompra; }
    public void setOrdenCompra(String ordenCompra) { this.ordenCompra = ordenCompra; }
    public String getIdSesion() { return idSesion; }
    public void setIdSesion(String idSesion) { this.idSesion = idSesion; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public String getUrlRetorno() { return urlRetorno; }
    public void setUrlRetorno(String urlRetorno) { this.urlRetorno = urlRetorno; }
}
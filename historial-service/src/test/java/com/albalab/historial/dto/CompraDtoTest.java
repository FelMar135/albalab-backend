package com.albalab.historial.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CompraDtoTest {

    @Test
    void debeIniciarSinProductosNiTotal() {
        CompraRequest request = new CompraRequest();

        assertThat(request.getProductos()).isNull();
        assertThat(request.getTotal()).isNull();
    }

    @Test
    void debeGuardarProductosYTotal() {
        CompraRequest request = new CompraRequest();
        request.setProductos("Polera Albalab x2");
        request.setTotal(30000);

        assertThat(request.getProductos()).isEqualTo("Polera Albalab x2");
        assertThat(request.getTotal()).isEqualTo(30000);
    }
}

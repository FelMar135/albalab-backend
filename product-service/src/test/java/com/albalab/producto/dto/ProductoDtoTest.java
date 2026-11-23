package com.albalab.producto.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import com.albalab.producto.model.Producto;

class ProductoDtoTest {

    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void debeAceptarUnProductoValido() {
        assertValido(new ProductoRequest(
                "Polera Albalab", 15000, "Polera oficial", "https://ejemplo.cl/polera.png", true));
    }

    @Test
    void debeRechazarNombreVacioYPrecioInvalido() {
        assertInvalido(new ProductoRequest(" ", 0, null, null, true), "nombre", "precio");
    }

    @Test
    void debeRechazarPrecioNulo() {
        assertInvalido(new ProductoRequest("Polera Albalab", null, null, null, true),
                "precio");
    }

    @Test
    void debeMapearUnProductoARespuesta() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Polera Albalab");
        producto.setPrecio(15000);
        producto.setDescripcion("Polera oficial");
        producto.setImagenUrl("https://ejemplo.cl/polera.png");
        producto.setDisponible(true);

        ProductoResponse respuesta = ProductoResponse.from(producto);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.nombre()).isEqualTo("Polera Albalab");
        assertThat(respuesta.precio()).isEqualTo(15000);
        assertThat(respuesta.descripcion()).isEqualTo("Polera oficial");
        assertThat(respuesta.imagenUrl()).isEqualTo("https://ejemplo.cl/polera.png");
        assertThat(respuesta.disponible()).isTrue();
    }

    private static void assertValido(Object dto) {
        assertThat(VALIDATOR.validate(dto)).isEmpty();
    }

    private static void assertInvalido(Object dto, String... campos) {
        assertThat(VALIDATOR.validate(dto))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(campos);
    }
}

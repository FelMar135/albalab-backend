package com.albalab.producto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.albalab.producto.dto.ProductoRequest;
import com.albalab.producto.dto.ProductoResponse;
import com.albalab.producto.exception.RecursoNoEncontradoException;
import com.albalab.producto.model.Producto;
import com.albalab.producto.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void debeListarProductos() {
        when(productoRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(producto(1L, "Polera Albalab", 15000, true)));

        List<ProductoResponse> respuesta = productoService.listar();

        assertThat(respuesta).hasSize(1);
        assertThat(respuesta.get(0).nombre()).isEqualTo("Polera Albalab");
        assertThat(respuesta.get(0).precio()).isEqualTo(15000);
    }

    @Test
    void debeObtenerProductoPorId() {
        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto(1L, "Polera Albalab", 15000, true)));

        ProductoResponse respuesta = productoService.obtener(1L);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.disponible()).isTrue();
    }

    @Test
    void debeFallarAlBuscarProductoInexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtener(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Producto no encontrado con id 99");
    }

    @Test
    void debeCrearProductoConDisponiblePorDefecto() {
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            producto.setId(10L);
            return producto;
        });

        ProductoResponse respuesta = productoService.crear(new ProductoRequest(
                "  Polera Albalab ", 15000, "Polera oficial", "https://ejemplo.cl/polera.png", null));

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        Producto guardado = captor.getValue();

        assertThat(respuesta.id()).isEqualTo(10L);
        assertThat(guardado.getNombre()).isEqualTo("Polera Albalab");
        assertThat(guardado.getDisponible()).isTrue();
        assertThat(guardado.getFechaCreacion()).isNotNull();
    }

    @Test
    void debeActualizarProducto() {
        Producto producto = producto(1L, "Polera", 10000, true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(producto)).thenReturn(producto);

        ProductoResponse respuesta = productoService.actualizar(1L, new ProductoRequest(
                "  Poleron ", 20000, "Nueva descripcion", null, false));

        assertThat(respuesta.nombre()).isEqualTo("Poleron");
        assertThat(producto.getPrecio()).isEqualTo(20000);
        assertThat(producto.getDisponible()).isFalse();
    }

    @Test
    void debeFallarAlActualizarProductoInexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.actualizar(99L, new ProductoRequest(
                "Polera", 15000, null, null, true)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Producto no encontrado con id 99");
    }

    @Test
    void debeEliminarProducto() {
        Producto producto = producto(1L, "Polera", 15000, true);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.eliminar(1L);

        verify(productoRepository).delete(producto);
    }

    private Producto producto(Long id, String nombre, Integer precio, Boolean disponible) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setDescripcion("Descripcion");
        producto.setImagenUrl("https://ejemplo.cl/producto.png");
        producto.setDisponible(disponible);
        return producto;
    }
}

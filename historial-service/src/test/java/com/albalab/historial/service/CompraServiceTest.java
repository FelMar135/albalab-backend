package com.albalab.historial.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.albalab.historial.dto.CompraRequest;
import com.albalab.historial.model.Compra;
import com.albalab.historial.repository.CompraRepository;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @InjectMocks
    private CompraService compraService;

    @Test
    void debeGuardarLaCompraDelUsuarioAutenticado() {
        when(compraRepository.save(any(Compra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CompraRequest request = new CompraRequest();
        request.setProductos("Polera Albalab x2");
        request.setTotal(30000);

        long antes = System.currentTimeMillis();
        Compra guardada = compraService.guardar("usuario@albalab.cl", request);
        long despues = System.currentTimeMillis();

        ArgumentCaptor<Compra> captor = ArgumentCaptor.forClass(Compra.class);
        verify(compraRepository).save(captor.capture());
        Compra enviada = captor.getValue();

        assertThat(guardada.getUsuarioEmail()).isEqualTo("usuario@albalab.cl");
        assertThat(guardada.getProductos()).isEqualTo("Polera Albalab x2");
        assertThat(guardada.getTotal()).isEqualTo(30000);
        assertThat(guardada.getFecha()).isBetween(antes, despues);
        assertThat(enviada).isSameAs(guardada);
    }

    @Test
    void debeListarLasComprasDelUsuario() {
        List<Compra> compras = List.of(
                new Compra("usuario@albalab.cl", 2000L, 30000, "Polera Albalab x2"),
                new Compra("usuario@albalab.cl", 1000L, 15000, "Taza Albalab")
        );
        when(compraRepository.findByUsuarioEmailOrderByFechaDesc("usuario@albalab.cl"))
                .thenReturn(compras);

        List<Compra> respuesta = compraService.listar("usuario@albalab.cl");

        assertThat(respuesta).containsExactlyElementsOf(compras);
        verify(compraRepository)
                .findByUsuarioEmailOrderByFechaDesc("usuario@albalab.cl");
    }
}

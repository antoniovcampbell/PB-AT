package com.pbtp1.avaliacoes.listener;

import com.pbtp1.avaliacoes.model.CompraProduto;
import com.pbtp1.avaliacoes.repository.CompraProdutoRepository;
import com.pbtp1.shared.messaging.EventoCompra;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompraProdutoListenerTest {
    @Mock
    private CompraProdutoRepository repository;

    @InjectMocks
    private CompraProdutoListener listener;

    @Test
    void deveProcessarEventoDeCompraUmaUnicaVez() {
        EventoCompra evento = new EventoCompra(1L, 2L, 3L, "CRIADA", LocalDateTime.now());
        CompraProduto salvo = CompraProduto.builder()
                .compraId(1L)
                .usuarioId(2L)
                .produtoId(3L)
                .ativa(true)
                .build();
        when(repository.findByCompraIdAndProdutoId(1L, 3L)).thenReturn(Optional.empty(), Optional.of(salvo));

        listener.processar(evento);
        listener.processar(evento);

        verify(repository, times(1)).save(any(CompraProduto.class));
    }

    @Test
    void deveDesativarCompraCancelada() {
        CompraProduto compra = CompraProduto.builder()
                .compraId(1L)
                .usuarioId(2L)
                .produtoId(3L)
                .ativa(true)
                .build();
        when(repository.findByCompraIdAndProdutoId(1L, 3L)).thenReturn(Optional.of(compra));

        listener.processar(new EventoCompra(1L, 2L, 3L, "CANCELADA", LocalDateTime.now()));

        verify(repository).save(compra);
        org.assertj.core.api.Assertions.assertThat(compra.getAtiva()).isFalse();
    }
}

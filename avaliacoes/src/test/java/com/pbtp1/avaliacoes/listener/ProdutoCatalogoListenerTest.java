package com.pbtp1.avaliacoes.listener;

import com.pbtp1.avaliacoes.model.ProdutoCatalogo;
import com.pbtp1.avaliacoes.repository.ProdutoCatalogoRepository;
import com.pbtp1.shared.messaging.EventoProduto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProdutoCatalogoListenerTest {
    @Mock
    private ProdutoCatalogoRepository repository;

    @InjectMocks
    private ProdutoCatalogoListener listener;

    @Test
    void deveAtualizarProjecaoDoProduto() {
        listener.processar(new EventoProduto(4L, "Produto", "Descrição", BigDecimal.valueOf(10.0), 2L,
                5, "ATIVO", "ATUALIZADO", LocalDateTime.now()));

        ArgumentCaptor<ProdutoCatalogo> captor = ArgumentCaptor.forClass(ProdutoCatalogo.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(4L);
        assertThat(captor.getValue().getStatus()).isEqualTo("ATIVO");
    }
}

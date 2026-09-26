package com.pbtp1.shared.messaging;

import com.pbtp1.shared.dto.AvaliacaoDTO;
import com.pbtp1.shared.dto.ProdutoDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SharedContractsTest {
    @Test
    void dtoProdutoEAvaliacaoPreservamSeusCampos() {
        ProdutoDTO produto = new ProdutoDTO(7L, "Fone", "Bluetooth", 99.90);
        AvaliacaoDTO avaliacao = new AvaliacaoDTO(3L, 7L, 12L, "Ana", 5, "Ótimo", LocalDateTime.MIN);

        assertThat(produto.id()).isEqualTo(7L);
        assertThat(produto.nome()).isEqualTo("Fone");
        assertThat(produto.descricao()).isEqualTo("Bluetooth");
        assertThat(produto.preco()).isEqualTo(99.90);
        assertThat(avaliacao.id()).isEqualTo(3L);
        assertThat(avaliacao.produtoId()).isEqualTo(7L);
        assertThat(avaliacao.compraId()).isEqualTo(12L);
        assertThat(avaliacao.nomeUsuario()).isEqualTo("Ana");
        assertThat(avaliacao.nota()).isEqualTo(5);
        assertThat(avaliacao.comentario()).isEqualTo("Ótimo");
        assertThat(avaliacao.dataCriacao()).isEqualTo(LocalDateTime.MIN);
    }

    @Test
    void eventoCompraMantemCamposEConstrutoresDeCompatibilidade() {
        LocalDateTime agora = LocalDateTime.now();
        EventoCompra completo = new EventoCompra(1L, 2L, 3L, "CRIADA", agora, true, "Ana");
        EventoCompra legado = new EventoCompra(4L, 5L, 6L, "CRIADA", agora, true);
        EventoCompra simples = new EventoCompra(7L, 8L, 9L, "CRIADA", agora);

        assertThat(completo.compraId()).isEqualTo(1L);
        assertThat(completo.usuarioId()).isEqualTo(2L);
        assertThat(completo.produtoId()).isEqualTo(3L);
        assertThat(completo.tipo()).isEqualTo("CRIADA");
        assertThat(completo.ocorridoEm()).isEqualTo(agora);
        assertThat(completo.demonstracao()).isTrue();
        assertThat(completo.nomeUsuario()).isEqualTo("Ana");
        assertThat(legado.demonstracao()).isTrue();
        assertThat(legado.nomeUsuario()).isNull();
        assertThat(simples.demonstracao()).isFalse();
        assertThat(simples.nomeUsuario()).isNull();
    }

    @Test
    void eventoProdutoExpõeSnapshotPublicadoPeloCatalogo() {
        LocalDateTime agora = LocalDateTime.now();
        EventoProduto evento = new EventoProduto(2L, "Notebook", "Leve", BigDecimal.TEN,
                4L, 8, "ATIVO", "ATUALIZADO", agora);

        assertThat(evento.produtoId()).isEqualTo(2L);
        assertThat(evento.nome()).isEqualTo("Notebook");
        assertThat(evento.descricao()).isEqualTo("Leve");
        assertThat(evento.preco()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(evento.categoriaId()).isEqualTo(4L);
        assertThat(evento.estoque()).isEqualTo(8);
        assertThat(evento.status()).isEqualTo("ATIVO");
        assertThat(evento.tipo()).isEqualTo("ATUALIZADO");
        assertThat(evento.ocorridoEm()).isEqualTo(agora);
        assertThat(TipoEventoProduto.values()).containsExactly(
                TipoEventoProduto.CRIADO, TipoEventoProduto.ATUALIZADO, TipoEventoProduto.EXCLUIDO);
    }
}

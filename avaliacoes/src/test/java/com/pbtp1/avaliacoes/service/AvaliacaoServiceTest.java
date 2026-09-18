package com.pbtp1.avaliacoes.service;

import com.pbtp1.avaliacoes.model.Avaliacao;
import com.pbtp1.avaliacoes.repository.AvaliacaoRepository;
import com.pbtp1.avaliacoes.repository.ProdutoCatalogoRepository;
import com.pbtp1.avaliacoes.model.ProdutoCatalogo;
import com.pbtp1.shared.dto.AvaliacaoDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AvaliacaoServiceTest {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private ProdutoCatalogoRepository produtoCatalogoRepository;

    @BeforeEach
    void setUp() {
        avaliacaoRepository.deleteAll();
        produtoCatalogoRepository.deleteAll();
        produtoCatalogoRepository.save(ProdutoCatalogo.builder()
                .id(1L)
                .nome("Smartphone")
                .descricao("Android")
                .preco(1999.99)
                .build());
    }

    @Test
    void deveSalvarAvaliacaoParaProdutoExistente() {
        AvaliacaoDTO salva = avaliacaoService.salvar(
                Avaliacao.builder()
                        .produtoId(1L)
                        .nomeUsuario("Ana")
                        .nota(5)
                        .comentario("Excelente")
                        .build()
        );

        assertThat(salva.id()).isNotNull();
        assertThat(salva.produtoId()).isEqualTo(1L);
        assertThat(salva.dataCriacao()).isNotNull();
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoExiste() {
        Avaliacao avaliacao = Avaliacao.builder()
                .produtoId(999L)
                .nomeUsuario("Ana")
                .nota(4)
                .build();

        assertThatThrownBy(() -> avaliacaoService.salvar(avaliacao))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Produto não sincronizado");
    }

    @Test
    void deveBuscarPorId() {
        AvaliacaoDTO salva = avaliacaoService.salvar(
                Avaliacao.builder().produtoId(1L).nomeUsuario("Bruno").nota(3).build()
        );

        AvaliacaoDTO encontrada = avaliacaoService.buscarPorId(salva.id());
        assertThat(encontrada.nomeUsuario()).isEqualTo("Bruno");
        assertThat(encontrada.nota()).isEqualTo(3);
    }

    @Test
    void deveListarPorProduto() {
        avaliacaoService.salvar(Avaliacao.builder().produtoId(1L).nomeUsuario("A").nota(5).build());
        avaliacaoService.salvar(Avaliacao.builder().produtoId(1L).nomeUsuario("B").nota(4).build());

        List<AvaliacaoDTO> avaliacoes = avaliacaoService.listarPorProduto(1L);
        assertThat(avaliacoes).hasSize(2);
    }

    @Test
    void deveCalcularMediaPorProduto() {
        avaliacaoService.salvar(Avaliacao.builder().produtoId(1L).nomeUsuario("A").nota(5).build());
        avaliacaoService.salvar(Avaliacao.builder().produtoId(1L).nomeUsuario("B").nota(4).build());

        AvaliacaoService.MediaAvaliacao media = avaliacaoService.calcularMediaPorProduto(1L);

        assertThat(media.total()).isEqualTo(2);
        assertThat(media.media()).isEqualTo(4.5);
    }

    @Test
    void deveAtualizarAvaliacao() {
        AvaliacaoDTO salva = avaliacaoService.salvar(
                Avaliacao.builder().produtoId(1L).nomeUsuario("Ana").nota(2).build()
        );

        AvaliacaoDTO atualizada = avaliacaoService.atualizar(salva.id(),
                Avaliacao.builder().nomeUsuario("Ana").nota(5).comentario("Mudei de ideia").build());

        assertThat(atualizada.nota()).isEqualTo(5);
        assertThat(atualizada.comentario()).isEqualTo("Mudei de ideia");
        assertThat(atualizada.produtoId()).isEqualTo(1L);
    }

    @Test
    void deveDeletarAvaliacao() {
        AvaliacaoDTO salva = avaliacaoService.salvar(
                Avaliacao.builder().produtoId(1L).nomeUsuario("Ana").nota(5).build()
        );

        avaliacaoService.deletar(salva.id());

        assertThatThrownBy(() -> avaliacaoService.buscarPorId(salva.id()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoDeletarIdInexistente() {
        assertThatThrownBy(() -> avaliacaoService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

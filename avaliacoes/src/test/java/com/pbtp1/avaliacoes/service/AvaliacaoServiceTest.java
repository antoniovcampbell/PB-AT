package com.pbtp1.avaliacoes.service;

import com.pbtp1.avaliacoes.model.Avaliacao;
import com.pbtp1.avaliacoes.repository.AvaliacaoRepository;
import com.pbtp1.avaliacoes.repository.ProdutoCatalogoRepository;
import com.pbtp1.avaliacoes.repository.CompraProdutoRepository;
import com.pbtp1.avaliacoes.listener.CompraProdutoListener;
import com.pbtp1.avaliacoes.model.ProdutoCatalogo;
import com.pbtp1.shared.dto.AvaliacaoDTO;
import com.pbtp1.shared.messaging.EventoCompra;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

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

    @Autowired
    private CompraProdutoRepository compraProdutoRepository;

    @Autowired
    private CompraProdutoListener compraProdutoListener;

    @BeforeEach
    void setUp() {
        avaliacaoRepository.deleteAll();
        compraProdutoRepository.deleteAll();
        produtoCatalogoRepository.deleteAll();
        produtoCatalogoRepository.save(ProdutoCatalogo.builder()
                .id(1L)
                .nome("Smartphone")
                .descricao("Android")
                .preco(BigDecimal.valueOf(1999.99))
                .build());
    }

    @Test
    void deveSalvarAvaliacaoParaProdutoExistente() {
        AvaliacaoDTO salva = avaliacaoService.salvar(
                avaliacao(100L, 7L, "Ana", 5, "Excelente"), 7L
        );

        assertThat(salva.id()).isNotNull();
        assertThat(salva.produtoId()).isEqualTo(1L);
        assertThat(salva.dataCriacao()).isNotNull();
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoExiste() {
        Avaliacao avaliacao = Avaliacao.builder()
                .compraId(100L)
                .produtoId(999L)
                .usuarioId(7L)
                .nomeUsuario("Ana")
                .nota(4)
                .build();

        assertThatThrownBy(() -> avaliacaoService.salvar(avaliacao, 7L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Produto não sincronizado");
    }

    @Test
    void deveImpedirMaisDeUmaAvaliacaoDoMesmoProdutoNaMesmaCompra() {
        avaliacaoService.salvar(avaliacao(200L, 7L, "Ana", 5, null), 7L);

        assertThatThrownBy(() -> avaliacaoService.salvar(avaliacao(200L, 7L, "Ana", 4, null), 7L))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class)
                .hasMessageContaining("desta compra já foi avaliado");
    }

    @Test
    void devePermitirAvaliacoesDoMesmoProdutoEmComprasDiferentes() {
        avaliacaoService.salvar(avaliacao(210L, 7L, "Ana", 5, null), 7L);
        AvaliacaoDTO segundaCompra = avaliacaoService.salvar(avaliacao(211L, 7L, "Ana", 4, null), 7L);

        assertThat(segundaCompra.compraId()).isEqualTo(211L);
    }

    @Test
    void deveLiberarAvaliacaoDepoisDeReceberEventoDeCompra() {
        compraProdutoListener.processar(new EventoCompra(10L, 7L, 1L, "CRIADA", LocalDateTime.now()));

        AvaliacaoDTO avaliacao = avaliacaoService.salvar(avaliacao(10L, 7L, "Ana", 5, null), 7L);

        assertThat(avaliacao.produtoId()).isEqualTo(1L);
        assertThat(avaliacao.nomeUsuario()).isEqualTo("Ana");
    }

    @Test
    void deveRevogarElegibilidadeDepoisDeReceberCancelamento() {
        compraProdutoListener.processar(new EventoCompra(11L, 7L, 1L, "CRIADA", LocalDateTime.now()));
        assertThat(compraProdutoRepository.findByCompraIdAndProdutoIdAndUsuarioIdAndAtivaTrue(11L, 1L, 7L)).isPresent();

        compraProdutoListener.processar(new EventoCompra(11L, 7L, 1L, "CANCELADA", LocalDateTime.now()));

        assertThat(compraProdutoRepository.findByCompraIdAndProdutoIdAndUsuarioIdAndAtivaTrue(11L, 1L, 7L)).isEmpty();
    }

    @Test
    void deveBuscarPorId() {
        AvaliacaoDTO salva = avaliacaoService.salvar(avaliacao(300L, 7L, "Bruno", 3, null), 7L);

        AvaliacaoDTO encontrada = avaliacaoService.buscarPorId(salva.id());
        assertThat(encontrada.nomeUsuario()).isEqualTo("Bruno");
        assertThat(encontrada.nota()).isEqualTo(3);
    }

    @Test
    void deveListarPorProduto() {
        avaliacaoService.salvar(avaliacao(401L, 7L, "A", 5, null), 7L);
        avaliacaoService.salvar(avaliacao(402L, 8L, "B", 4, null), 8L);

        List<AvaliacaoDTO> avaliacoes = avaliacaoService.listarPorProduto(1L);
        assertThat(avaliacoes).hasSize(2);
    }

    @Test
    void deveCalcularMediaPorProduto() {
        avaliacaoService.salvar(avaliacao(501L, 7L, "A", 5, null), 7L);
        avaliacaoService.salvar(avaliacao(502L, 8L, "B", 4, null), 8L);

        AvaliacaoService.MediaAvaliacao media = avaliacaoService.calcularMediaPorProduto(1L);

        assertThat(media.total()).isEqualTo(2);
        assertThat(media.media()).isEqualTo(4.5);
    }

    @Test
    void deveAtualizarAvaliacao() {
        AvaliacaoDTO salva = avaliacaoService.salvar(avaliacao(600L, 7L, "Ana", 2, null), 7L);

        AvaliacaoDTO atualizada = avaliacaoService.atualizar(salva.id(),
                Avaliacao.builder().nomeUsuario("Ana").nota(5).comentario("Mudei de ideia").build());

        assertThat(atualizada.nota()).isEqualTo(5);
        assertThat(atualizada.comentario()).isEqualTo("Mudei de ideia");
        assertThat(atualizada.produtoId()).isEqualTo(1L);
    }

    @Test
    void deveDeletarAvaliacao() {
        AvaliacaoDTO salva = avaliacaoService.salvar(avaliacao(700L, 7L, "Ana", 5, null), 7L);

        avaliacaoService.deletar(salva.id());

        assertThatThrownBy(() -> avaliacaoService.buscarPorId(salva.id()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoDeletarIdInexistente() {
        assertThatThrownBy(() -> avaliacaoService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private Avaliacao avaliacao(Long compraId, Long usuarioId, String nome, int nota, String comentario) {
        return Avaliacao.builder()
                .compraId(compraId)
                .produtoId(1L)
                .usuarioId(usuarioId)
                .nomeUsuario(nome)
                .nota(nota)
                .comentario(comentario)
                .build();
    }
}

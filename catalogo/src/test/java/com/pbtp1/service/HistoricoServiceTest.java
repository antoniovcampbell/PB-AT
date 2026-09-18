package com.pbtp1.service;

import com.pbtp1.model.Categoria;
import com.pbtp1.model.Produto;
import com.pbtp1.repository.CategoriaRepository;
import com.pbtp1.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HistoricoServiceTest {

    @Autowired
    private HistoricoService historicoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
    }

    @Test
    void deveRegistrarHistoricoAposCriacao() {
        categoria = categoriaRepository.save(
                Categoria.builder().nome("Audit").descricao("Categoria para auditoria").build()
        );

        Produto produto = produtoRepository.save(
                Produto.builder()
                        .nome("Produto Auditado")
                        .descricao("Versao inicial")
                        .preco(100.0)
                        .categoria(categoria)
                        .build()
        );

        List<?> revisoes = historicoService.listarRevisoesProduto(produto.getId());

        assertThat(revisoes).isNotEmpty();
    }

    @Test
    void deveRegistrarHistoricoAposAtualizacao() {
        categoria = categoriaRepository.save(
                Categoria.builder().nome("Audit").descricao("Categoria para auditoria").build()
        );

        Produto produto = produtoRepository.save(
                Produto.builder()
                        .nome("Produto Auditado")
                        .descricao("Versao inicial")
                        .preco(100.0)
                        .categoria(categoria)
                        .build()
        );

        produto.setNome("Produto Modificado");
        produto.setPreco(200.0);
        produtoRepository.save(produto);

        List<?> revisoes = historicoService.listarRevisoesProduto(produto.getId());

        assertThat(revisoes).hasSize(2);
    }

    @Test
    void deveRegistrarHistoricoAposDelecao() {
        categoria = categoriaRepository.save(
                Categoria.builder().nome("Audit").descricao("Categoria para auditoria").build()
        );

        Produto produto = produtoRepository.save(
                Produto.builder()
                        .nome("Produto Deletado")
                        .descricao("Sera deletado")
                        .preco(50.0)
                        .categoria(categoria)
                        .build()
        );

        produtoRepository.delete(produto);

        List<?> revisoes = historicoService.listarRevisoesProduto(produto.getId());

        assertThat(revisoes).hasSize(2);
    }

    @Test
    void deveRecuperarEstadoAnterior() {
        categoria = categoriaRepository.save(
                Categoria.builder().nome("Audit").descricao("Categoria para auditoria").build()
        );

        Produto produto = produtoRepository.save(
                Produto.builder()
                        .nome("Produto Auditado")
                        .descricao("Versao inicial")
                        .preco(100.0)
                        .categoria(categoria)
                        .build()
        );

        produto.setNome("Nome Alterado");
        produto.setPreco(999.99);
        produtoRepository.save(produto);

        List<?> revisoes = historicoService.listarRevisoesProduto(produto.getId());

        assertThat(revisoes).hasSize(2);

        Object[] segundaRevisao = (Object[]) revisoes.get(0);
        Produto produtoModificado = (Produto) segundaRevisao[0];

        Object[] primeiraRevisao = (Object[]) revisoes.get(1);
        Produto produtoOriginal = (Produto) primeiraRevisao[0];

        assertThat(produtoOriginal.getNome()).isEqualTo("Produto Auditado");
        assertThat(produtoModificado.getNome()).isEqualTo("Nome Alterado");
    }

    @Test
    void deveListarTodasRevisoes() {
        categoria = categoriaRepository.save(
                Categoria.builder().nome("Audit").descricao("Categoria para auditoria").build()
        );

        produtoRepository.save(
                Produto.builder().nome("Produto 1").descricao("Teste 1").preco(50.0).categoria(categoria).build()
        );

        produtoRepository.save(
                Produto.builder().nome("Produto 2").descricao("Teste 2").preco(150.0).categoria(categoria).build()
        );

        List<?> todasRevisoes = historicoService.listarTodasRevisoes();

        assertThat(todasRevisoes).isNotEmpty();
    }
}

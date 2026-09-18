package com.pbtp1.repository;

import com.pbtp1.model.Categoria;
import com.pbtp1.model.Produto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProdutoRepositoryTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EntityManager entityManager;

    private Categoria categoria;
    private Produto produto1;

    @BeforeEach
    void setUp() {
        categoria = categoriaRepository.save(
                Categoria.builder().nome("Eletrônicos").descricao("Produtos eletrônicos").build()
        );

        Categoria outraCategoria = categoriaRepository.save(
                Categoria.builder().nome("Livros").descricao("Livros e revistas").build()
        );

        produto1 = produtoRepository.save(
                Produto.builder()
                        .nome("Smartphone")
                        .descricao("Smartphone Android")
                        .preco(1999.99)
                        .categoria(categoria)
                        .build()
        );

        produtoRepository.save(
                Produto.builder()
                        .nome("Notebook")
                        .descricao("Notebook 16GB RAM")
                        .preco(4999.99)
                        .categoria(categoria)
                        .build()
        );

        produtoRepository.save(
                Produto.builder()
                        .nome("Livro Java")
                        .descricao("Aprenda Java do zero")
                        .preco(89.90)
                        .categoria(outraCategoria)
                        .build()
        );

        entityManager.flush();
    }

    @Test
    void deveSalvarEEncontrarProdutoPorId() {
        Optional<Produto> encontrado = produtoRepository.findById(produto1.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Smartphone");
    }

    @Test
    void deveListarTodosProdutos() {
        List<Produto> produtos = produtoRepository.findAll();
        assertThat(produtos).hasSize(3);
    }

    @Test
    void deveBuscarPorNomeIgnorandoCase() {
        List<Produto> encontrados = produtoRepository.findByNomeContainingIgnoreCase("smart");
        assertThat(encontrados).hasSize(1);
        assertThat(encontrados.get(0).getNome()).isEqualTo("Smartphone");
    }

    @Test
    void deveBuscarPorNomeParcial() {
        List<Produto> encontrados = produtoRepository.findByNomeContainingIgnoreCase("book");
        assertThat(encontrados).hasSize(1);
        assertThat(encontrados.get(0).getNome()).isEqualTo("Notebook");
    }

    @Test
    void deveBuscarPorFaixaDePreco() {
        List<Produto> encontrados = produtoRepository.findByPrecoBetween(1000.0, 5000.0);
        assertThat(encontrados).hasSize(2);
    }

    @Test
    void deveBuscarPorCategoriaId() {
        List<Produto> encontrados = produtoRepository.findByCategoriaId(categoria.getId());
        assertThat(encontrados).hasSize(2);
    }

    @Test
    void deveBuscarPorCategoriaNome() {
        List<Produto> encontrados = produtoRepository.findByCategoriaNomeIgnoreCase("eletrônicos");
        assertThat(encontrados).hasSize(2);
    }

    @Test
    void deveBuscarPorTermoNoNomeOuDescricao() {
        List<Produto> porNome = produtoRepository.buscarPorTermo("smart");
        assertThat(porNome).hasSize(1);

        List<Produto> porDescricao = produtoRepository.buscarPorTermo("RAM");
        assertThat(porDescricao).hasSize(1);
        assertThat(porDescricao.get(0).getNome()).isEqualTo("Notebook");
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoEncontrado() {
        List<Produto> encontrados = produtoRepository.findByNomeContainingIgnoreCase("inexistente");
        assertThat(encontrados).isEmpty();
    }

    @Test
    void deveAtualizarProduto() {
        Produto produto = produtoRepository.findById(produto1.getId()).orElseThrow();
        produto.setPreco(1499.99);
        produtoRepository.save(produto);

        entityManager.flush();
        entityManager.clear();

        Produto atualizado = produtoRepository.findById(produto1.getId()).orElseThrow();
        assertThat(atualizado.getPreco()).isEqualTo(1499.99);
    }

    @Test
    void deveDeletarProduto() {
        produtoRepository.deleteById(produto1.getId());

        entityManager.flush();

        assertThat(produtoRepository.findById(produto1.getId())).isEmpty();
        assertThat(produtoRepository.count()).isEqualTo(2);
    }
}

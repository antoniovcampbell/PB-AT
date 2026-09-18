package com.pbtp1.service;

import com.pbtp1.model.Categoria;
import com.pbtp1.model.Produto;
import com.pbtp1.repository.CategoriaRepository;
import com.pbtp1.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ProdutoServiceTest {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();

        categoria = categoriaRepository.save(
                Categoria.builder().nome("Teste").descricao("Categoria de teste").build()
        );
    }

    @Test
    void deveSalvarProduto() {
        Produto produto = Produto.builder()
                .nome("Produto Teste")
                .descricao("Descricao teste")
                .preco(100.0)
                .categoria(categoria)
                .build();

        Produto salvo = produtoService.salvar(produto);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("Produto Teste");
        assertThat(salvo.getCategoria().getId()).isEqualTo(categoria.getId());
        assertThat(salvo.getDataCriacao()).isNotNull();
        assertThat(salvo.getDataModificacao()).isNotNull();
    }

    @Test
    void deveLancarExcecaoQuandoCategoriaNaoExiste() {
        Categoria categoriaInvalida = Categoria.builder().id(999L).build();
        Produto produto = Produto.builder()
                .nome("Produto Invalido")
                .preco(50.0)
                .categoria(categoriaInvalida)
                .build();

        assertThatThrownBy(() -> produtoService.salvar(produto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Categoria não encontrada");
    }

    @Test
    void deveBuscarProdutoPorId() {
        Produto salvo = produtoService.salvar(
                Produto.builder().nome("Busca").preco(10.0).build()
        );

        Produto encontrado = produtoService.buscarPorId(salvo.getId());
        assertThat(encontrado.getNome()).isEqualTo("Busca");
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        assertThatThrownBy(() -> produtoService.buscarPorId(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveAtualizarProduto() {
        Produto salvo = produtoService.salvar(
                Produto.builder().nome("Original").preco(50.0).categoria(categoria).build()
        );

        Produto atualizacao = Produto.builder()
                .nome("Atualizado")
                .descricao("Nova descricao")
                .preco(99.99)
                .build();

        Produto atualizado = produtoService.atualizar(salvo.getId(), atualizacao);

        assertThat(atualizado.getNome()).isEqualTo("Atualizado");
        assertThat(atualizado.getPreco()).isEqualTo(99.99);
    }

    @Test
    void deveDeletarProduto() {
        Produto salvo = produtoService.salvar(
                Produto.builder().nome("Deletar").preco(1.0).build()
        );

        produtoService.deletar(salvo.getId());

        assertThatThrownBy(() -> produtoService.buscarPorId(salvo.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveLancarExcecaoAoDeletarIdInexistente() {
        assertThatThrownBy(() -> produtoService.deletar(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deveBuscarPorNome() {
        produtoService.salvar(Produto.builder().nome("Mouse USB").preco(30.0).build());
        produtoService.salvar(Produto.builder().nome("Teclado USB").preco(80.0).build());

        List<Produto> encontrados = produtoService.buscarPorNome("usb");
        assertThat(encontrados).hasSize(2);
    }

    @Test
    void deveBuscarPorFaixaPreco() {
        produtoService.salvar(Produto.builder().nome("Barato").preco(10.0).build());
        produtoService.salvar(Produto.builder().nome("Medio").preco(50.0).build());
        produtoService.salvar(Produto.builder().nome("Caro").preco(200.0).build());

        List<Produto> encontrados = produtoService.buscarPorFaixaPreco(20.0, 100.0);
        assertThat(encontrados).hasSize(1);
        assertThat(encontrados.get(0).getNome()).isEqualTo("Medio");
    }

    @Test
    void deveBuscarPorTermo() {
        produtoService.salvar(
                Produto.builder().nome("Monitor").descricao("Monitor 4K Ultra HD").preco(2500.0).build()
        );

        List<Produto> porNome = produtoService.buscarPorTermo("monitor");
        assertThat(porNome).hasSize(1);

        List<Produto> porDescricao = produtoService.buscarPorTermo("ultra");
        assertThat(porDescricao).hasSize(1);
    }
}

package com.pbtp1.service;

import com.pbtp1.model.Produto;
import com.pbtp1.model.Compra;
import com.pbtp1.model.StatusCompra;
import com.pbtp1.repository.CarrinhoItemRepository;
import com.pbtp1.repository.CompraRepository;
import com.pbtp1.repository.EventoOutboxRepository;
import com.pbtp1.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CompraServiceTest {
    @Autowired
    private CompraService compraService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private CarrinhoItemRepository carrinhoItemRepository;

    @Autowired
    private EventoOutboxRepository eventoOutboxRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        carrinhoItemRepository.deleteAll();
        compraRepository.deleteAll();
        eventoOutboxRepository.deleteAll();
        produtoRepository.deleteAll();
    }

    @Test
    void deveSerIdempotenteParaMesmaChave() {
        Produto produto = produtoService.salvar(Produto.builder()
                .nome("Produto de checkout")
                .preco(BigDecimal.valueOf(19.90))
                .estoque(2)
                .build());
        CompraService.CompraRequest request = new CompraService.CompraRequest(
                List.of(new CompraService.ItemRequest(produto.getId(), 1)));

        CompraService.CompraResponse primeira = compraService.criar(10L, request, "checkout-10-1");
        CompraService.CompraResponse repetida = compraService.criar(10L, request, "checkout-10-1");

        assertThat(repetida.id()).isEqualTo(primeira.id());
        assertThat(compraRepository.count()).isEqualTo(1);
        assertThat(produtoRepository.findById(produto.getId()).orElseThrow().getEstoque()).isEqualTo(1);
    }

    @Test
    void deveBaixarEstoqueDeTodosOsItensDoCheckout() {
        Produto primeiro = produtoService.salvar(Produto.builder()
                .nome("Primeiro produto")
                .preco(BigDecimal.valueOf(10))
                .estoque(3)
                .build());
        Produto segundo = produtoService.salvar(Produto.builder()
                .nome("Segundo produto")
                .preco(BigDecimal.valueOf(15))
                .estoque(4)
                .build());

        CompraService.CompraResponse compra = compraService.criar(11L, new CompraService.CompraRequest(List.of(
                new CompraService.ItemRequest(primeiro.getId(), 2),
                new CompraService.ItemRequest(segundo.getId(), 1))), "checkout-11-1");

        assertThat(compra.total()).isEqualByComparingTo("35.00");
        assertThat(produtoRepository.findById(primeiro.getId()).orElseThrow().getEstoque()).isEqualTo(1);
        assertThat(produtoRepository.findById(segundo.getId()).orElseThrow().getEstoque()).isEqualTo(3);
    }

    @Test
    void deveImpedirRegressaoDeStatus() {
        Compra compra = compraRepository.save(Compra.builder()
                .usuarioId(12L)
                .status(StatusCompra.CRIADA)
                .total(BigDecimal.ZERO)
                .criadaEm(LocalDateTime.now())
                .idempotencyKey("status-12-1")
                .build());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> compraService.atualizarStatus(compra.getId(), StatusCompra.ENVIADA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Transição de compra inválida");
    }
}

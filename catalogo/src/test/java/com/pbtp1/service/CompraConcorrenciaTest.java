package com.pbtp1.service;

import com.pbtp1.model.Produto;
import com.pbtp1.repository.CompraRepository;
import com.pbtp1.repository.EventoOutboxRepository;
import com.pbtp1.repository.ProdutoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CompraConcorrenciaTest {
    @Autowired
    private CompraService compraService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private EventoOutboxRepository eventoOutboxRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        eventoOutboxRepository.deleteAll();
        compraRepository.deleteAll();
        produtoRepository.deleteAll();
        executor = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void devePermitirApenasUmaBaixaQuandoDuasComprasDisputamOUltimoItem() throws Exception {
        Produto produto = produtoService.salvar(Produto.builder()
                .nome("Produto concorrente")
                .preco(new BigDecimal("10.00"))
                .estoque(1)
                .build());
        CompraService.CompraRequest request = new CompraService.CompraRequest(
                List.of(new CompraService.ItemRequest(produto.getId(), 1)));
        CountDownLatch inicio = new CountDownLatch(1);

        Future<Boolean> primeira = executarCheckout(20L, request, "concorrencia-1", inicio);
        Future<Boolean> segunda = executarCheckout(21L, request, "concorrencia-2", inicio);
        inicio.countDown();

        boolean primeiraConcluiu = primeira.get(10, TimeUnit.SECONDS);
        boolean segundaConcluiu = segunda.get(10, TimeUnit.SECONDS);

        assertThat(primeiraConcluiu).isNotEqualTo(segundaConcluiu);
        assertThat(compraRepository.count()).isEqualTo(1);
        assertThat(produtoRepository.findById(produto.getId()).orElseThrow().getEstoque()).isZero();
    }

    private Future<Boolean> executarCheckout(Long usuarioId, CompraService.CompraRequest request,
                                              String chave, CountDownLatch inicio) {
        return executor.submit(() -> {
            inicio.await();
            try {
                compraService.criar(usuarioId, request, chave);
                return true;
            } catch (RuntimeException exception) {
                return false;
            }
        });
    }
}

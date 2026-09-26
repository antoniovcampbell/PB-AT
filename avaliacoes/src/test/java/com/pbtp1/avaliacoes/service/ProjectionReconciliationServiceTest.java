package com.pbtp1.avaliacoes.service;

import com.pbtp1.avaliacoes.model.Avaliacao;
import com.pbtp1.avaliacoes.model.CompraProduto;
import com.pbtp1.avaliacoes.model.ProdutoCatalogo;
import com.pbtp1.avaliacoes.repository.AvaliacaoRepository;
import com.pbtp1.avaliacoes.repository.CompraProdutoRepository;
import com.pbtp1.avaliacoes.repository.ProdutoCatalogoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class ProjectionReconciliationServiceTest {
    private static final String SECRET = "projection-secret";

    @Mock private ProdutoCatalogoRepository produtoRepository;
    @Mock private CompraProdutoRepository compraRepository;
    @Mock private AvaliacaoRepository avaliacaoRepository;

    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;
    private ProjectionReconciliationService service;

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        service = new ProjectionReconciliationService(produtoRepository, compraRepository, avaliacaoRepository,
                restClientBuilder, "http://catalogo", SECRET);
    }

    @AfterEach
    void verificarRequests() {
        server.verify();
    }

    @Test
    void reconciliaProdutosComprasNomesECancelamentos() {
        server.expect(requestTo("http://catalogo/internal/projection/products"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Internal-Secret", SECRET))
                .andRespond(withSuccess("""
                        [
                          {"produtoId":1,"nome":"Fone","descricao":"Bluetooth","preco":129.90,"categoriaId":2,"estoque":8,"status":"ATIVO","tipo":"SINCRONIZADO","ocorridoEm":null},
                          {"produtoId":null,"nome":"Ignorado","descricao":null,"preco":null,"categoriaId":null,"estoque":null,"status":null,"tipo":null,"ocorridoEm":null}
                        ]
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://catalogo/internal/projection/purchases"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Internal-Secret", SECRET))
                .andRespond(withSuccess("""
                        [
                          {"compraId":50,"usuarioId":7,"produtoId":1,"tipo":"CRIADA","ocorridoEm":null,"demonstracao":true,"nomeUsuario":"Ana Silva"},
                          {"compraId":51,"usuarioId":8,"produtoId":2,"tipo":"CRIADA","ocorridoEm":null,"demonstracao":false,"nomeUsuario":"Bruno Lima"},
                          {"compraId":52,"usuarioId":9,"produtoId":3,"tipo":"CANCELADA","ocorridoEm":null,"demonstracao":false,"nomeUsuario":"Camila Souza"},
                          {"compraId":null,"usuarioId":1,"produtoId":4,"tipo":"CRIADA","ocorridoEm":null,"demonstracao":false,"nomeUsuario":"Ignorar"}
                        ]
                        """, MediaType.APPLICATION_JSON));

        ProdutoCatalogo obsoleto = ProdutoCatalogo.builder().id(99L).nome("Removido").build();
        when(produtoRepository.findAll()).thenReturn(List.of(obsoleto));
        CompraProduto existente = CompraProduto.builder().compraId(50L).usuarioId(7L).produtoId(1L)
                .nomeUsuario("Nome antigo").ativa(true).build();
        CompraProduto cancelada = CompraProduto.builder().compraId(52L).usuarioId(9L).produtoId(3L)
                .nomeUsuario("Nome antigo").ativa(true).build();
        when(compraRepository.findByCompraIdAndProdutoId(50L, 1L)).thenReturn(Optional.of(existente));
        when(compraRepository.findByCompraIdAndProdutoId(51L, 2L)).thenReturn(Optional.empty());
        when(compraRepository.findByCompraIdAndProdutoId(52L, 3L)).thenReturn(Optional.of(cancelada));
        Avaliacao antiga = Avaliacao.builder().id(4L).compraId(50L).usuarioId(7L).produtoId(1L)
                .nomeUsuario("Nome antigo").nota(4).build();
        when(avaliacaoRepository.findAllByUsuarioIdAndProdutoId(7L, 1L)).thenReturn(List.of(antiga));
        when(avaliacaoRepository.findAllByUsuarioIdAndProdutoId(8L, 2L)).thenReturn(List.of());
        when(avaliacaoRepository.findAllByUsuarioIdAndProdutoId(9L, 3L)).thenReturn(List.of());
        when(avaliacaoRepository.findByCompraIdAndProdutoId(50L, 1L)).thenReturn(Optional.of(antiga));

        service.reconciliar();

        ArgumentCaptor<ProdutoCatalogo> produtoSalvo = ArgumentCaptor.forClass(ProdutoCatalogo.class);
        verify(produtoRepository).save(produtoSalvo.capture());
        assertThat(produtoSalvo.getValue().getNome()).isEqualTo("Fone");
        verify(produtoRepository).delete(obsoleto);
        assertThat(existente.getNomeUsuario()).isEqualTo("Ana Silva");
        assertThat(cancelada.getAtiva()).isFalse();
        assertThat(antiga.getNomeUsuario()).isEqualTo("Ana Silva");
        verify(compraRepository).save(argThat(compra -> compra.getCompraId().equals(51L)
                && compra.getNomeUsuario().equals("Bruno Lima") && Boolean.FALSE.equals(compra.getDemonstracao())));
        verify(compraRepository).save(cancelada);
        verify(avaliacaoRepository, atLeastOnce()).save(antiga);
    }

    @Test
    void ignoraFalhasDoCatalogoEDeixaAsProjecoesIntactas() {
        server.expect(requestTo("http://catalogo/internal/projection/products"))
                .andRespond(withServerError());

        service.reconciliar();

        verify(produtoRepository, never()).save(any());
        verify(produtoRepository, never()).findAll();
        verify(compraRepository, never()).save(any());
    }
}

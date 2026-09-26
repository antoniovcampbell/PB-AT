package com.pbtp1.avaliacoes.service;

import com.pbtp1.avaliacoes.model.CompraProduto;
import com.pbtp1.avaliacoes.model.ProdutoCatalogo;
import com.pbtp1.avaliacoes.repository.CompraProdutoRepository;
import com.pbtp1.avaliacoes.repository.AvaliacaoRepository;
import com.pbtp1.avaliacoes.repository.ProdutoCatalogoRepository;
import com.pbtp1.shared.messaging.EventoCompra;
import com.pbtp1.shared.messaging.EventoProduto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.projection.reconciliation.enabled", havingValue = "true", matchIfMissing = true)
public class ProjectionReconciliationService {
    private final ProdutoCatalogoRepository produtoRepository;
    private final CompraProdutoRepository compraRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final RestClient catalogoClient;
    private final String internalSecret;

    public ProjectionReconciliationService(ProdutoCatalogoRepository produtoRepository,
                                             CompraProdutoRepository compraRepository,
                                             AvaliacaoRepository avaliacaoRepository,
                                             RestClient.Builder restClientBuilder,
                                             @Value("${app.catalogo.url:http://localhost:8090}") String catalogoUrl,
                                            @Value("${app.internal.secret:pb-at-internal-secret}") String internalSecret) {
        this.produtoRepository = produtoRepository;
        this.compraRepository = compraRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.catalogoClient = restClientBuilder.baseUrl(catalogoUrl).build();
        this.internalSecret = internalSecret;
    }

    @Scheduled(initialDelayString = "${app.projection.reconciliation.initial-delay-ms:30000}",
            fixedDelayString = "${app.projection.reconciliation.fixed-delay-ms:300000}")
    @Transactional
    public void reconciliar() {
        try {
            List<EventoProduto> produtos = buscar("/internal/projection/products", new ParameterizedTypeReference<>() {
            });
            List<EventoCompra> compras = buscar("/internal/projection/purchases", new ParameterizedTypeReference<>() {
            });
            sincronizarProdutos(produtos);
            sincronizarCompras(compras);
        } catch (RestClientException exception) {
            log.warn("Não foi possível reconciliar as projeções com o catálogo: {}", exception.getMessage());
        }
    }

    private <T> List<T> buscar(String caminho, ParameterizedTypeReference<List<T>> tipo) {
        return catalogoClient.get()
                .uri(caminho)
                .header("X-Internal-Secret", internalSecret)
                .retrieve()
                .body(tipo);
    }

    private void sincronizarProdutos(List<EventoProduto> eventos) {
        Set<Long> ids = new HashSet<>();
        for (EventoProduto evento : eventos) {
            if (evento.produtoId() == null) continue;
            ids.add(evento.produtoId());
            produtoRepository.save(ProdutoCatalogo.builder()
                    .id(evento.produtoId())
                    .nome(evento.nome())
                    .descricao(evento.descricao())
                    .preco(evento.preco())
                    .categoriaId(evento.categoriaId())
                    .estoque(evento.estoque())
                    .status(evento.status())
                    .atualizadoEm(evento.ocorridoEm())
                    .build());
        }
        produtoRepository.findAll().stream()
                .filter(produto -> !ids.contains(produto.getId()))
                .forEach(produtoRepository::delete);
    }

    private void sincronizarCompras(List<EventoCompra> eventos) {
        for (EventoCompra evento : eventos) {
            if (evento.compraId() == null || evento.usuarioId() == null || evento.produtoId() == null) {
                continue;
            }
            Optional<CompraProduto> existente = compraRepository.findByCompraIdAndProdutoId(evento.compraId(), evento.produtoId());
            String nomeUsuario = evento.nomeUsuario();
            if (nomeUsuario != null && !nomeUsuario.isBlank()) {
                avaliacaoRepository.findAllByUsuarioIdAndProdutoId(evento.usuarioId(), evento.produtoId())
                        .stream()
                        .filter(avaliacao -> !nomeUsuario.equals(avaliacao.getNomeUsuario()))
                        .forEach(avaliacao -> {
                            avaliacao.setNomeUsuario(nomeUsuario);
                            avaliacaoRepository.save(avaliacao);
                        });
            }
            if ("CANCELADA".equals(evento.tipo())) {
                existente.ifPresent(compra -> {
                    compra.setAtiva(false);
                    compraRepository.save(compra);
                });
                continue;
            }
            if (existente.isPresent()) {
                existente.ifPresent(compra -> {
                    if (nomeUsuario != null && !nomeUsuario.isBlank()) compra.setNomeUsuario(nomeUsuario);
                });
                existente.ifPresent(compraRepository::save);
                if (nomeUsuario != null && !nomeUsuario.isBlank()) {
                    avaliacaoRepository.findByCompraIdAndProdutoId(evento.compraId(), evento.produtoId())
                            .ifPresent(avaliacao -> {
                                avaliacao.setNomeUsuario(nomeUsuario);
                                avaliacaoRepository.save(avaliacao);
                            });
                }
                continue;
            }
            compraRepository.save(CompraProduto.builder()
                    .compraId(evento.compraId())
                    .usuarioId(evento.usuarioId())
                    .nomeUsuario(nomeUsuario)
                    .produtoId(evento.produtoId())
                    .demonstracao(evento.demonstracao())
                    .ativa(true)
                    .build());
        }
    }
}

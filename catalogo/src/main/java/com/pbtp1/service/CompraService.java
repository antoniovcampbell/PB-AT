package com.pbtp1.service;

import com.pbtp1.model.*;
import com.pbtp1.repository.CompraRepository;
import com.pbtp1.repository.ProdutoRepository;
import com.pbtp1.repository.UsuarioRepository;
import com.pbtp1.shared.messaging.EventoCompra;
import com.pbtp1.shared.messaging.RabbitMQConstantes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompraService {
    private static final Map<StatusCompra, Set<StatusCompra>> TRANSICOES = Map.of(
            StatusCompra.CRIADA, Set.of(StatusCompra.PAGA, StatusCompra.ENVIADA, StatusCompra.ENTREGUE, StatusCompra.CANCELADA),
            StatusCompra.PAGA, Set.of(StatusCompra.ENVIADA, StatusCompra.ENTREGUE, StatusCompra.CANCELADA),
            StatusCompra.ENVIADA, Set.of(StatusCompra.ENTREGUE, StatusCompra.CANCELADA),
            StatusCompra.ENTREGUE, Set.of(StatusCompra.ENVIADA),
            StatusCompra.CANCELADA, Set.of(StatusCompra.CRIADA));

    private final CompraRepository compraRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OutboxService outboxService;
    private final ProdutoService produtoService;

    @Transactional
    public CompraResponse criar(Long usuarioId, CompraRequest request) {
        return criar(usuarioId, request, UUID.randomUUID().toString());
    }

    @Transactional
    public CompraResponse criar(Long usuarioId, CompraRequest request, String idempotencyKey) {
        return criarInterno(usuarioId, request, false, idempotencyKey);
    }

    @Transactional
    public CompraResponse criarDemonstracao(Long usuarioId, CompraRequest request) {
        return criarInterno(usuarioId, request, true, "demo-%d".formatted(usuarioId));
    }

    private CompraResponse criarInterno(Long usuarioId, CompraRequest request, boolean demonstracao, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException("Idempotency-Key inválida");
        }
        Compra existente = compraRepository.findByUsuarioIdAndIdempotencyKey(usuarioId, idempotencyKey).orElse(null);
        if (existente != null) {
            return paraResponse(existente);
        }
        if (request == null || request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("A compra precisa ter pelo menos um produto");
        }
        Compra compra = Compra.builder()
                .usuarioId(usuarioId)
                .status(StatusCompra.CRIADA)
                .total(BigDecimal.ZERO)
                .criadaEm(LocalDateTime.now())
                .demonstracao(demonstracao)
                .estoqueControlado(true)
                .idempotencyKey(idempotencyKey)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (ItemRequest item : request.itens()) {
            if (item.quantidade() == null || item.quantidade() < 1) {
                throw new IllegalArgumentException("A quantidade deve ser positiva");
            }
            Produto produto = produtoRepository.findById(item.produtoId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + item.produtoId()));
            int estoque = produto.getEstoque() == null ? 0 : produto.getEstoque();
            if (produto.getStatus() == StatusProduto.INATIVO || produto.getStatus() == StatusProduto.ESGOTADO || estoque < item.quantidade()) {
                throw new IllegalArgumentException("Produto sem estoque disponível: " + produto.getNome());
            }

            produto.setEstoque(estoque - item.quantidade());
            if (produto.getEstoque() == 0) {
                produto.setStatus(StatusProduto.ESGOTADO);
            } else if (produto.getEstoque() <= 3) {
                produto.setStatus(StatusProduto.ESTOQUE_BAIXO);
            }
            produtoRepository.save(produto);
            outboxService.registrar(
                    RabbitMQConstantes.EXCHANGE_PRODUTOS,
                    RabbitMQConstantes.ROUTING_KEY_PRODUTO_ATUALIZADO,
                    produtoService.eventoAtual(produto, "ATUALIZADO"));

            compra.getItens().add(ItemCompra.builder()
                    .compra(compra)
                    .produtoId(produto.getId())
                    .nomeProduto(produto.getNome())
                    .precoUnitario(produto.getPreco())
                    .quantidade(item.quantidade())
                    .build());
            total = total.add(produto.getPreco().multiply(BigDecimal.valueOf(item.quantidade())));
        }

        compra.setTotal(total);
        Compra salva = compraRepository.save(compra);
        registrarEventosCompra(salva, StatusCompra.CRIADA.name(), RabbitMQConstantes.ROUTING_KEY_COMPRA_CRIADA);
        return paraResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<CompraResponse> minhas(Long usuarioId) {
        return compraRepository.findByUsuarioIdOrderByCriadaEmDesc(usuarioId).stream().map(this::paraResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CompraResponse> todas() {
        return compraRepository.findAll().stream().map(this::paraResponse).toList();
    }

    @Transactional
    public CompraResponse atualizarStatus(Long id, StatusCompra status) {
        Compra compra = compraRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Compra não encontrada"));
        if (status == null) {
            throw new IllegalArgumentException("Status obrigatório");
        }
        if (compra.getStatus() == status) {
            return paraResponse(compra);
        }
        validarTransicao(compra.getStatus(), status);
        if (status == StatusCompra.CANCELADA) return cancelar(compra);
        if (compra.getStatus() == StatusCompra.CANCELADA && compra.isEstoqueControlado()) {
            reservarEstoque(compra);
        }
        boolean reabrindo = compra.getStatus() == StatusCompra.CANCELADA;
        compra.setStatus(status);
        Compra salva = compraRepository.save(compra);
        if (reabrindo) {
            registrarEventosCompra(salva, "REABERTA", RabbitMQConstantes.ROUTING_KEY_COMPRA_ATUALIZADA);
        }
        return paraResponse(salva);
    }

    @Transactional
    public CompraResponse cancelar(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Compra não encontrada"));
        if (compra.getStatus() == StatusCompra.CANCELADA) {
            return paraResponse(compra);
        }
        validarTransicao(compra.getStatus(), StatusCompra.CANCELADA);
        return cancelar(compra);
    }

    private CompraResponse cancelar(Compra compra) {
        if (compra.isEstoqueControlado()) {
            for (ItemCompra item : compra.getItens()) {
                Produto produto = produtoRepository.findById(item.getProdutoId()).orElse(null);
                if (produto == null) {
                    continue;
                }
                int estoque = produto.getEstoque() == null ? 0 : produto.getEstoque();
                atualizarEstoque(produto, estoque + item.getQuantidade());
            }
        }
        compra.setStatus(StatusCompra.CANCELADA);
        Compra salva = compraRepository.save(compra);
        registrarEventosCompra(salva, StatusCompra.CANCELADA.name(), RabbitMQConstantes.ROUTING_KEY_COMPRA_ATUALIZADA);
        return paraResponse(salva);
    }

    private void reservarEstoque(Compra compra) {
        for (ItemCompra item : compra.getItens()) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado para reabrir a compra: " + item.getProdutoId()));
            int estoque = produto.getEstoque() == null ? 0 : produto.getEstoque();
            if (produto.getStatus() == StatusProduto.INATIVO || produto.getStatus() == StatusProduto.ESGOTADO
                    || estoque < item.getQuantidade()) {
                throw new IllegalArgumentException("Estoque insuficiente para reabrir a compra: " + produto.getNome());
            }
            atualizarEstoque(produto, estoque - item.getQuantidade());
        }
    }

    private void atualizarEstoque(Produto produto, int estoque) {
        produto.setEstoque(estoque);
        if (produto.getStatus() != StatusProduto.INATIVO) {
            produto.setStatus(estoque == 0 ? StatusProduto.ESGOTADO
                    : estoque <= 3 ? StatusProduto.ESTOQUE_BAIXO : StatusProduto.ATIVO);
        }
        produtoRepository.save(produto);
        outboxService.registrar(
                RabbitMQConstantes.EXCHANGE_PRODUTOS,
                RabbitMQConstantes.ROUTING_KEY_PRODUTO_ATUALIZADO,
                produtoService.eventoAtual(produto, "ATUALIZADO"));
    }

    private void registrarEventosCompra(Compra compra, String tipo, String routingKey) {
        String nomeUsuario = usuarioRepository.findById(compra.getUsuarioId()).map(Usuario::getNome).orElse(null);
        boolean demonstracao = Boolean.TRUE.equals(compra.getDemonstracao());
        LocalDateTime ocorridoEm = LocalDateTime.now();
        compra.getItens().forEach(item -> outboxService.registrar(
                RabbitMQConstantes.EXCHANGE_COMPRAS,
                routingKey,
                new EventoCompra(compra.getId(), compra.getUsuarioId(), item.getProdutoId(), tipo, ocorridoEm,
                        demonstracao, nomeUsuario)));
    }

    private void validarTransicao(StatusCompra atual, StatusCompra proximo) {
        if (!TRANSICOES.getOrDefault(atual, Set.of()).contains(proximo)) {
            throw new IllegalArgumentException("Transição de compra inválida: " + atual + " -> " + proximo);
        }
    }

    private CompraResponse paraResponse(Compra compra) {
        return new CompraResponse(compra.getId(), compra.getUsuarioId(), compra.getStatus().name(), compra.getTotal(), compra.getCriadaEm(),
                compra.getItens().stream().map(item -> new ItemResponse(item.getProdutoId(), item.getNomeProduto(), item.getPrecoUnitario(), item.getQuantidade())).toList());
    }

    public record CompraRequest(List<ItemRequest> itens) {
    }

    public record ItemRequest(Long produtoId, Integer quantidade) {
    }

    public record CompraResponse(Long id, Long usuarioId, String status, BigDecimal total, LocalDateTime criadaEm, List<ItemResponse> itens) {
    }

    public record ItemResponse(Long produtoId, String nomeProduto, BigDecimal precoUnitario, Integer quantidade) {
    }
}

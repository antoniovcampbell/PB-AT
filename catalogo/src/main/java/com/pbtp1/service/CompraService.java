package com.pbtp1.service;

import com.pbtp1.model.*;
import com.pbtp1.repository.CompraRepository;
import com.pbtp1.repository.ProdutoRepository;
import com.pbtp1.shared.messaging.EventoCompra;
import com.pbtp1.shared.messaging.EventoProduto;
import com.pbtp1.shared.messaging.RabbitMQConstantes;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraService {
    private final CompraRepository compraRepository;
    private final ProdutoRepository produtoRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public CompraResponse criar(Long usuarioId, CompraRequest request) {
        if (request == null || request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("A compra precisa ter pelo menos um produto");
        }

        Compra compra = Compra.builder()
                .usuarioId(usuarioId)
                .status(StatusCompra.CRIADA)
                .total(0.0)
                .criadaEm(LocalDateTime.now())
                .build();

        double total = 0;
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
            rabbitTemplate.convertAndSend(
                    RabbitMQConstantes.EXCHANGE_PRODUTOS,
                    RabbitMQConstantes.ROUTING_KEY_PRODUTO_ATUALIZADO,
                    new EventoProduto(produto.getId(), produto.getNome(), produto.getDescricao(), produto.getPreco(),
                            produto.getCategoria() != null ? produto.getCategoria().getId() : null,
                            produto.getEstoque(), produto.getStatus().name(), "ATUALIZADO", LocalDateTime.now()));

            compra.getItens().add(ItemCompra.builder()
                    .compra(compra)
                    .produtoId(produto.getId())
                    .nomeProduto(produto.getNome())
                    .precoUnitario(produto.getPreco())
                    .quantidade(item.quantidade())
                    .build());
            total += produto.getPreco() * item.quantidade();
        }

        compra.setTotal(total);
        Compra salva = compraRepository.save(compra);
        salva.getItens().forEach(item -> rabbitTemplate.convertAndSend(
                RabbitMQConstantes.EXCHANGE_COMPRAS,
                RabbitMQConstantes.ROUTING_KEY_COMPRA_CRIADA,
                new EventoCompra(salva.getId(), usuarioId, item.getProdutoId(), "CRIADA", LocalDateTime.now())));
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
        compra.setStatus(status);
        return paraResponse(compraRepository.save(compra));
    }

    private CompraResponse paraResponse(Compra compra) {
        return new CompraResponse(compra.getId(), compra.getUsuarioId(), compra.getStatus().name(), compra.getTotal(), compra.getCriadaEm(),
                compra.getItens().stream().map(item -> new ItemResponse(item.getProdutoId(), item.getNomeProduto(), item.getPrecoUnitario(), item.getQuantidade())).toList());
    }

    public record CompraRequest(List<ItemRequest> itens) {
    }

    public record ItemRequest(Long produtoId, Integer quantidade) {
    }

    public record CompraResponse(Long id, Long usuarioId, String status, Double total, LocalDateTime criadaEm, List<ItemResponse> itens) {
    }

    public record ItemResponse(Long produtoId, String nomeProduto, Double precoUnitario, Integer quantidade) {
    }
}

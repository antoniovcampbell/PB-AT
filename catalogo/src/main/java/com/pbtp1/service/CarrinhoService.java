package com.pbtp1.service;

import com.pbtp1.model.CarrinhoItem;
import com.pbtp1.model.Produto;
import com.pbtp1.model.StatusProduto;
import com.pbtp1.repository.CarrinhoItemRepository;
import com.pbtp1.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CarrinhoService {
    private final CarrinhoItemRepository carrinhoItemRepository;
    private final ProdutoRepository produtoRepository;
    private final CompraService compraService;

    @Transactional(readOnly = true)
    public CarrinhoResponse listar(Long usuarioId) {
        List<ItemCarrinhoResponse> itens = carrinhoItemRepository.findByUsuarioIdOrderByIdAsc(usuarioId).stream()
                .map(this::paraItem)
                .toList();
        return resposta(itens);
    }

    @Transactional
    public CarrinhoResponse adicionar(Long usuarioId, ItemCarrinhoRequest request) {
        validarQuantidade(request);
        Produto produto = produto(request.produtoId());
        CarrinhoItem item = carrinhoItemRepository.findByUsuarioIdAndProdutoId(usuarioId, produto.getId())
                .orElseGet(() -> CarrinhoItem.builder().usuarioId(usuarioId).produtoId(produto.getId()).quantidade(0).build());
        int quantidade = item.getQuantidade() + request.quantidade();
        validarDisponibilidade(produto, quantidade);
        item.setQuantidade(quantidade);
        carrinhoItemRepository.save(item);
        return listar(usuarioId);
    }

    @Transactional
    public CarrinhoResponse atualizar(Long usuarioId, Long produtoId, ItemCarrinhoRequest request) {
        validarQuantidade(request);
        Produto produto = produto(produtoId);
        CarrinhoItem item = carrinhoItemRepository.findByUsuarioIdAndProdutoId(usuarioId, produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não está no carrinho"));
        validarDisponibilidade(produto, request.quantidade());
        item.setQuantidade(request.quantidade());
        carrinhoItemRepository.save(item);
        return listar(usuarioId);
    }

    @Transactional
    public CarrinhoResponse remover(Long usuarioId, Long produtoId) {
        carrinhoItemRepository.findByUsuarioIdAndProdutoId(usuarioId, produtoId).ifPresent(carrinhoItemRepository::delete);
        return listar(usuarioId);
    }

    @Transactional
    public CompraService.CompraResponse finalizar(Long usuarioId, String idempotencyKey) {
        List<CarrinhoItem> itens = carrinhoItemRepository.findByUsuarioIdOrderByIdAsc(usuarioId);
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("O carrinho está vazio");
        }
        CompraService.CompraResponse compra = compraService.criar(usuarioId, new CompraService.CompraRequest(
                itens.stream().map(item -> new CompraService.ItemRequest(item.getProdutoId(), item.getQuantidade())).toList()), idempotencyKey);
        carrinhoItemRepository.deleteByUsuarioId(usuarioId);
        return compra;
    }

    private ItemCarrinhoResponse paraItem(CarrinhoItem item) {
        Produto produto = produto(item.getProdutoId());
        return new ItemCarrinhoResponse(produto.getId(), produto.getNome(), produto.getPreco(), item.getQuantidade(), produto.getEstoque());
    }

    private CarrinhoResponse resposta(List<ItemCarrinhoResponse> itens) {
        BigDecimal total = itens.stream()
                .map(item -> item.preco().multiply(BigDecimal.valueOf(item.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarrinhoResponse(itens, total);
    }

    private Produto produto(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
    }

    private void validarQuantidade(ItemCarrinhoRequest request) {
        if (request == null || request.produtoId() == null || request.quantidade() == null || request.quantidade() < 1) {
            throw new IllegalArgumentException("Produto e quantidade positiva são obrigatórios");
        }
    }

    private void validarDisponibilidade(Produto produto, int quantidade) {
        int estoque = produto.getEstoque() == null ? 0 : produto.getEstoque();
        if (produto.getStatus() == StatusProduto.INATIVO || produto.getStatus() == StatusProduto.ESGOTADO || estoque < quantidade) {
            throw new IllegalArgumentException("Produto sem estoque disponível: " + produto.getNome());
        }
    }

    public record ItemCarrinhoRequest(Long produtoId, Integer quantidade) {
    }

    public record ItemCarrinhoResponse(Long produtoId, String nomeProduto, BigDecimal preco, Integer quantidade, Integer estoque) {
    }

    public record CarrinhoResponse(List<ItemCarrinhoResponse> itens, BigDecimal total) {
    }
}

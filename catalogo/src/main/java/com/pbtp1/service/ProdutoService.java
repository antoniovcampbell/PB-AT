package com.pbtp1.service;

import com.pbtp1.model.Categoria;
import com.pbtp1.model.Produto;
import com.pbtp1.repository.CategoriaRepository;
import com.pbtp1.repository.ProdutoRepository;
import com.pbtp1.shared.messaging.EventoProduto;
import com.pbtp1.shared.messaging.RabbitMQConstantes;
import com.pbtp1.model.StatusProduto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final OutboxService outboxService;

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));
    }

    @Transactional
    public Produto salvar(Produto produto) {
        if (produto.getEstoque() == null) {
            produto.setEstoque(10);
        }
        validarDados(produto);
        if (produto.getStatus() == null) {
            produto.setStatus(StatusProduto.ATIVO);
        }
        if (produto.getCategoria() != null && produto.getCategoria().getId() != null) {
            Categoria categoria = categoriaRepository.findById(produto.getCategoria().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + produto.getCategoria().getId()));
            produto.setCategoria(categoria);
        }

        Produto salvo = produtoRepository.save(produto);
        registrarEvento(salvo, RabbitMQConstantes.ROUTING_KEY_PRODUTO_CRIADO, "CRIADO");
        return salvo;
    }

    @Transactional
    public Produto atualizar(Long id, Produto produtoAtualizado) {
        Produto produto = buscarPorId(id);

        produto.setNome(produtoAtualizado.getNome());
        produto.setDescricao(produtoAtualizado.getDescricao());
        produto.setPreco(produtoAtualizado.getPreco());
        produto.setEstoque(produtoAtualizado.getEstoque() == null ? 0 : produtoAtualizado.getEstoque());
        produto.setStatus(produtoAtualizado.getStatus() == null ? StatusProduto.ATIVO : produtoAtualizado.getStatus());
        validarDados(produto);

        if (produtoAtualizado.getCategoria() != null && produtoAtualizado.getCategoria().getId() != null) {
            Categoria categoria = categoriaRepository.findById(produtoAtualizado.getCategoria().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + produtoAtualizado.getCategoria().getId()));
            produto.setCategoria(categoria);
        } else {
            produto.setCategoria(null);
        }

        Produto salvo = produtoRepository.save(produto);
        registrarEvento(salvo, RabbitMQConstantes.ROUTING_KEY_PRODUTO_ATUALIZADO, "ATUALIZADO");
        return salvo;
    }

    @Transactional
    public void deletar(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto não encontrado: " + id);
        }

        Produto produto = buscarPorId(id);
        produtoRepository.deleteById(id);
        registrarEvento(produto, RabbitMQConstantes.ROUTING_KEY_PRODUTO_EXCLUIDO, "EXCLUIDO");
    }

    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    public List<Produto> buscarPorFaixaPreco(BigDecimal min, BigDecimal max) {
        return produtoRepository.findByPrecoBetween(min, max);
    }

    public List<Produto> buscarPorCategoria(Long categoriaId) {
        return produtoRepository.findByCategoriaId(categoriaId);
    }

    public List<Produto> buscarPorTermo(String termo) {
        return produtoRepository.buscarPorTermo(termo);
    }

    public EventoProduto eventoAtual(Produto produto, String tipo) {
        return new EventoProduto(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria() != null ? produto.getCategoria().getId() : null,
                produto.getEstoque(),
                produto.getStatus() != null ? produto.getStatus().name() : StatusProduto.ATIVO.name(),
                tipo,
                LocalDateTime.now());
    }

    private void registrarEvento(Produto produto, String routingKey, String tipo) {
        EventoProduto evento = new EventoProduto(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria() != null ? produto.getCategoria().getId() : null,
                produto.getEstoque(),
                produto.getStatus() != null ? produto.getStatus().name() : StatusProduto.ATIVO.name(),
                tipo,
                LocalDateTime.now());

        outboxService.registrar(RabbitMQConstantes.EXCHANGE_PRODUTOS, routingKey, evento);
    }

    private void validarDados(Produto produto) {
        if (produto.getPreco() == null || produto.getPreco().signum() <= 0) {
            throw new IllegalArgumentException("O preço deve ser positivo");
        }
        if (produto.getEstoque() == null || produto.getEstoque() < 0) {
            throw new IllegalArgumentException("O estoque não pode ser negativo");
        }
    }
}

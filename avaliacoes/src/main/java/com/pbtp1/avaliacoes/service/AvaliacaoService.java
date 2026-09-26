package com.pbtp1.avaliacoes.service;

import com.pbtp1.avaliacoes.model.Avaliacao;
import com.pbtp1.avaliacoes.repository.ProdutoCatalogoRepository;
import com.pbtp1.avaliacoes.repository.AvaliacaoRepository;
import com.pbtp1.shared.dto.AvaliacaoDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ProdutoCatalogoRepository produtoCatalogoRepository;

    public record MediaAvaliacao(Long produtoId, Double media, Long total) {
    }

    public List<AvaliacaoDTO> listarTodas() {
        return avaliacaoRepository.findAll().stream().map(this::paraDTO).toList();
    }

    public List<AvaliacaoDTO> listarDoUsuario(Long usuarioId) {
        return avaliacaoRepository.findByUsuarioId(usuarioId).stream().map(this::paraDTO).toList();
    }

    public List<AvaliacaoDTO> listarPorProduto(Long produtoId) {
        return avaliacaoRepository.findByProdutoId(produtoId).stream().map(this::paraDTO).toList();
    }

    public List<AvaliacaoDTO> listarPorProdutoENota(Long produtoId, Integer nota) {
        return avaliacaoRepository.findByProdutoIdAndNota(produtoId, nota).stream().map(this::paraDTO).toList();
    }

    public AvaliacaoDTO buscarPorId(Long id) {
        return paraDTO(buscarEntidade(id));
    }

    public MediaAvaliacao calcularMediaPorProduto(Long produtoId) {
        Double media = avaliacaoRepository.calcularMediaPorProduto(produtoId).orElse(0.0);
        long total = avaliacaoRepository.countByProdutoId(produtoId);
        return new MediaAvaliacao(produtoId, media, total);
    }

    @Transactional
    public AvaliacaoDTO salvar(Avaliacao avaliacao, Long usuarioId) {
        validarProduto(avaliacao.getProdutoId());
        if (avaliacao.getCompraId() == null) {
            throw new EntityNotFoundException("Informe a compra relacionada à avaliação");
        }
        avaliacao.setUsuarioId(usuarioId);
        if (avaliacaoRepository.existsByCompraIdAndProdutoId(avaliacao.getCompraId(), avaliacao.getProdutoId()))
            throw new DataIntegrityViolationException("Este produto desta compra já foi avaliado");
        try {
            return paraDTO(avaliacaoRepository.save(avaliacao));
        } catch (DataIntegrityViolationException exception) {
            throw new DataIntegrityViolationException("Este produto desta compra já foi avaliado", exception);
        }
    }

    @Transactional
    public AvaliacaoDTO atualizar(Long id, Avaliacao avaliacaoAtualizada) {
        Avaliacao avaliacao = buscarEntidade(id);

        avaliacao.setNomeUsuario(avaliacaoAtualizada.getNomeUsuario());
        avaliacao.setNota(avaliacaoAtualizada.getNota());
        avaliacao.setComentario(avaliacaoAtualizada.getComentario());

        return paraDTO(avaliacaoRepository.save(avaliacao));
    }

    @Transactional
    public void deletar(Long id) {
        if (!avaliacaoRepository.existsById(id)) {
            throw new EntityNotFoundException("Avaliação não encontrada: " + id);
        }
        avaliacaoRepository.deleteById(id);
    }

    public Long usuarioId(Long id) {
        return buscarEntidade(id).getUsuarioId();
    }

    private Avaliacao buscarEntidade(Long id) {
        return avaliacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada: " + id));
    }

    private void validarProduto(Long produtoId) {
        if (!produtoCatalogoRepository.existsById(produtoId)) {
            throw new EntityNotFoundException("Produto não sincronizado no catálogo local: " + produtoId);
        }
    }

    private AvaliacaoDTO paraDTO(Avaliacao a) {
        return new AvaliacaoDTO(
                a.getId(),
                a.getProdutoId(),
                a.getCompraId(),
                a.getNomeUsuario(),
                a.getNota(),
                a.getComentario(),
                a.getDataCriacao());
    }
}

package com.pbtp1.service;

import com.pbtp1.model.Categoria;
import com.pbtp1.repository.CategoriaRepository;
import com.pbtp1.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + id));
    }

    public Categoria buscarPorNome(String nome) {
        return categoriaRepository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada: " + nome));
    }

    @Transactional
    public Categoria salvar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Categoria atualizar(Long id, Categoria categoriaAtualizada) {
        Categoria categoria = buscarPorId(id);
        categoria.setNome(categoriaAtualizada.getNome());
        categoria.setDescricao(categoriaAtualizada.getDescricao());
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void deletar(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new EntityNotFoundException("Categoria não encontrada: " + id);
        }
        long produtosAssociados = produtoRepository.countByCategoriaId(id);
        if (produtosAssociados > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Remova ou reclassifique os produtos desta categoria antes de excluí-la.");
        }
        categoriaRepository.deleteById(id);
    }
}

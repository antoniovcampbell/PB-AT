package com.pbtp1.repository;

import com.pbtp1.model.CarrinhoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarrinhoItemRepository extends JpaRepository<CarrinhoItem, Long> {
    List<CarrinhoItem> findByUsuarioIdOrderByIdAsc(Long usuarioId);
    Optional<CarrinhoItem> findByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);
    void deleteByUsuarioId(Long usuarioId);
}

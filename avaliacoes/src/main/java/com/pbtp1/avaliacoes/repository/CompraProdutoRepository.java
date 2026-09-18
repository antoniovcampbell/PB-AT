package com.pbtp1.avaliacoes.repository;

import com.pbtp1.avaliacoes.model.CompraProduto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraProdutoRepository extends JpaRepository<CompraProduto, Long> {
    boolean existsByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);
}

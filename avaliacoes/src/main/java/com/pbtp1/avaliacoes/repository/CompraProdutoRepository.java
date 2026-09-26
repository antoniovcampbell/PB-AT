package com.pbtp1.avaliacoes.repository;

import com.pbtp1.avaliacoes.model.CompraProduto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompraProdutoRepository extends JpaRepository<CompraProduto, Long> {
    Optional<CompraProduto> findByCompraIdAndProdutoIdAndUsuarioIdAndAtivaTrue(
            Long compraId, Long produtoId, Long usuarioId);

    boolean existsByUsuarioIdAndProdutoIdAndAtivaTrue(Long usuarioId, Long produtoId);

    Optional<CompraProduto> findByCompraIdAndProdutoId(Long compraId, Long produtoId);

    List<CompraProduto> findByDemonstracaoTrueAndAtivaTrue();
}

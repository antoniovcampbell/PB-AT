package com.pbtp1.avaliacoes.repository;

import com.pbtp1.avaliacoes.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    List<Avaliacao> findByProdutoId(Long produtoId);

    List<Avaliacao> findByProdutoIdAndNota(Long produtoId, Integer nota);

    List<Avaliacao> findByNota(Integer nota);

    boolean existsByProdutoId(Long produtoId);

    long countByProdutoId(Long produtoId);

    @Query("SELECT ROUND(AVG(a.nota), 2) FROM Avaliacao a WHERE a.produtoId = :produtoId")
    Optional<Double> calcularMediaPorProduto(@Param("produtoId") Long produtoId);
}
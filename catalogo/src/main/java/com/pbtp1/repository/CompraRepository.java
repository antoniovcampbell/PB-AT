package com.pbtp1.repository;

import com.pbtp1.model.Compra;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);

    @EntityGraph(attributePaths = "itens")
    List<Compra> findByDemonstracaoTrueOrderByIdAsc();

    @EntityGraph(attributePaths = "itens")
    Optional<Compra> findByUsuarioIdAndIdempotencyKey(Long usuarioId, String idempotencyKey);
}

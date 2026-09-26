package com.pbtp1.repository;

import com.pbtp1.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);
    Optional<Compra> findByUsuarioIdAndIdempotencyKey(Long usuarioId, String idempotencyKey);
}

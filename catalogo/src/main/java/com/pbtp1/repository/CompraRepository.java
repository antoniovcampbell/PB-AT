package com.pbtp1.repository;

import com.pbtp1.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);
}

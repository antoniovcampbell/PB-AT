package com.pbtp1.avaliacoes.repository;

import com.pbtp1.avaliacoes.model.ProdutoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoCatalogoRepository extends JpaRepository<ProdutoCatalogo, Long> {
}

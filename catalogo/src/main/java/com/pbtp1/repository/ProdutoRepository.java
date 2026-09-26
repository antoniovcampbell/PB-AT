
package com.pbtp1.repository;

import com.pbtp1.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByNomeContainingIgnoreCase(String nome);
    List<Produto> findByPrecoBetween(BigDecimal min, BigDecimal max);
    List<Produto> findByCategoriaId(Long categoriaId);
    List<Produto> findByCategoriaNomeIgnoreCase(String nome);
    Optional<Produto> findByNomeIgnoreCase(String nome);

    @Query("select p from Produto p where lower(p.nome) like lower(concat('%', :termo, '%')) "
            + "or lower(p.descricao) like lower(concat('%', :termo, '%'))")
    List<Produto> buscarPorTermo(@Param("termo") String termo);
}

package com.pbtp1.shared.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoProduto(
        Long produtoId,
        String nome,
        String descricao,
        BigDecimal preco,
        Long categoriaId,
        Integer estoque,
        String status,
        String tipo,
        LocalDateTime ocorridoEm) implements Serializable {
}

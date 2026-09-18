package com.pbtp1.shared.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoProduto(
        Long produtoId,
        String nome,
        String descricao,
        Double preco,
        Long categoriaId,
        Integer estoque,
        String status,
        String tipo,
        LocalDateTime ocorridoEm) implements Serializable {
}

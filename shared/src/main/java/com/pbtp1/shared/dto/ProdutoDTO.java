package com.pbtp1.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record ProdutoDTO(
        Long id,
        String nome,
        String descricao,
        Double preco) {
}
package com.pbtp1.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;


@JsonIgnoreProperties(ignoreUnknown = true)
public record AvaliacaoDTO(
        Long id,
        Long produtoId,
        String nomeUsuario,
        Integer nota,
        String comentario,
        LocalDateTime dataCriacao) {
}
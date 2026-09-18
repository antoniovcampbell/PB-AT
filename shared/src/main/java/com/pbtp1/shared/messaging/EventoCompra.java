package com.pbtp1.shared.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoCompra(
        Long compraId,
        Long usuarioId,
        Long produtoId,
        String tipo,
        LocalDateTime ocorridoEm) implements Serializable {
}

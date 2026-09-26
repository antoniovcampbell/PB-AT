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
        LocalDateTime ocorridoEm,
        boolean demonstracao,
        String nomeUsuario) implements Serializable {

    public EventoCompra(Long compraId, Long usuarioId, Long produtoId, String tipo,
                        LocalDateTime ocorridoEm, boolean demonstracao) {
        this(compraId, usuarioId, produtoId, tipo, ocorridoEm, demonstracao, null);
    }

    public EventoCompra(Long compraId, Long usuarioId, Long produtoId, String tipo, LocalDateTime ocorridoEm) {
        this(compraId, usuarioId, produtoId, tipo, ocorridoEm, false, null);
    }
}

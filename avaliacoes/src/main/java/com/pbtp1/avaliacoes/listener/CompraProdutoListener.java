package com.pbtp1.avaliacoes.listener;

import com.pbtp1.avaliacoes.model.CompraProduto;
import com.pbtp1.avaliacoes.repository.CompraProdutoRepository;
import com.pbtp1.shared.messaging.EventoCompra;
import com.pbtp1.shared.messaging.RabbitMQConstantes;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CompraProdutoListener {
    private final CompraProdutoRepository repository;

    @RabbitListener(queues = RabbitMQConstantes.FILA_COMPRAS,
            autoStartup = "${app.rabbitmq.listener.auto-startup:true}")
    @Transactional
    public void processar(EventoCompra evento) {
        if (evento == null || evento.compraId() == null || evento.usuarioId() == null || evento.produtoId() == null) {
            return;
        }

        Optional<CompraProduto> existente = repository.findByCompraIdAndProdutoId(evento.compraId(), evento.produtoId());
        if ("CANCELADA".equals(evento.tipo())) {
            existente.ifPresent(compra -> {
                compra.setAtiva(false);
                repository.save(compra);
            });
            return;
        }
        if (existente.isPresent()) {
            return;
        }
        repository.save(CompraProduto.builder()
                .compraId(evento.compraId())
                .usuarioId(evento.usuarioId())
                .nomeUsuario(evento.nomeUsuario())
                .produtoId(evento.produtoId())
                .demonstracao(evento.demonstracao())
                .ativa(true)
                .build());
    }
}

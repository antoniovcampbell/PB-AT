package com.pbtp1.avaliacoes.listener;

import com.pbtp1.avaliacoes.model.CompraProduto;
import com.pbtp1.avaliacoes.repository.CompraProdutoRepository;
import com.pbtp1.shared.messaging.EventoCompra;
import com.pbtp1.shared.messaging.RabbitMQConstantes;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CompraProdutoListener {
    private final CompraProdutoRepository repository;

    @RabbitListener(queues = RabbitMQConstantes.FILA_COMPRAS,
            autoStartup = "${app.rabbitmq.listener.auto-startup:true}")
    @Transactional
    public void processar(EventoCompra evento) {
        if (evento == null || evento.usuarioId() == null || evento.produtoId() == null
                || repository.existsByUsuarioIdAndProdutoId(evento.usuarioId(), evento.produtoId())) {
            return;
        }
        repository.save(CompraProduto.builder()
                .usuarioId(evento.usuarioId())
                .produtoId(evento.produtoId())
                .build());
    }
}

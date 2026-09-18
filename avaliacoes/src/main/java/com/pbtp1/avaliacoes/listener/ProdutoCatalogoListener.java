package com.pbtp1.avaliacoes.listener;

import com.pbtp1.avaliacoes.model.ProdutoCatalogo;
import com.pbtp1.avaliacoes.repository.ProdutoCatalogoRepository;
import com.pbtp1.shared.messaging.EventoProduto;
import com.pbtp1.shared.messaging.RabbitMQConstantes;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProdutoCatalogoListener {

    private final ProdutoCatalogoRepository produtoCatalogoRepository;

    @RabbitListener(queues = RabbitMQConstantes.FILA_PRODUTOS, autoStartup = "${app.rabbitmq.listener.auto-startup:true}")
    @Transactional
    public void processar(EventoProduto evento) {
        if (evento == null || evento.produtoId() == null) {
            return;
        }

        if ("EXCLUIDO".equals(evento.tipo())) {
            if (produtoCatalogoRepository.existsById(evento.produtoId())) {
                produtoCatalogoRepository.deleteById(evento.produtoId());
            }
            return;
        }

        produtoCatalogoRepository.save(ProdutoCatalogo.builder()
                .id(evento.produtoId())
                .nome(evento.nome())
                .descricao(evento.descricao())
                .preco(evento.preco())
                .categoriaId(evento.categoriaId())
                .estoque(evento.estoque())
                .status(evento.status())
                .atualizadoEm(evento.ocorridoEm() != null ? evento.ocorridoEm() : LocalDateTime.now())
                .build());
    }
}

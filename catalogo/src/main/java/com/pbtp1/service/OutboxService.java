package com.pbtp1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbtp1.model.EventoOutbox;
import com.pbtp1.repository.EventoOutboxRepository;
import com.pbtp1.shared.messaging.EventoCompra;
import com.pbtp1.shared.messaging.EventoProduto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {
    private final EventoOutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.outbox.confirm-timeout-ms:5000}")
    private long confirmTimeoutMs;

    public void registrar(String exchange, String routingKey, Object evento) {
        try {
            repository.save(EventoOutbox.builder()
                    .exchange(exchange)
                    .routingKey(routingKey)
                    .payload(objectMapper.writeValueAsString(evento))
                    .criadoEm(LocalDateTime.now())
                    .build());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível registrar o evento", exception);
        }
    }

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms:5000}")
    @Transactional
    public void publicarPendentes() {
        repository.findTop100ByPublicadoEmIsNullOrderByIdAsc().forEach(this::publicar);
    }

    private void publicar(EventoOutbox evento) {
        try {
            Object payload = evento.getRoutingKey().startsWith("produto.")
                    ? objectMapper.readValue(evento.getPayload(), EventoProduto.class)
                    : objectMapper.readValue(evento.getPayload(), EventoCompra.class);
            CorrelationData correlationData = new CorrelationData(String.valueOf(evento.getId()));
            rabbitTemplate.convertAndSend(evento.getExchange(), evento.getRoutingKey(), payload, correlationData);
            CorrelationData.Confirm confirm = correlationData.getFuture()
                    .get(confirmTimeoutMs, TimeUnit.MILLISECONDS);
            if (!confirm.ack()) {
                throw new IllegalStateException("Broker rejeitou a publicação: " + confirm.reason());
            }
            if (correlationData.getReturned() != null) {
                throw new IllegalStateException("Mensagem não roteada para nenhuma fila");
            }
            evento.setPublicadoEm(LocalDateTime.now());
            evento.setUltimoErro(null);
        } catch (Exception exception) {
            evento.setTentativas(evento.getTentativas() + 1);
            evento.setUltimoErro(mensagemDeErro(exception));
            log.warn("Falha ao publicar evento outbox {}. Nova tentativa será feita depois", evento.getId(), exception);
        }
        repository.save(evento);
    }

    private String mensagemDeErro(Exception exception) {
        String mensagem = exception.getMessage();
        return mensagem == null ? exception.getClass().getSimpleName() : mensagem.substring(0, Math.min(mensagem.length(), 1000));
    }
}

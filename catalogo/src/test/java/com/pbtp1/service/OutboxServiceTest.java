package com.pbtp1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbtp1.model.EventoOutbox;
import com.pbtp1.repository.EventoOutboxRepository;
import com.pbtp1.shared.messaging.EventoCompra;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OutboxServiceTest {

    @Test
    void deveMarcarEventoSomenteAposConfirmacaoDoBroker() {
        EventoOutbox evento = evento();
        EventoOutboxRepository repository = mock(EventoOutboxRepository.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        when(repository.findTop100ByPublicadoEmIsNullOrderByIdAsc()).thenReturn(List.of(evento));
        doAnswer(invocation -> {
            CorrelationData correlation = invocation.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).convertAndSend(eq("catalogo.compras.exchange"), eq("compra.criada"),
                any(EventoCompra.class), any(CorrelationData.class));

        OutboxService service = new OutboxService(repository, new ObjectMapper().findAndRegisterModules(), rabbitTemplate);
        ReflectionTestUtils.setField(service, "confirmTimeoutMs", 1000L);

        service.publicarPendentes();

        assertThat(evento.getPublicadoEm()).isNotNull();
        assertThat(evento.getTentativas()).isZero();
    }

    @Test
    void deveManterEventoPendenteQuandoBrokerRecusarPublicacao() {
        EventoOutbox evento = evento();
        EventoOutboxRepository repository = mock(EventoOutboxRepository.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        when(repository.findTop100ByPublicadoEmIsNullOrderByIdAsc()).thenReturn(List.of(evento));
        doAnswer(invocation -> {
            CorrelationData correlation = invocation.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(false, "exchange indisponivel"));
            return null;
        }).when(rabbitTemplate).convertAndSend(eq("catalogo.compras.exchange"), eq("compra.criada"),
                any(EventoCompra.class), any(CorrelationData.class));

        OutboxService service = new OutboxService(repository, new ObjectMapper().findAndRegisterModules(), rabbitTemplate);
        ReflectionTestUtils.setField(service, "confirmTimeoutMs", 1000L);

        service.publicarPendentes();

        assertThat(evento.getPublicadoEm()).isNull();
        assertThat(evento.getTentativas()).isEqualTo(1);
        assertThat(evento.getUltimoErro()).contains("Broker rejeitou");
    }

    private EventoOutbox evento() {
        return EventoOutbox.builder()
                .id(1L)
                .exchange("catalogo.compras.exchange")
                .routingKey("compra.criada")
                .payload("{\"compraId\":1,\"usuarioId\":2,\"produtoId\":3,\"tipo\":\"CRIADA\",\"ocorridoEm\":\"2026-09-25T19:00:00\",\"demonstracao\":false}")
                .criadoEm(LocalDateTime.now())
                .build();
    }
}

package com.pbtp1.config;

import com.pbtp1.shared.messaging.RabbitMQConstantes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange produtosExchange() {
        return new TopicExchange(RabbitMQConstantes.EXCHANGE_PRODUTOS, true, false);
    }

    @Bean
    public TopicExchange comprasExchange() {
        return new TopicExchange(RabbitMQConstantes.EXCHANGE_COMPRAS, true, false);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setMandatory(true);
        return template;
    }
}

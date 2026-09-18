package com.pbtp1.config;

import com.pbtp1.shared.messaging.RabbitMQConstantes;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
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
    public Queue produtosQueue() {
        return new Queue(RabbitMQConstantes.FILA_PRODUTOS, true);
    }

    @Bean
    public Binding produtosBinding(Queue produtosQueue, TopicExchange produtosExchange) {
        return BindingBuilder.bind(produtosQueue).to(produtosExchange).with("produto.*");
    }

    @Bean
    public JacksonJsonMessageConverter jacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}

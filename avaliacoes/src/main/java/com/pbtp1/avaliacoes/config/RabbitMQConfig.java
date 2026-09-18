package com.pbtp1.avaliacoes.config;

import com.pbtp1.shared.messaging.RabbitMQConstantes;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@EnableRabbit
@Profile("!test")
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    @Bean
    public TopicExchange produtosExchange() {
        return new TopicExchange(RabbitMQConstantes.EXCHANGE_PRODUTOS, true, false);
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
    public TopicExchange comprasExchange() {
        return new TopicExchange(RabbitMQConstantes.EXCHANGE_COMPRAS, true, false);
    }

    @Bean
    public Queue comprasQueue() {
        return new Queue(RabbitMQConstantes.FILA_COMPRAS, true);
    }

    @Bean
    public Binding comprasBinding(Queue comprasQueue, TopicExchange comprasExchange) {
        return BindingBuilder.bind(comprasQueue).to(comprasExchange).with("compra.*");
    }

    @Bean
    public JacksonJsonMessageConverter jacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

}

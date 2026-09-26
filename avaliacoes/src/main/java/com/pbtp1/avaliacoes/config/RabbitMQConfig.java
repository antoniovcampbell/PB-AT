package com.pbtp1.avaliacoes.config;

import com.pbtp1.shared.messaging.RabbitMQConstantes;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Qualifier;
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
        return QueueBuilder.durable(RabbitMQConstantes.FILA_PRODUTOS)
                .deadLetterExchange(RabbitMQConstantes.EXCHANGE_DEAD_LETTERS)
                .deadLetterRoutingKey(RabbitMQConstantes.ROUTING_KEY_PRODUTOS_DEAD_LETTERS)
                .build();
    }

    @Bean
    public Binding produtosBinding(@Qualifier("produtosQueue") Queue produtosQueue,
                                   @Qualifier("produtosExchange") TopicExchange produtosExchange) {
        return BindingBuilder.bind(produtosQueue).to(produtosExchange).with("produto.*");
    }

    @Bean
    public TopicExchange comprasExchange() {
        return new TopicExchange(RabbitMQConstantes.EXCHANGE_COMPRAS, true, false);
    }

    @Bean
    public Queue comprasQueue() {
        return QueueBuilder.durable(RabbitMQConstantes.FILA_COMPRAS)
                .deadLetterExchange(RabbitMQConstantes.EXCHANGE_DEAD_LETTERS)
                .deadLetterRoutingKey(RabbitMQConstantes.ROUTING_KEY_COMPRAS_DEAD_LETTERS)
                .build();
    }

    @Bean
    public Binding comprasBinding(@Qualifier("comprasQueue") Queue comprasQueue,
                                  @Qualifier("comprasExchange") TopicExchange comprasExchange) {
        return BindingBuilder.bind(comprasQueue).to(comprasExchange).with("compra.*");
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(RabbitMQConstantes.EXCHANGE_DEAD_LETTERS, true, false);
    }

    @Bean
    public Queue produtosDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMQConstantes.FILA_PRODUTOS_DEAD_LETTERS).build();
    }

    @Bean
    public Queue comprasDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMQConstantes.FILA_COMPRAS_DEAD_LETTERS).build();
    }

    @Bean
    public Binding produtosDeadLetterBinding(@Qualifier("produtosDeadLetterQueue") Queue produtosDeadLetterQueue,
                                             @Qualifier("deadLetterExchange") TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(produtosDeadLetterQueue).to(deadLetterExchange)
                .with(RabbitMQConstantes.ROUTING_KEY_PRODUTOS_DEAD_LETTERS);
    }

    @Bean
    public Binding comprasDeadLetterBinding(@Qualifier("comprasDeadLetterQueue") Queue comprasDeadLetterQueue,
                                            @Qualifier("deadLetterExchange") TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(comprasDeadLetterQueue).to(deadLetterExchange)
                .with(RabbitMQConstantes.ROUTING_KEY_COMPRAS_DEAD_LETTERS);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter,
            MethodInterceptor rabbitRetryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setErrorHandler(new ConditionalRejectingErrorHandler());
        factory.setAdviceChain(rabbitRetryInterceptor);
        return factory;
    }

    @Bean
    public MethodInterceptor rabbitRetryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxRetries(2)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public JacksonJsonMessageConverter jacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

}

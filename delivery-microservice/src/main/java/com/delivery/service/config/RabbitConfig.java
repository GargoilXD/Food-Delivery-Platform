package com.delivery.service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    // --- Dead Letter Infrastructure ---
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("order.events.dlx");
    }

    @Bean
    public Queue orderPlacedDlq() {
        return new Queue("order.placed.dlq", true);
    }

    @Bean
    public Binding orderPlacedDlqBinding(Queue orderPlacedDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(orderPlacedDlq).to(deadLetterExchange).with("order.placed");
    }

    @Bean
    public Queue orderCancelledDlq() {
        return new Queue("order.cancelled.dlq", true);
    }

    @Bean
    public Binding orderCancelledDlqBinding(Queue orderCancelledDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(orderCancelledDlq).to(deadLetterExchange).with("order.cancelled");
    }

    // --- Main Exchange ---
    @Bean
    public TopicExchange orderEventsExchange() {
        return new TopicExchange("order.events");
    }

    @Bean
    public TopicExchange deliveryEventsExchange() {
        return new TopicExchange("delivery.events");
    }

    // --- order.placed queue (with DLQ) ---
    @Bean
    public Queue orderPlacedQueue() {
        return QueueBuilder.durable("order.placed.queue")
                .withArgument("x-dead-letter-exchange", "order.events.dlx")
                .withArgument("x-dead-letter-routing-key", "order.placed")
                .build();
    }

    @Bean
    public Binding orderPlacedBinding(Queue orderPlacedQueue, TopicExchange orderEventsExchange) {
        return BindingBuilder.bind(orderPlacedQueue).to(orderEventsExchange).with("order.placed");
    }

    // --- order.cancelled queue (with DLQ) ---
    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable("order.cancelled.queue")
                .withArgument("x-dead-letter-exchange", "order.events.dlx")
                .withArgument("x-dead-letter-routing-key", "order.cancelled")
                .build();
    }

    @Bean
    public Binding orderCancelledBinding(Queue orderCancelledQueue, TopicExchange orderEventsExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(orderEventsExchange).with("order.cancelled");
    }
}

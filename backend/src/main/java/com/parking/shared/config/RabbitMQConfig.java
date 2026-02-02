package com.parking.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "parking.notifications";
    public static final String QUEUE_NAME = "email.send";
    public static final String ROUTING_KEY = "spots.listed";

    @Bean
    public TopicExchange parkingExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    @Bean
    public Binding binding(Queue emailQueue, TopicExchange parkingExchange) {
        return BindingBuilder
                .bind(emailQueue)
                .to(parkingExchange)
                .with(ROUTING_KEY);
    }
}

package com.parking.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String RESERVATION_QUEUE = "reservation.created.queue";
    public static final String RESERVATION_EXCHANGE = "reservation.exchange";
    public static final String RESERVATION_ROUTING_KEY = "reservation.created";

    @Bean
    public Queue reservationQueue() {
        return QueueBuilder.durable(RESERVATION_QUEUE).build();
    }

    @Bean
    public TopicExchange reservationExchange() {
        return new TopicExchange(RESERVATION_EXCHANGE);
    }

    @Bean
    public Binding reservationBinding(Queue reservationQueue, TopicExchange reservationExchange) {
        return BindingBuilder.bind(reservationQueue).to(reservationExchange).with(RESERVATION_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

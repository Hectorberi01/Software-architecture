package com.parking.messaging.publisher;

import com.parking.messaging.event.ReservationCreatedEvent;
import com.parking.shared.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReservationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public ReservationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes a reservation-created event to the RabbitMQ exchange.
     * The message is picked up by an external service that sends the confirmation
     * email.
     */
    public void publishReservationCreated(ReservationCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.RESERVATION_EXCHANGE,
                RabbitMQConfig.RESERVATION_ROUTING_KEY,
                event);
    }
}

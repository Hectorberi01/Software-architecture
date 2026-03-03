package com.parking.messaging.publisher;

import com.parking.messaging.event.ReservationCreatedEvent;
import com.parking.shared.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ReservationEventPublisher publisher;

    @Test
    void publishReservationCreated_sendsToCorrectExchangeAndRoutingKey() {
        ReservationCreatedEvent event = new ReservationCreatedEvent(
                1L, "employee@parking.com", "Employee",
                "A01", LocalDate.now(), LocalDate.now());

        publisher.publishReservationCreated(event);

        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitMQConfig.RESERVATION_EXCHANGE,
                RabbitMQConfig.RESERVATION_ROUTING_KEY,
                event);
    }
}

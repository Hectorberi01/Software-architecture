package com.parking.messaging.consumer;

import com.parking.messaging.event.ReservationCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationConsumer.class);

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "reservation.email.queue", durable = "true"), exchange = @Exchange(value = "reservation-exchange", type = "topic"), key = "reservation.created"))
    public void receiveReservationCreated(ReservationCreatedEvent event) {
        log.info("📧 PREPARING EMAIL NOTIFICATION 📧");
        log.info("To: {}", event.getUserEmail());
        log.info("Subject: Confirmation de votre demande de réservation (Place: {})", event.getSpotCode());
        log.info(
                "Body: Bonjour {}, votre réservation pour la place {} du {} au {} est bien enregistrée et en attente de vérification par un admin ou manager. Dès son approbation, vous recevrez une confirmation et vous pourrez procéder au Check-in le jour-J !",
                event.getUserFirstName(),
                event.getSpotCode(),
                event.getStartDate(),
                event.getEndDate());
        log.info("📧 EMAIL SUCCESSFULLY SENT VIA RABBITMQ CONSUMER 📧");
    }
}

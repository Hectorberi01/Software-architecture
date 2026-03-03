package com.parking.checkin.scheduler;

import com.parking.reservation.model.Reservation;
import com.parking.reservation.model.ReservationStatus;
import com.parking.reservation.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class NoShowScheduler {

    private static final Logger log = LoggerFactory.getLogger(NoShowScheduler.class);

    private final ReservationRepository reservationRepository;

    public NoShowScheduler(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Runs every day at 11:00 AM server time.
     * Any PENDING reservation for today that hasn't been checked in is marked as
     * NO_SHOW,
     * automatically releasing the spot for other users to book.
     *
     * Cron: "0 0 11 * * *" = second 0, minute 0, hour 11, every day, every month,
     * every weekday
     */
    @Scheduled(cron = "0 0 11 * * *")
    @Transactional
    public void releaseNoShowReservations() {
        LocalDate today = LocalDate.now();
        List<Reservation> pendingReservations = reservationRepository
                .findApprovedReservationsForDate(today);

        if (pendingReservations.isEmpty()) {
            log.info("No-show check at 11:00: no pending reservations for today.");
            return;
        }

        log.info("No-show check at 11:00: releasing {} unchecked reservations", pendingReservations.size());
        int updatedCount = 0;
        for (Reservation res : pendingReservations) {
            res.setStatus(ReservationStatus.NO_SHOW);
            reservationRepository.save(res);
            updatedCount++;
            log.info("Reservation {} marked as NO_SHOW", res.getId());
        }

        log.info("No-show check complete. {} reservations updated.", updatedCount);
    }
}

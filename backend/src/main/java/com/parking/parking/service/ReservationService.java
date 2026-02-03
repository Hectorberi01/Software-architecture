package com.parking.parking.service;

import com.parking.parking.dto.CreateReservationRequest;
import com.parking.parking.dto.ReservationDto;
import com.parking.parking.model.ParkingSpot;
import com.parking.parking.model.Reservation;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.parking.repository.ReservationRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final RabbitTemplate rabbitTemplate;

    public ReservationService(ReservationRepository reservationRepository,
                              ParkingSpotRepository parkingSpotRepository,
                              RabbitTemplate rabbitTemplate) {
        this.reservationRepository = reservationRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public ReservationDto createReservation(CreateReservationRequest request) {
        if (request.reservationDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Reservation date cannot be in the past");
        }

        DayOfWeek dayOfWeek = request.reservationDate().getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Reservations are not allowed on weekends");
        }

        long activeReservations = reservationRepository.countActiveReservationsByEmail(
            request.userEmail(), LocalDate.now());
        if (activeReservations >= 5) {
            throw new IllegalStateException("Maximum 5 active reservations allowed per user");
        }

        if (reservationRepository.existsByParkingSpotIdAndReservationDate(
                request.parkingSpotId(), request.reservationDate())) {
            throw new IllegalStateException("Spot is already reserved for this date");
        }

        ParkingSpot spot = parkingSpotRepository.findById(request.parkingSpotId())
            .orElseThrow(() -> new IllegalArgumentException("Parking spot not found"));

        Reservation reservation = new Reservation();
        reservation.setParkingSpot(spot);
        reservation.setUserEmail(request.userEmail());
        reservation.setReservationDate(request.reservationDate());

        Reservation saved = reservationRepository.save(reservation);

        rabbitTemplate.convertAndSend(
            "parking.notifications",
            "reservation.created",
            "Reservation created: spot " + spot.getCode() +
            " for " + request.userEmail() + " on " + request.reservationDate()
        );

        return toDto(saved);
    }

    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        reservationRepository.delete(reservation);

        rabbitTemplate.convertAndSend(
            "parking.notifications",
            "reservation.cancelled",
            "Reservation cancelled: spot " + reservation.getParkingSpot().getCode() +
            " for " + reservation.getUserEmail() + " on " + reservation.getReservationDate()
        );
    }

    public List<Long> getReservedSpotIdsByDate(LocalDate date) {
        return reservationRepository.findReservedSpotIdsByDate(date);
    }

    public List<ReservationDto> getReservationsByDate(LocalDate date) {
        return reservationRepository.findByReservationDate(date).stream()
            .map(this::toDto)
            .toList();
    }

    public List<ReservationDto> getActiveReservationsByEmail(String email) {
        return reservationRepository.findByUserEmailAndReservationDateGreaterThanEqual(email, LocalDate.now())
            .stream()
            .map(this::toDto)
            .toList();
    }

    public long countActiveReservations(String email) {
        return reservationRepository.countActiveReservationsByEmail(email, LocalDate.now());
    }

    private ReservationDto toDto(Reservation reservation) {
        return new ReservationDto(
            reservation.getId(),
            reservation.getParkingSpot().getId(),
            reservation.getParkingSpot().getCode(),
            reservation.getUserEmail(),
            reservation.getReservationDate(),
            reservation.getCreatedAt()
        );
    }
}

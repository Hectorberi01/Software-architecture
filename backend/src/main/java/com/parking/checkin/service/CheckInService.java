package com.parking.checkin.service;

import com.parking.reservation.model.Reservation;
import com.parking.reservation.model.ReservationStatus;
import com.parking.reservation.repository.ReservationRepository;
import com.parking.checkin.dto.CheckInResponse;
import com.parking.user.model.User;
import com.parking.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class CheckInService {

        private final ReservationRepository reservationRepository;
        private final UserRepository userRepository;

        public CheckInService(ReservationRepository reservationRepository, UserRepository userRepository) {
                this.reservationRepository = reservationRepository;
                this.userRepository = userRepository;
        }

        /**
         * Performs check-in for the authenticated user's reservation on a given spot
         * today.
         * A user must check in on the start date of their reservation to confirm
         * occupancy.
         */
        @Transactional
        public CheckInResponse checkIn(Long spotId, String userEmail) {
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                LocalDate today = LocalDate.now();

                Reservation reservation = reservationRepository
                                .findApprovedReservationsForDate(today)
                                .stream()
                                .filter(r -> r.getSpot().getId().equals(spotId))
                                .filter(r -> r.getUser().getId().equals(user.getId()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                                "No active reservation found for spot " + spotId + " today"));

                reservation.setStatus(ReservationStatus.CHECKED_IN);
                reservationRepository.save(reservation);

                return new CheckInResponse(
                                reservation.getId(),
                                reservation.getSpot().getCode(),
                                ReservationStatus.CHECKED_IN,
                                "Check-in successful for spot " + reservation.getSpot().getCode());
        }

        /**
         * QR Code endpoint: allows check-in by scanning a QR code linked to a specific
         * spot.
         * The endpoint is accessible without authentication (for QR scanner use).
         */
        @Transactional
        public CheckInResponse checkInBySpotCode(String spotCode, String userEmail) {
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                LocalDate today = LocalDate.now();

                Reservation reservation = reservationRepository
                                .findApprovedReservationsForDate(today)
                                .stream()
                                .filter(r -> r.getSpot().getCode().equals(spotCode))
                                .filter(r -> r.getUser().getId().equals(user.getId()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                                "No active reservation found for spot " + spotCode + " today"));

                reservation.setStatus(ReservationStatus.CHECKED_IN);
                reservationRepository.save(reservation);

                return new CheckInResponse(
                                reservation.getId(),
                                reservation.getSpot().getCode(),
                                ReservationStatus.CHECKED_IN,
                                "Check-in successful for spot " + spotCode);
        }
}

package com.parking.reservation.service;

import com.parking.messaging.event.ReservationCreatedEvent;
import com.parking.messaging.publisher.ReservationEventPublisher;
import com.parking.parking.model.ParkingSpot;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.reservation.dto.CreateReservationRequest;
import com.parking.reservation.dto.ReservationResponse;
import com.parking.reservation.model.Reservation;
import com.parking.reservation.model.ReservationStatus;
import com.parking.reservation.repository.ReservationRepository;
import com.parking.shared.util.BusinessDayUtils;
import com.parking.user.model.Role;
import com.parking.user.model.User;
import com.parking.user.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final int EMPLOYEE_MAX_BUSINESS_DAYS = 5;
    private static final int MANAGER_MAX_CALENDAR_DAYS = 30;

    private final ReservationRepository reservationRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final UserRepository userRepository;
    private final ReservationEventPublisher eventPublisher;

    public ReservationService(ReservationRepository reservationRepository,
            ParkingSpotRepository parkingSpotRepository,
            UserRepository userRepository,
            ReservationEventPublisher eventPublisher) {
        this.reservationRepository = reservationRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        validateDateRange(request.getStartDate(), request.getEndDate(), user);

        ParkingSpot spot = parkingSpotRepository.findById(request.getSpotId())
                .orElseThrow(() -> new IllegalArgumentException("Parking spot not found: " + request.getSpotId()));

        // Check spot availability (no conflicting active reservations)
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                spot.getId(), request.getStartDate(), request.getEndDate());
        if (!conflicts.isEmpty()) {
            throw new IllegalStateException("Spot " + spot.getCode() + " is already reserved for the requested period");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setSpot(spot);
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());
        reservation.setStatus(ReservationStatus.PENDING);
        Reservation saved = reservationRepository.save(reservation);

        // Publish event to RabbitMQ for email notification
        eventPublisher.publishReservationCreated(new ReservationCreatedEvent(
                saved.getId(), user.getEmail(), user.getFirstName(),
                spot.getCode(), saved.getStartDate(), saved.getEndDate()));

        return ReservationResponse.from(saved);
    }

    public List<ReservationResponse> getMyReservations(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return reservationRepository.findByUserId(user.getId()).stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAllHistory().stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponse cancelReservation(Long reservationId, String userEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Employee can only cancel their own; Admins and Managers can cancel any
        boolean isOwner = reservation.getUser().getEmail().equals(userEmail);
        boolean isAdminOrManager = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MANAGER;

        if (!isOwner && !isAdminOrManager) {
            throw new AccessDeniedException("You can only cancel your own reservations");
        }

        if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
            throw new IllegalStateException("Cannot cancel a reservation that has already been checked in");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse approveReservation(Long reservationId, String userEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException("Only admins and managers can approve reservations");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING reservations can be approved");
        }

        reservation.setStatus(ReservationStatus.APPROVED);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse rejectReservation(Long reservationId, String userEmail) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.MANAGER) {
            throw new AccessDeniedException("Only admins and managers can reject reservations");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only PENDING reservations can be rejected");
        }

        reservation.setStatus(ReservationStatus.REJECTED);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    /**
     * Validates reservation duration based on user role.
     * - EMPLOYEE: maximum 5 business days (cannot book a full month)
     * - MANAGER: maximum 30 calendar days (special privilege)
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate, User user) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        // Immediate reservation is allowed (same day)
        LocalDate today = LocalDate.now();
        if (startDate.isBefore(today)) {
            throw new IllegalArgumentException("Cannot reserve in the past");
        }

        if (user.getRole() == Role.MANAGER || user.getRole() == Role.ADMIN) {
            // Managers/admins can book up to 30 calendar days
            long calendarDays = endDate.toEpochDay() - startDate.toEpochDay() + 1;
            if (calendarDays > MANAGER_MAX_CALENDAR_DAYS) {
                throw new IllegalArgumentException(
                        "Reservation duration cannot exceed " + MANAGER_MAX_CALENDAR_DAYS + " calendar days");
            }
        } else {
            // Standard employees: max 5 business days
            long businessDays = BusinessDayUtils.countBusinessDays(startDate, endDate);
            if (businessDays > EMPLOYEE_MAX_BUSINESS_DAYS) {
                throw new IllegalArgumentException(
                        "Reservation duration cannot exceed " + EMPLOYEE_MAX_BUSINESS_DAYS + " business days");
            }
        }
    }
}

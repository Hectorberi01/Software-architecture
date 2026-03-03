package com.parking.reservation.service;

import com.parking.messaging.publisher.ReservationEventPublisher;
import com.parking.parking.model.ParkingSpot;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.reservation.dto.CreateReservationRequest;

import com.parking.reservation.model.Reservation;

import com.parking.reservation.repository.ReservationRepository;
import com.parking.user.model.Role;
import com.parking.user.model.User;
import com.parking.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReservationEventPublisher eventPublisher;

    @InjectMocks
    private ReservationService reservationService;

    private User employee;
    private User manager;
    private ParkingSpot spot;

    @BeforeEach
    void setUp() {
        employee = new User();
        employee.setId(1L);
        employee.setEmail("employee@parking.com");
        employee.setRole(Role.EMPLOYEE);
        employee.setEnabled(true);

        manager = new User();
        manager.setId(2L);
        manager.setEmail("manager@parking.com");
        manager.setRole(Role.MANAGER);
        manager.setEnabled(true);

        spot = new ParkingSpot();
        spot.setId(1L);
        spot.setCode("A01");
        spot.setRow("A");
        spot.setNumber(1);
        spot.setHasCharger(true);
    }

    @Test
    void employee_canBook_upTo5BusinessDays() {
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        if (monday.isBefore(LocalDate.now()))
            monday = monday.plusWeeks(1);
        LocalDate friday = monday.plusDays(4); // Mon to Fri = 5 business days

        CreateReservationRequest request = buildRequest(spot.getId(), monday, friday);
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(parkingSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));
        when(reservationRepository.findConflictingReservations(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any())).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setUser(employee);
            r.setSpot(spot);
            return r;
        });

        assertThatCode(() -> reservationService.createReservation(request, employee.getEmail()))
                .doesNotThrowAnyException();
    }

    @Test
    void employee_cannotBook_moreThan5BusinessDays() {
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        if (monday.isBefore(LocalDate.now()))
            monday = monday.plusWeeks(1);
        LocalDate followingMonday = monday.plusDays(7); // 6 business days

        CreateReservationRequest request = buildRequest(spot.getId(), monday, followingMonday);
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> reservationService.createReservation(request, employee.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5 business days");
    }

    @Test
    void manager_canBook_upTo30CalendarDays() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(29); // 30 calendar days

        CreateReservationRequest request = buildRequest(spot.getId(), start, end);
        when(userRepository.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));
        when(parkingSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));
        when(reservationRepository.findConflictingReservations(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any())).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setUser(manager);
            r.setSpot(spot);
            return r;
        });

        assertThatCode(() -> reservationService.createReservation(request, manager.getEmail()))
                .doesNotThrowAnyException();
    }

    @Test
    void sameDayReservation_isAllowed() {
        LocalDate today = LocalDate.now();

        CreateReservationRequest request = buildRequest(spot.getId(), today, today);
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(parkingSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));
        when(reservationRepository.findConflictingReservations(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any())).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setUser(employee);
            r.setSpot(spot);
            return r;
        });

        assertThatCode(() -> reservationService.createReservation(request, employee.getEmail()))
                .doesNotThrowAnyException();
    }

    @Test
    void emailEvent_isPublished_afterReservation() {
        LocalDate today = LocalDate.now();

        CreateReservationRequest request = buildRequest(spot.getId(), today, today);
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(parkingSpotRepository.findById(spot.getId())).thenReturn(Optional.of(spot));
        when(reservationRepository.findConflictingReservations(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any())).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setUser(employee);
            r.setSpot(spot);
            return r;
        });

        reservationService.createReservation(request, employee.getEmail());

        verify(eventPublisher, times(1)).publishReservationCreated(any());
    }

    @Test
    void manager_canReject_pendingReservation() {
        Reservation pending = new Reservation();
        pending.setId(10L);
        pending.setStatus(com.parking.reservation.model.ReservationStatus.PENDING);
        pending.setUser(employee);
        pending.setSpot(spot);
        pending.setStartDate(LocalDate.now());
        pending.setEndDate(LocalDate.now());

        when(reservationRepository.findById(10L)).thenReturn(Optional.of(pending));
        when(userRepository.findByEmail(manager.getEmail())).thenReturn(Optional.of(manager));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        com.parking.reservation.dto.ReservationResponse res = reservationService.rejectReservation(10L,
                manager.getEmail());

        assertThat(res.getStatus()).isEqualTo(com.parking.reservation.model.ReservationStatus.REJECTED);
    }

    private CreateReservationRequest buildRequest(Long spotId, LocalDate start, LocalDate end) {
        CreateReservationRequest req = new CreateReservationRequest();
        req.setSpotId(spotId);
        req.setStartDate(start);
        req.setEndDate(end);
        return req;
    }
}

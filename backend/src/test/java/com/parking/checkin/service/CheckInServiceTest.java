package com.parking.checkin.service;

import com.parking.checkin.dto.CheckInResponse;
import com.parking.parking.model.ParkingSpot;
import com.parking.reservation.model.Reservation;
import com.parking.reservation.model.ReservationStatus;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CheckInService checkInService;

    private User employee;
    private ParkingSpot spot;
    private Reservation pendingReservation;

    @BeforeEach
    void setUp() {
        employee = new User();
        employee.setId(1L);
        employee.setEmail("employee@parking.com");
        employee.setRole(Role.EMPLOYEE);

        spot = new ParkingSpot();
        spot.setId(1L);
        spot.setCode("A01");

        pendingReservation = new Reservation();
        pendingReservation.setId(1L);
        pendingReservation.setUser(employee);
        pendingReservation.setSpot(spot);
        pendingReservation.setStartDate(LocalDate.now());
        pendingReservation.setEndDate(LocalDate.now());
        pendingReservation.setStatus(ReservationStatus.APPROVED);
    }

    @Test
    void checkIn_success_setsStatusToCheckedIn() {
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(reservationRepository.findApprovedReservationsForDate(LocalDate.now()))
                .thenReturn(List.of(pendingReservation));
        when(reservationRepository.save(any())).thenReturn(pendingReservation);

        CheckInResponse response = checkInService.checkIn(spot.getId(), employee.getEmail());

        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CHECKED_IN);
        assertThat(response.getSpotCode()).isEqualTo("A01");
    }

    @Test
    void checkIn_fails_whenNoActiveReservation() {
        when(userRepository.findByEmail(employee.getEmail())).thenReturn(Optional.of(employee));
        when(reservationRepository.findApprovedReservationsForDate(LocalDate.now()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> checkInService.checkIn(spot.getId(), employee.getEmail()))
                .isInstanceOf(IllegalStateException.class);
    }
}

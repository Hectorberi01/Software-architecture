package com.parking.parking.service;

import com.parking.parking.dto.CreateReservationRequest;
import com.parking.parking.dto.ReservationDto;
import com.parking.parking.model.ParkingSpot;
import com.parking.parking.model.Reservation;
import com.parking.parking.model.User;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.parking.repository.ReservationRepository;
import com.parking.parking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
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
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ReservationService reservationService;

    private ParkingSpot sampleSpot;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleSpot = new ParkingSpot(1L, "A01", "A", 1, true);
        sampleUser = new User();
        sampleUser.setId(42L);
        sampleUser.setEmail("test@email.com");
        sampleUser.setPasswordHash("hash");
        sampleUser.setFirstName("Test");
        sampleUser.setLastName("User");
    }

    private LocalDate getNextWeekday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private LocalDate getNextSaturday() {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != DayOfWeek.SATURDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    @Test
    @DisplayName("createReservation succeeds for valid weekday request")
    void createReservation_shouldSucceedForValidRequest() {
        LocalDate futureDate = getNextWeekday();
        CreateReservationRequest request = new CreateReservationRequest(
            1L, sampleUser.getId(), futureDate
        );

        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
        when(reservationRepository.countActiveReservationsByUserId(eq(sampleUser.getId()), any(LocalDate.class)))
            .thenReturn(0L);
        when(reservationRepository.existsByParkingSpotIdAndReservationDate(1L, futureDate))
            .thenReturn(false);
        when(parkingSpotRepository.findById(1L)).thenReturn(Optional.of(sampleSpot));
        when(reservationRepository.save(any(Reservation.class)))
            .thenAnswer(inv -> {
                Reservation r = inv.getArgument(0);
                r.setId(100L);
                r.setCreatedAt(LocalDateTime.now());
                return r;
            });

        ReservationDto result = reservationService.createReservation(request);

        assertThat(result.parkingSpotId()).isEqualTo(1L);
        assertThat(result.spotCode()).isEqualTo("A01");
        assertThat(result.userId()).isEqualTo(sampleUser.getId());
        assertThat(result.userEmail()).isEqualTo("test@email.com");
        assertThat(result.reservationDate()).isEqualTo(futureDate);
        verify(rabbitTemplate).convertAndSend(eq("parking.notifications"),
            eq("reservation.created"), anyString());
    }

    @Test
    @DisplayName("createReservation fails for past date")
    void createReservation_shouldFailForPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        CreateReservationRequest request = new CreateReservationRequest(
            1L, sampleUser.getId(), pastDate
        );
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("past");
    }

    @Test
    @DisplayName("createReservation fails when spot already reserved")
    void createReservation_shouldFailWhenSpotAlreadyReserved() {
        LocalDate futureDate = getNextWeekday();
        CreateReservationRequest request = new CreateReservationRequest(
            1L, sampleUser.getId(), futureDate
        );

        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
        when(reservationRepository.countActiveReservationsByUserId(eq(sampleUser.getId()), any(LocalDate.class)))
            .thenReturn(0L);
        when(reservationRepository.existsByParkingSpotIdAndReservationDate(1L, futureDate))
            .thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already reserved");
    }

    @Test
    @DisplayName("createReservation fails when user has 5 active reservations")
    void createReservation_shouldFailWhenMaxReservationsReached() {
        LocalDate futureDate = getNextWeekday();
        CreateReservationRequest request = new CreateReservationRequest(
            1L, sampleUser.getId(), futureDate
        );

        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
        when(reservationRepository.countActiveReservationsByUserId(eq(sampleUser.getId()), any(LocalDate.class)))
            .thenReturn(5L);

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Maximum 5");
    }

    @Test
    @DisplayName("createReservation fails for weekend date")
    void createReservation_shouldFailForWeekend() {
        LocalDate saturday = getNextSaturday();
        CreateReservationRequest request = new CreateReservationRequest(
            1L, sampleUser.getId(), saturday
        );
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> reservationService.createReservation(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("weekend");
    }

    @Test
    @DisplayName("cancelReservation deletes and publishes message")
    void cancelReservation_shouldDeleteAndPublishMessage() {
        Reservation reservation = new Reservation();
        reservation.setId(100L);
        reservation.setParkingSpot(sampleSpot);
        reservation.setUser(sampleUser);
        reservation.setReservationDate(LocalDate.now().plusDays(1));

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(100L);

        verify(reservationRepository).delete(reservation);
        verify(rabbitTemplate).convertAndSend(eq("parking.notifications"),
            eq("reservation.cancelled"), anyString());
    }

    @Test
    @DisplayName("getReservationHistory returns active and past reservations for a user")
    void getReservationHistory_shouldReturnActiveAndPast() {
        Reservation active = new Reservation();
        active.setId(1L);
        active.setParkingSpot(sampleSpot);
        active.setUser(sampleUser);
        active.setReservationDate(LocalDate.now().plusDays(2));

        Reservation past = new Reservation();
        past.setId(2L);
        past.setParkingSpot(sampleSpot);
        past.setUser(sampleUser);
        past.setReservationDate(LocalDate.now().minusDays(2));

        when(reservationRepository.findByUserIdAndReservationDateGreaterThanEqual(eq(sampleUser.getId()), any(LocalDate.class)))
            .thenReturn(java.util.List.of(active));
        when(reservationRepository.findByUserIdAndReservationDateLessThan(eq(sampleUser.getId()), any(LocalDate.class)))
            .thenReturn(java.util.List.of(past));

        var history = reservationService.getReservationHistory(sampleUser.getId());

        assertThat(history.activeReservations()).hasSize(1);
        assertThat(history.pastReservations()).hasSize(1);
        assertThat(history.activeReservations().get(0).id()).isEqualTo(1L);
        assertThat(history.pastReservations().get(0).id()).isEqualTo(2L);
    }
}

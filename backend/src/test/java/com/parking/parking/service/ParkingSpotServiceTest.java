package com.parking.parking.service;

import com.parking.parking.dto.ParkingSpotDto;
import com.parking.parking.model.ParkingSpot;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.reservation.model.Reservation;
import com.parking.reservation.model.ReservationStatus;
import com.parking.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSpotServiceTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ParkingSpotService parkingSpotService;

    @Test
    void getAvailableSpots_excludesAlreadyReservedSpots() {
        ParkingSpot spotA = buildSpot(1L, "A01", "A", true);
        ParkingSpot spotB = buildSpot(2L, "B01", "B", false);

        Reservation activeReservation = new Reservation();
        activeReservation.setSpot(spotA);
        activeReservation.setStartDate(LocalDate.now());
        activeReservation.setEndDate(LocalDate.now().plusDays(1));
        activeReservation.setStatus(ReservationStatus.PENDING);

        when(parkingSpotRepository.findAll()).thenReturn(List.of(spotA, spotB));
        when(reservationRepository.findAll()).thenReturn(List.of(activeReservation));

        List<ParkingSpotDto> available = parkingSpotService.getAvailableSpots(
                LocalDate.now(), LocalDate.now().plusDays(1), null);

        assertThat(available).hasSize(1);
        assertThat(available.get(0).getCode()).isEqualTo("B01");
    }

    @Test
    void getAvailableSpots_withChargerFilter_returnsOnlyElectricSpots() {
        ParkingSpot spotA = buildSpot(1L, "A01", "A", true);
        ParkingSpot spotB = buildSpot(2L, "B01", "B", false);

        when(parkingSpotRepository.findAll()).thenReturn(List.of(spotA, spotB));
        when(reservationRepository.findAll()).thenReturn(List.of());

        List<ParkingSpotDto> available = parkingSpotService.getAvailableSpots(
                LocalDate.now(), LocalDate.now().plusDays(1), true);

        assertThat(available).hasSize(1);
        assertThat(available.get(0).isHasCharger()).isTrue();
    }

    private ParkingSpot buildSpot(Long id, String code, String row, boolean hasCharger) {
        ParkingSpot s = new ParkingSpot();
        s.setId(id);
        s.setCode(code);
        s.setRow(row);
        s.setNumber(1);
        s.setHasCharger(hasCharger);
        return s;
    }
}

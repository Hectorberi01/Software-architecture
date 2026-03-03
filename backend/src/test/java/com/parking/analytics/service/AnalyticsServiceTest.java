package com.parking.analytics.service;

import com.parking.analytics.dto.AnalyticsResponse;
import com.parking.analytics.repository.AnalyticsRepository;
import com.parking.parking.model.ParkingSpot;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.reservation.model.ReservationStatus;
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
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;
    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void getDashboardStats_returnsCorrectRates() {
        when(parkingSpotRepository.count()).thenReturn(60L);
        when(analyticsRepository.countCurrentlyOccupied(any(LocalDate.class))).thenReturn(30L);
        when(analyticsRepository.count()).thenReturn(100L);
        when(analyticsRepository.countByStatus(ReservationStatus.NO_SHOW)).thenReturn(10L);
        when(analyticsRepository.countElectricSpotsInUse(any(LocalDate.class))).thenReturn(5L);
        when(parkingSpotRepository.findByHasCharger(true)).thenReturn(List.of(
                new ParkingSpot(), new ParkingSpot(), new ParkingSpot(),
                new ParkingSpot(), new ParkingSpot(), new ParkingSpot(),
                new ParkingSpot(), new ParkingSpot(), new ParkingSpot(),
                new ParkingSpot(), new ParkingSpot(), new ParkingSpot(),
                new ParkingSpot(), new ParkingSpot(), new ParkingSpot(),
                new ParkingSpot(), new ParkingSpot(), new ParkingSpot(),
                new ParkingSpot(), new ParkingSpot())); // 20 electric spots

        AnalyticsResponse result = analyticsService.getDashboardStats();

        assertThat(result.getOccupancyRate()).isEqualTo(50.0); // 30/60 * 100
        assertThat(result.getNoShowRate()).isEqualTo(10.0); // 10/100 * 100
        assertThat(result.getElectricUsageRate()).isEqualTo(25.0); // 5/20 * 100
    }
}

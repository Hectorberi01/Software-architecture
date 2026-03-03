package com.parking.analytics.service;

import com.parking.analytics.dto.AnalyticsResponse;
import com.parking.analytics.repository.AnalyticsRepository;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.reservation.model.ReservationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final ParkingSpotRepository parkingSpotRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository,
            ParkingSpotRepository parkingSpotRepository) {
        this.analyticsRepository = analyticsRepository;
        this.parkingSpotRepository = parkingSpotRepository;
    }

    /**
     * Gathers parking statistics for the manager dashboard.
     * Includes: occupancy rate, no-show rate, electric usage rate.
     */
    public AnalyticsResponse getDashboardStats() {
        LocalDate today = LocalDate.now();

        long totalSpots = parkingSpotRepository.count();
        long occupiedToday = analyticsRepository.countCurrentlyOccupied(today);
        long totalReservations = analyticsRepository.count();
        long noShowCount = analyticsRepository.countByStatus(ReservationStatus.NO_SHOW);
        long electricSpotsInUse = analyticsRepository.countElectricSpotsInUse(today);
        long totalElectricSpots = parkingSpotRepository.findByHasCharger(true).size();

        return new AnalyticsResponse(
                totalSpots, occupiedToday, totalReservations,
                noShowCount, electricSpotsInUse, totalElectricSpots);
    }
}

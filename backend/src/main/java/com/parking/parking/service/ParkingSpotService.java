package com.parking.parking.service;

import com.parking.parking.dto.ParkingSpotDto;
import com.parking.parking.repository.ParkingSpotRepository;
import com.parking.reservation.model.ReservationStatus;
import com.parking.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParkingSpotService {

    private final ParkingSpotRepository parkingSpotRepository;
    private final ReservationRepository reservationRepository;

    public ParkingSpotService(ParkingSpotRepository parkingSpotRepository,
            ReservationRepository reservationRepository) {
        this.parkingSpotRepository = parkingSpotRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ParkingSpotDto> getAllSpots() {
        return parkingSpotRepository.findAll().stream()
                .map(ParkingSpotDto::from)
                .collect(Collectors.toList());
    }

    /**
     * Returns all parking spots that are available (not reserved) between the given
     * dates.
     * Optionally filters to only spots with chargers (rows A and F).
     */
    public List<ParkingSpotDto> getAvailableSpots(LocalDate startDate, LocalDate endDate, Boolean withCharger) {
        List<Long> occupiedSpotIds = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED
                        && r.getStatus() != ReservationStatus.NO_SHOW)
                .filter(r -> !r.getStartDate().isAfter(endDate) && !r.getEndDate().isBefore(startDate))
                .map(r -> r.getSpot().getId())
                .collect(Collectors.toList());

        return parkingSpotRepository.findAll().stream()
                .filter(spot -> !occupiedSpotIds.contains(spot.getId()))
                .filter(spot -> withCharger == null || spot.isHasCharger() == withCharger)
                .map(ParkingSpotDto::from)
                .collect(Collectors.toList());
    }

    public ParkingSpotDto getSpotById(Long id) {
        return parkingSpotRepository.findById(id)
                .map(ParkingSpotDto::from)
                .orElseThrow(() -> new IllegalArgumentException("Spot not found: " + id));
    }
}

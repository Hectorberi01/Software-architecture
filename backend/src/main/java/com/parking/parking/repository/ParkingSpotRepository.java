package com.parking.parking.repository;

import com.parking.parking.model.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {
    List<ParkingSpot> findByHasCharger(boolean hasCharger);

    List<ParkingSpot> findByRow(String row);
}

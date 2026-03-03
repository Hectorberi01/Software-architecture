package com.parking.analytics.repository;

import com.parking.reservation.model.Reservation;
import com.parking.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface AnalyticsRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.startDate <= :today AND r.endDate >= :today AND r.status != 'CANCELLED'")
    long countCurrentlyOccupied(@Param("today") LocalDate today);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.status = :status")
    long countByStatus(@Param("status") ReservationStatus status);

    @Query("SELECT COUNT(DISTINCT r.spot.id) FROM Reservation r WHERE r.spot.hasCharger = true AND r.startDate <= :today AND r.endDate >= :today AND r.status != 'CANCELLED'")
    long countElectricSpotsInUse(@Param("today") LocalDate today);
}

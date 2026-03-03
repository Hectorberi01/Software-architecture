package com.parking.reservation.repository;

import com.parking.reservation.model.Reservation;
import com.parking.reservation.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

        List<Reservation> findByUserId(Long userId);

        List<Reservation> findByStatus(ReservationStatus status);

        // Finds all active reservations that conflict with a spot during a given period
        @Query("SELECT r FROM Reservation r WHERE r.spot.id = :spotId " +
                        "AND r.status NOT IN ('CANCELLED', 'NO_SHOW') " +
                        "AND r.startDate <= :endDate AND r.endDate >= :startDate")
        List<Reservation> findConflictingReservations(
                        @Param("spotId") Long spotId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Finds all APPROVED reservations for a specific date where check-in has not
        // been done
        @Query("SELECT r FROM Reservation r WHERE r.status = 'APPROVED' AND r.startDate = :date")
        List<Reservation> findApprovedReservationsForDate(@Param("date") LocalDate date);

        // For analytics: count by status
        long countByStatus(ReservationStatus status);

        // Historical: all reservations ordered by creation date
        @Query("SELECT r FROM Reservation r ORDER BY r.createdAt DESC")
        List<Reservation> findAllHistory();
}

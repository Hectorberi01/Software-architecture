package com.parking.parking.repository;

import com.parking.parking.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByReservationDate(LocalDate date);

    boolean existsByParkingSpotIdAndReservationDate(Long spotId, LocalDate date);

    @Query("SELECT r.parkingSpot.id FROM Reservation r WHERE r.reservationDate = :date")
    List<Long> findReservedSpotIdsByDate(LocalDate date);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userEmail = :email AND r.reservationDate >= :today")
    long countActiveReservationsByEmail(String email, LocalDate today);

    List<Reservation> findByUserEmailAndReservationDateGreaterThanEqual(String email, LocalDate date);
}

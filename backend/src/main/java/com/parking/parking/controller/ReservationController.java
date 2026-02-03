package com.parking.parking.controller;

import com.parking.parking.dto.CreateReservationRequest;
import com.parking.parking.dto.ReservationDto;
import com.parking.parking.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationDto> createReservation(
            @RequestBody CreateReservationRequest request) {
        ReservationDto reservation = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reserved-spots")
    public ResponseEntity<List<Long>> getReservedSpotIds(
            @RequestParam LocalDate date) {
        List<Long> reservedIds = reservationService.getReservedSpotIdsByDate(date);
        return ResponseEntity.ok(reservedIds);
    }

    @GetMapping
    public ResponseEntity<List<ReservationDto>> getReservationsByDate(
            @RequestParam LocalDate date) {
        List<ReservationDto> reservations = reservationService.getReservationsByDate(date);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ReservationDto>> getUserReservations(
            @RequestParam String email) {
        List<ReservationDto> reservations = reservationService.getActiveReservationsByEmail(email);
        return ResponseEntity.ok(reservations);
    }

    @GetMapping("/user/count")
    public ResponseEntity<Long> getUserReservationCount(
            @RequestParam String email) {
        long count = reservationService.countActiveReservations(email);
        return ResponseEntity.ok(count);
    }
}

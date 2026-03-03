package com.parking.parking.controller;

import com.parking.parking.dto.ParkingSpotDto;
import com.parking.parking.service.ParkingSpotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/parking-spots")
public class ParkingSpotController {

    private final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @GetMapping
    public ResponseEntity<List<ParkingSpotDto>> getAllSpots() {
        return ResponseEntity.ok(parkingSpotService.getAllSpots());
    }

    /**
     * Returns spots available for the given date range.
     * Optional query param: withCharger=true|false for electric vehicle filtering.
     */
    @GetMapping("/available")
    public ResponseEntity<List<ParkingSpotDto>> getAvailableSpots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean withCharger) {
        return ResponseEntity.ok(parkingSpotService.getAvailableSpots(startDate, endDate, withCharger));
    }

    /**
     * QR code data endpoint: returns spot info for display on printed QR codes.
     * Each parking spot has a QR code linked to this endpoint with its code.
     */
    @GetMapping("/{id}/qr")
    public ResponseEntity<ParkingSpotDto> getSpotQrInfo(@PathVariable Long id) {
        return ResponseEntity.ok(parkingSpotService.getSpotById(id));
    }
}

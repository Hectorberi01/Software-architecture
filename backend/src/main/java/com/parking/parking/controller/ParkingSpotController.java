package com.parking.parking.controller;

import com.parking.parking.dto.ParkingSpotDto;
import com.parking.parking.service.ParkingSpotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/spots")
public class ParkingSpotController {

    private final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @GetMapping
    public ResponseEntity<List<ParkingSpotDto>> getAllSpots() {
        List<ParkingSpotDto> spots = parkingSpotService.getAllSpots();
        return ResponseEntity.ok(spots);
    }
}

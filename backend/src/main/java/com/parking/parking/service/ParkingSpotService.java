package com.parking.parking.service;

import com.parking.parking.dto.ParkingSpotDto;
import com.parking.parking.model.ParkingSpot;
import com.parking.parking.repository.ParkingSpotRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingSpotService {

    private final ParkingSpotRepository parkingSpotRepository;
    private final RabbitTemplate rabbitTemplate;

    public ParkingSpotService(ParkingSpotRepository parkingSpotRepository,
                              RabbitTemplate rabbitTemplate) {
        this.parkingSpotRepository = parkingSpotRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<ParkingSpotDto> getAllSpots() {
        List<ParkingSpot> spots = parkingSpotRepository.findAll();

        rabbitTemplate.convertAndSend(
            "parking.notifications",
            "spots.listed",
            "Spots listed: " + spots.size() + " spots returned"
        );

        return spots.stream()
                .map(this::toDto)
                .toList();
    }

    private ParkingSpotDto toDto(ParkingSpot spot) {
        return new ParkingSpotDto(
                spot.getId(),
                spot.getCode(),
                spot.getRow(),
                spot.getNumber(),
                spot.isHasCharger()
        );
    }
}

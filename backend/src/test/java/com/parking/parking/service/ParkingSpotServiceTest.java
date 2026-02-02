package com.parking.parking.service;

import com.parking.parking.dto.ParkingSpotDto;
import com.parking.parking.model.ParkingSpot;
import com.parking.parking.repository.ParkingSpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingSpotServiceTest {

    @Mock
    private ParkingSpotRepository parkingSpotRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ParkingSpotService parkingSpotService;

    private List<ParkingSpot> sampleSpots;

    @BeforeEach
    void setUp() {
        sampleSpots = List.of(
            new ParkingSpot(1L, "A01", "A", 1, true),
            new ParkingSpot(2L, "B05", "B", 5, false),
            new ParkingSpot(3L, "F10", "F", 10, true)
        );
    }

    @Test
    @DisplayName("getAllSpots returns all spots mapped to DTOs")
    void getAllSpots_shouldReturnAllSpotsMappedToDtos() {
        when(parkingSpotRepository.findAll()).thenReturn(sampleSpots);

        List<ParkingSpotDto> result = parkingSpotService.getAllSpots();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).code()).isEqualTo("A01");
        assertThat(result.get(0).hasCharger()).isTrue();
        assertThat(result.get(1).code()).isEqualTo("B05");
        assertThat(result.get(1).hasCharger()).isFalse();
        assertThat(result.get(2).code()).isEqualTo("F10");
        assertThat(result.get(2).row()).isEqualTo("F");
    }

    @Test
    @DisplayName("getAllSpots publishes a message to RabbitMQ")
    void getAllSpots_shouldPublishMessageToRabbitMQ() {
        when(parkingSpotRepository.findAll()).thenReturn(sampleSpots);

        parkingSpotService.getAllSpots();

        verify(rabbitTemplate).convertAndSend(
            eq("parking.notifications"),
            eq("spots.listed"),
            anyString()
        );
    }

    @Test
    @DisplayName("getAllSpots returns empty list when no spots exist")
    void getAllSpots_shouldReturnEmptyListWhenNoSpots() {
        when(parkingSpotRepository.findAll()).thenReturn(List.of());

        List<ParkingSpotDto> result = parkingSpotService.getAllSpots();

        assertThat(result).isEmpty();
    }
}

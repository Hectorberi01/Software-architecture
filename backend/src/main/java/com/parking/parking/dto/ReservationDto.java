package com.parking.parking.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationDto(
    Long id,
    Long parkingSpotId,
    String spotCode,
    Long userId,
    String userEmail,
    LocalDate reservationDate,
    LocalDateTime createdAt
) {}

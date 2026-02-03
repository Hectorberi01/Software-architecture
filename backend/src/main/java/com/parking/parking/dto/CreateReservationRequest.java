package com.parking.parking.dto;

import java.time.LocalDate;

public record CreateReservationRequest(
    Long parkingSpotId,
    String userEmail,
    LocalDate reservationDate
) {}

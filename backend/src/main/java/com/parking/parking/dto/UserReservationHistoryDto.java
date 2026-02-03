package com.parking.parking.dto;

import java.util.List;

public record UserReservationHistoryDto(
    List<ReservationDto> activeReservations,
    List<ReservationDto> pastReservations
) {}

package com.parking.parking.dto;

public record ParkingSpotDto(
    Long id,
    String code,
    String row,
    int number,
    boolean hasCharger
) {}

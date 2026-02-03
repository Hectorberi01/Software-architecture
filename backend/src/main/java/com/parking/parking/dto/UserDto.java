package com.parking.parking.dto;

import java.time.LocalDateTime;

public record UserDto(
    Long id,
    String email,
    String firstName,
    String lastName,
    LocalDateTime createdAt
) {}

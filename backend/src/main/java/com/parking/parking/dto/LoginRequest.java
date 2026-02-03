package com.parking.parking.dto;

public record LoginRequest(
    String email,
    String password
) {}

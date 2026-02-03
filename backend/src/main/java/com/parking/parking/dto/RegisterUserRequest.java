package com.parking.parking.dto;

public record RegisterUserRequest(
    String email,
    String password,
    String firstName,
    String lastName
) {}

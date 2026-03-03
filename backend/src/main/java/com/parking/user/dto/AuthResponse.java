package com.parking.user.dto;

import com.parking.user.model.Role;

public class AuthResponse {

    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    public AuthResponse(String token, String email, String firstName, String lastName, Role role) {
        this.token = token;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Role getRole() {
        return role;
    }
}

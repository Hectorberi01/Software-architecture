package com.parking.user.dto;

import com.parking.user.model.Role;
import com.parking.user.model.User;
import java.time.LocalDateTime;

public class UserAdminDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;

    public static UserAdminDto from(User user) {
        UserAdminDto dto = new UserAdminDto();
        dto.id = user.getId();
        dto.email = user.getEmail();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.role = user.getRole();
        dto.enabled = user.isEnabled();
        dto.createdAt = user.getCreatedAt();
        return dto;
    }

    public Long getId() {
        return id;
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

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

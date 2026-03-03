package com.parking.user.service;

import com.parking.user.dto.CreateUserRequest;
import com.parking.user.dto.UpdateUserRequest;
import com.parking.user.dto.UserAdminDto;
import com.parking.user.model.Role;
import com.parking.user.model.User;
import com.parking.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAdminService userAdminService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@parking.com");
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");
        existingUser.setRole(Role.EMPLOYEE);
        existingUser.setEnabled(true);
    }

    @Test
    void createUser_succeeds_withValidRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("new@parking.com");
        request.setPassword("password123");
        request.setFirstName("New");
        request.setLastName("User");
        request.setRole(Role.MANAGER);

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        UserAdminDto result = userAdminService.createUser(request);

        assertThat(result.getRole()).isEqualTo(Role.MANAGER);
        assertThat(result.getEmail()).isEqualTo("new@parking.com");
    }

    @Test
    void createUser_fails_whenEmailExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("existing@parking.com");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userAdminService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void updateUser_canChangeRole() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setRole(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenReturn(existingUser);

        userAdminService.updateUser(1L, request);

        assertThat(existingUser.getRole()).isEqualTo(Role.ADMIN);
    }
}

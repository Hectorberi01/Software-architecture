package com.parking.parking.service;

import com.parking.parking.dto.LoginRequest;
import com.parking.parking.dto.RegisterUserRequest;
import com.parking.parking.dto.UserDto;
import com.parking.parking.model.User;
import com.parking.parking.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("registerUser saves user and returns DTO")
    void registerUser_shouldPersistAndReturnDto() {
        RegisterUserRequest request = new RegisterUserRequest("test@acme.com", "password", "Jane", "Doe");
        User saved = new User(1L, "test@acme.com", "hash", "Jane", "Doe", LocalDateTime.now());

        when(userRepository.existsByEmailIgnoreCase("test@acme.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDto dto = userService.registerUser(request);

        assertThat(dto.email()).isEqualTo("test@acme.com");
        assertThat(dto.firstName()).isEqualTo("Jane");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser throws when email already used")
    void registerUser_shouldFailWhenDuplicateEmail() {
        RegisterUserRequest request = new RegisterUserRequest("duplicate@acme.com", "password", "Jane", "Doe");
        when(userRepository.existsByEmailIgnoreCase("duplicate@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(request))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("authenticate returns DTO for valid credentials")
    void authenticate_shouldReturnDtoForValidCredentials() {
        LoginRequest request = new LoginRequest("login@acme.com", "secret");
        User user = new User(5L, "login@acme.com", "hash", "John", "Smith", LocalDateTime.now());

        when(userRepository.findByEmailIgnoreCase("login@acme.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);

        UserDto dto = userService.authenticate(request);

        assertThat(dto.id()).isEqualTo(5L);
        verify(passwordEncoder).matches("secret", "hash");
    }

    @Test
    @DisplayName("authenticate throws for invalid credentials")
    void authenticate_shouldThrowForInvalidCredentials() {
        LoginRequest request = new LoginRequest("nouser@acme.com", "secret");
        when(userRepository.findByEmailIgnoreCase("nouser@acme.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticate(request))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

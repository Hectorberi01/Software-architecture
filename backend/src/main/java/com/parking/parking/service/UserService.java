package com.parking.parking.service;

import com.parking.parking.dto.LoginRequest;
import com.parking.parking.dto.RegisterUserRequest;
import com.parking.parking.dto.UserDto;
import com.parking.parking.model.User;
import com.parking.parking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto registerUser(RegisterUserRequest request) {
        validateRegisterRequest(request);

        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalStateException("Un compte existe déjà avec cet email");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    public UserDto authenticate(LoginRequest request) {
        if (request.email() == null || request.email().isBlank() ||
            request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Email et mot de passe sont requis");
        }

        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(() -> new IllegalArgumentException("Identifiants invalides"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Identifiants invalides");
        }

        return toDto(user);
    }

    private void validateRegisterRequest(RegisterUserRequest request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email obligatoire");
        }
        if (request.password() == null || request.password().length() < 6) {
            throw new IllegalArgumentException("Mot de passe minimum 6 caractères");
        }
        if (request.firstName() == null || request.firstName().isBlank()) {
            throw new IllegalArgumentException("Prénom obligatoire");
        }
        if (request.lastName() == null || request.lastName().isBlank()) {
            throw new IllegalArgumentException("Nom obligatoire");
        }
    }

    private UserDto toDto(User user) {
        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getCreatedAt()
        );
    }
}

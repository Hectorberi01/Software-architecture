package com.parking.user.service;

import com.parking.user.dto.CreateUserRequest;
import com.parking.user.dto.UpdateUserRequest;
import com.parking.user.dto.UserAdminDto;
import com.parking.user.model.User;
import com.parking.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserAdminDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserAdminDto::from)
                .collect(Collectors.toList());
    }

    public UserAdminDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return UserAdminDto.from(user);
    }

    public UserAdminDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setEnabled(true);
        return UserAdminDto.from(userRepository.save(user));
    }

    public UserAdminDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (request.getFirstName() != null)
            user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)
            user.setLastName(request.getLastName());
        if (request.getRole() != null)
            user.setRole(request.getRole());
        if (request.getEnabled() != null)
            user.setEnabled(request.getEnabled());

        return UserAdminDto.from(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        userRepository.deleteById(id);
    }
}

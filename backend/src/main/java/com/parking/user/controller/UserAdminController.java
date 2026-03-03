package com.parking.user.controller;

import com.parking.user.dto.CreateUserRequest;
import com.parking.user.dto.UpdateUserRequest;
import com.parking.user.dto.UserAdminDto;
import com.parking.user.service.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public ResponseEntity<List<UserAdminDto>> getAllUsers() {
        return ResponseEntity.ok(userAdminService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAdminDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userAdminService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserAdminDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userAdminService.createUser(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAdminDto> updateUser(@PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userAdminService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userAdminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

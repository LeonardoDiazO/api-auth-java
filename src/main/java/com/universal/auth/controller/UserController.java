package com.universal.auth.controller;

import com.universal.auth.dto.request.AssignRoleRequest;
import com.universal.auth.dto.request.ChangePasswordRequest;
import com.universal.auth.dto.request.CreateUserRequest;
import com.universal.auth.dto.request.UpdateUserRequest;
import com.universal.auth.dto.response.UserResponse;
import com.universal.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User Management
 * Provides endpoints for CRUD operations, role assignment, and password
 * management
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Create a new user
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all users for a specific application
     * GET /api/users?appId=1
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsersByApp(@RequestParam Long appId) {
        List<UserResponse> response = userService.getUsersByApp(appId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update user information
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete user
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assign a role to a user for a specific application
     * POST /api/users/{id}/roles
     */
    @PostMapping("/{id}/roles")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable Long id,
            @Valid @RequestBody AssignRoleRequest request) {
        UserResponse response = userService.assignRole(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove a role from a user for a specific application
     * DELETE /api/users/{userId}/roles/{roleId}/applications/{appId}
     */
    @DeleteMapping("/{userId}/roles/{roleId}/applications/{appId}")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            @PathVariable Long appId) {
        UserResponse response = userService.removeRole(userId, roleId, appId);
        return ResponseEntity.ok(response);
    }

    /**
     * Change user password
     * POST /api/users/{id}/change-password
     */
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Activate user
     * PUT /api/users/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        UserResponse response = userService.activateUser(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate user
     * PUT /api/users/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        UserResponse response = userService.deactivateUser(id);
        return ResponseEntity.ok(response);
    }
}

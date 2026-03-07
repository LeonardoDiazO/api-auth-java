package com.universal.auth.service;

import com.universal.auth.dto.request.AssignRoleRequest;
import com.universal.auth.dto.request.ChangePasswordRequest;
import com.universal.auth.dto.request.CreateUserRequest;
import com.universal.auth.dto.request.UpdateUserRequest;
import com.universal.auth.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    /**
     * Create a new user
     * 
     * @param request User creation request with username, password, and optional
     *                details
     * @return Created user response
     * @throws DuplicateUsernameException if username already exists
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Get user by ID
     * 
     * @param id User ID
     * @return User response with roles
     * @throws UserNotFoundException if user doesn't exist
     */
    UserResponse getUserById(Long id);

    /**
     * Get all users (across all applications)
     */
    List<UserResponse> getAllUsers();

    /**
     * Get all users belonging to a specific application
     */
    List<UserResponse> getUsersByApp(Long appId);

    /**
     * Update user information
     * 
     * @param id      User ID
     * @param request Update request with optional fullName and isActive
     * @return Updated user response
     * @throws UserNotFoundException if user doesn't exist
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Delete user (hard delete with cascade)
     * 
     * @param id User ID
     * @throws UserNotFoundException if user doesn't exist
     */
    void deleteUser(Long id);

    /**
     * Assign a role to a user for a specific application
     * 
     * @param userId  User ID
     * @param request Role assignment request with roleId and appId
     * @return Updated user response
     * @throws UserNotFoundException            if user doesn't exist
     * @throws RoleNotFoundException            if role doesn't exist
     * @throws ApplicationNotFoundException     if application doesn't exist
     * @throws DuplicateRoleAssignmentException if user already has this role for
     *                                          this app
     */
    UserResponse assignRole(Long userId, AssignRoleRequest request);

    /**
     * Remove a role from a user for a specific application
     * 
     * @param userId User ID
     * @param roleId Role ID
     * @param appId  Application ID
     * @return Updated user response
     * @throws UserNotFoundException if user doesn't exist
     */
    UserResponse removeRole(Long userId, Long roleId, Long appId);

    /**
     * Change user password
     * 
     * @param userId  User ID
     * @param request Password change request with current and new password
     * @throws UserNotFoundException    if user doesn't exist
     * @throws InvalidPasswordException if current password is incorrect or new
     *                                  password is invalid
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * Activate user
     * 
     * @param userId User ID
     * @return Updated user response
     * @throws UserNotFoundException if user doesn't exist
     */
    UserResponse activateUser(Long userId);

    /**
     * Deactivate user
     * 
     * @param userId User ID
     * @return Updated user response
     * @throws UserNotFoundException if user doesn't exist
     */
    UserResponse deactivateUser(Long userId);
}

package com.universal.auth.exception;

public class DuplicateRoleAssignmentException extends RuntimeException {
    public DuplicateRoleAssignmentException(Long userId, Long roleId, Long appId) {
        super(String.format("User %d already has role %d for application %d", userId, roleId, appId));
    }
}

package com.universal.auth.exception;

public class PermissionNotFoundException extends RuntimeException {
    public PermissionNotFoundException(Long permissionId) {
        super("Permission not found with ID: " + permissionId);
    }
}

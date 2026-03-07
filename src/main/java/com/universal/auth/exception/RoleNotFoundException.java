package com.universal.auth.exception;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(Long roleId) {
        super("Role not found with ID: " + roleId);
    }
}

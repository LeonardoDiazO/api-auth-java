package com.universal.auth.exception;

public class DuplicatePermissionAssignmentException extends RuntimeException {
    public DuplicatePermissionAssignmentException(Long roleId, Long permissionId) {
        super(String.format("Role %d already has permission %d", roleId, permissionId));
    }
}

package com.universal.auth.exception;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(Long appId) {
        super("Application not found with ID: " + appId);
    }
}

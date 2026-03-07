package com.universal.auth.exception;

public class DuplicateApplicationNameException extends RuntimeException {
    public DuplicateApplicationNameException(String appName) {
        super("Application name already exists: " + appName);
    }
}

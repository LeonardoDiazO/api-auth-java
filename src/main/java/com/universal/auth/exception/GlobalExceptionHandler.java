package com.universal.auth.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(UserNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUserNotFoundException(
                        UserNotFoundException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(DuplicateUsernameException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateUsernameException(
                        DuplicateUsernameException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(InvalidPasswordException.class)
        public ResponseEntity<ErrorResponse> handleInvalidPasswordException(
                        InvalidPasswordException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(RoleNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleRoleNotFoundException(
                        RoleNotFoundException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(ApplicationNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleApplicationNotFoundException(
                        ApplicationNotFoundException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(DuplicateRoleAssignmentException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateRoleAssignmentException(
                        DuplicateRoleAssignmentException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(DuplicatePermissionAssignmentException.class)
        public ResponseEntity<ErrorResponse> handleDuplicatePermissionAssignmentException(
                        DuplicatePermissionAssignmentException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(PermissionNotFoundException.class)
        public ResponseEntity<ErrorResponse> handlePermissionNotFoundException(
                        PermissionNotFoundException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(DuplicateApplicationNameException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateApplicationNameException(
                        DuplicateApplicationNameException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(DuplicateEmailException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateEmailException(
                        DuplicateEmailException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(AccountLockedException.class)
        public ResponseEntity<ErrorResponse> handleAccountLockedException(
                        AccountLockedException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.LOCKED.value(),
                                "Account Locked",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.LOCKED);
        }

        @ExceptionHandler(EmailNotVerifiedException.class)
        public ResponseEntity<ErrorResponse> handleEmailNotVerifiedException(
                        EmailNotVerifiedException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "Email Not Verified",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<ErrorResponse> handleInvalidTokenException(
                        InvalidTokenException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Invalid Token",
                                ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
        public ResponseEntity<ErrorResponse> handleAuthenticationException(
                        RuntimeException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Invalid credentials",
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ErrorResponse> handleResponseStatusException(
                        ResponseStatusException ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                ex.getStatusCode().value(),
                                ex.getStatusCode().toString(),
                                ex.getReason(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, ex.getStatusCode());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {
                String message = ex.getBindingResult().getFieldErrors().stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.joining(", "));

                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Failed",
                                message,
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex, HttpServletRequest request) {
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An unexpected error occurred: " + ex.getMessage(),
                                request.getRequestURI());
                return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}

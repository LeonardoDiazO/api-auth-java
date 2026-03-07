package com.universal.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotNull(message = "Application ID is required")
    private Long appId;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._]{3,50}$", message = "Username can only contain letters, numbers, dots and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be a valid phone number (10-15 digits, optional + prefix)")
    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)")
    private String password;

    @Size(max = 255, message = "Full name cannot exceed 255 characters")
    private String fullName;

    private Boolean isActive = true;
}

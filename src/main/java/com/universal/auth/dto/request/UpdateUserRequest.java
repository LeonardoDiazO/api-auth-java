package com.universal.auth.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email must be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be a valid phone number (10-15 digits, optional + prefix)")
    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @Size(max = 255, message = "Full name cannot exceed 255 characters")
    private String fullName;

    private Boolean isActive;
}

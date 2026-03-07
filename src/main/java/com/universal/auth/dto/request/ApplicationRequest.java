package com.universal.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {

    @NotBlank(message = "Application name is required")
    @Size(max = 255, message = "Application name cannot exceed 255 characters")
    private String appName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}

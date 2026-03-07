package com.universal.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    @NotBlank(message = "Role name is required")
    @Size(max = 100, message = "Role name cannot exceed 100 characters")
    private String roleName;

    @NotNull(message = "Application ID is required")
    private Long appId;
}

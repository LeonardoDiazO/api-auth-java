package com.universal.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleRequest {

    @NotNull(message = "Role ID is required")
    private Long roleId;

    @NotNull(message = "Application ID is required")
    private Long appId;
}

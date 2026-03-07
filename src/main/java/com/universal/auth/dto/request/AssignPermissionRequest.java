package com.universal.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionRequest {

    @NotNull(message = "Permission ID is required")
    private Long permissionId;
}

package com.universal.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDetailResponse {
    private Long roleId;
    private String roleName;
    private Long appId;
    private String appName;

    @Builder.Default
    private List<PermissionResponse> permissions = new ArrayList<>();
}

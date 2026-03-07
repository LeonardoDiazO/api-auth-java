package com.universal.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastPasswordChange;
    private Boolean isEmailVerified;
    private Long appId;
    private String appName;

    @Builder.Default
    private List<RoleResponse> roles = new ArrayList<>();
}

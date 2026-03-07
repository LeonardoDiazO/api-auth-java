package com.universal.auth.service;

import com.universal.auth.dto.request.AssignPermissionRequest;
import com.universal.auth.dto.request.RoleRequest;
import com.universal.auth.dto.response.RoleDetailResponse;

import java.util.List;

public interface RoleService {

    RoleDetailResponse createRole(RoleRequest request);

    RoleDetailResponse getRoleById(Long id);

    List<RoleDetailResponse> getAllRoles();

    List<RoleDetailResponse> getRolesByApplicationId(Long appId);

    RoleDetailResponse updateRole(Long id, RoleRequest request);

    void deleteRole(Long id);

    RoleDetailResponse assignPermission(Long roleId, AssignPermissionRequest request);

    RoleDetailResponse removePermission(Long roleId, Long permissionId);
}

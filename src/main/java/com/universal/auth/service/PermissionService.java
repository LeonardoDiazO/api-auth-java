package com.universal.auth.service;

import com.universal.auth.dto.request.PermissionRequest;
import com.universal.auth.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {

    PermissionResponse createPermission(PermissionRequest request);

    PermissionResponse getPermissionById(Long id);

    List<PermissionResponse> getAllPermissions();

    List<PermissionResponse> getPermissionsByApplicationId(Long appId);

    PermissionResponse updatePermission(Long id, PermissionRequest request);

    void deletePermission(Long id);
}

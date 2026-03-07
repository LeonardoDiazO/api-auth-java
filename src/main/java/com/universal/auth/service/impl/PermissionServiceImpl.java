package com.universal.auth.service.impl;

import com.universal.auth.domain.entities.ApplicationEntity;
import com.universal.auth.domain.entities.Permission;
import com.universal.auth.dto.request.PermissionRequest;
import com.universal.auth.dto.response.PermissionResponse;
import com.universal.auth.exception.ApplicationNotFoundException;
import com.universal.auth.exception.PermissionNotFoundException;
import com.universal.auth.repository.ApplicationRepository;
import com.universal.auth.repository.PermissionRepository;
import com.universal.auth.service.PermissionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        ApplicationEntity application = applicationRepository.findById(request.getAppId())
                .orElseThrow(() -> new ApplicationNotFoundException(request.getAppId()));

        Permission permission = new Permission();
        permission.setPermissionName(request.getPermissionName());
        permission.setApplication(application);

        Permission saved = permissionRepository.save(permission);
        return mapToResponse(saved);
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));
        return mapToResponse(permission);
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionResponse> getPermissionsByApplicationId(Long appId) {
        return permissionRepository.findByApplication_AppId(appId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));

        ApplicationEntity application = applicationRepository.findById(request.getAppId())
                .orElseThrow(() -> new ApplicationNotFoundException(request.getAppId()));

        permission.setPermissionName(request.getPermissionName());
        permission.setApplication(application);

        Permission updated = permissionRepository.save(permission);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));
        permissionRepository.delete(permission);
    }

    private PermissionResponse mapToResponse(Permission permission) {
        return PermissionResponse.builder()
                .permissionId(permission.getPermissionId())
                .permissionName(permission.getPermissionName())
                .appId(permission.getApplication() != null ? permission.getApplication().getAppId() : null)
                .appName(permission.getApplication() != null ? permission.getApplication().getAppName() : null)
                .build();
    }
}

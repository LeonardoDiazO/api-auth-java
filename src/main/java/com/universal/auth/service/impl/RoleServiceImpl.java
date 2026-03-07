package com.universal.auth.service.impl;

import com.universal.auth.domain.entities.ApplicationEntity;
import com.universal.auth.domain.entities.Permission;
import com.universal.auth.domain.entities.Role;
import com.universal.auth.domain.entities.RolePermission;
import com.universal.auth.dto.request.AssignPermissionRequest;
import com.universal.auth.dto.request.RoleRequest;
import com.universal.auth.dto.response.PermissionResponse;
import com.universal.auth.dto.response.RoleDetailResponse;
import com.universal.auth.exception.*;
import com.universal.auth.repository.ApplicationRepository;
import com.universal.auth.repository.PermissionRepository;
import com.universal.auth.repository.RolePermissionRepository;
import com.universal.auth.repository.RoleRepository;
import com.universal.auth.service.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final ApplicationRepository applicationRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    public RoleDetailResponse createRole(RoleRequest request) {
        ApplicationEntity application = applicationRepository.findById(request.getAppId())
                .orElseThrow(() -> new ApplicationNotFoundException(request.getAppId()));

        Role role = new Role();
        role.setRoleName(request.getRoleName());
        role.setApplication(application);

        Role saved = roleRepository.save(role);
        return mapToDetailResponse(saved);
    }

    @Override
    public RoleDetailResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        return mapToDetailResponse(role);
    }

    @Override
    public List<RoleDetailResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleDetailResponse> getRolesByApplicationId(Long appId) {
        return roleRepository.findByApplication_AppId(appId).stream()
                .map(this::mapToDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleDetailResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));

        ApplicationEntity application = applicationRepository.findById(request.getAppId())
                .orElseThrow(() -> new ApplicationNotFoundException(request.getAppId()));

        role.setRoleName(request.getRoleName());
        role.setApplication(application);

        Role updated = roleRepository.save(role);
        return mapToDetailResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleDetailResponse assignPermission(Long roleId, AssignPermissionRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new PermissionNotFoundException(request.getPermissionId()));

        if (rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            throw new DuplicatePermissionAssignmentException(roleId, request.getPermissionId());
        }

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);

        rolePermissionRepository.save(rolePermission);

        return mapToDetailResponse(role);
    }

    @Override
    @Transactional
    public RoleDetailResponse removePermission(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));

        rolePermissionRepository.deleteByRoleAndPermission(role, permission);

        return mapToDetailResponse(role);
    }

    private RoleDetailResponse mapToDetailResponse(Role role) {
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole(role);

        List<PermissionResponse> permissions = rolePermissions.stream()
                .map(rp -> PermissionResponse.builder()
                        .permissionId(rp.getPermission().getPermissionId())
                        .permissionName(rp.getPermission().getPermissionName())
                        .appId(rp.getPermission().getApplication() != null
                                ? rp.getPermission().getApplication().getAppId()
                                : null)
                        .appName(rp.getPermission().getApplication() != null
                                ? rp.getPermission().getApplication().getAppName()
                                : null)
                        .build())
                .collect(Collectors.toList());

        return RoleDetailResponse.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .appId(role.getApplication() != null ? role.getApplication().getAppId() : null)
                .appName(role.getApplication() != null ? role.getApplication().getAppName() : null)
                .permissions(permissions)
                .build();
    }
}

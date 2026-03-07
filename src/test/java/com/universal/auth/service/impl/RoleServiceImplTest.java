package com.universal.auth.service.impl;

import com.universal.auth.domain.entities.*;
import com.universal.auth.dto.request.*;
import com.universal.auth.dto.response.RoleDetailResponse;
import com.universal.auth.exception.*;
import com.universal.auth.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RoleServiceImpl Unit Tests")
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private ApplicationEntity app;
    private Role role;
    private Permission permission;

    @BeforeEach
    void setUp() {
        app = new ApplicationEntity();
        app.setAppId(1L);
        app.setAppName("POS System");

        role = new Role();
        role.setRoleId(1L);
        role.setRoleName("ADMIN");
        role.setApplication(app);

        permission = new Permission();
        permission.setPermissionId(1L);
        permission.setPermissionName("READ");
        permission.setApplication(app);

        when(rolePermissionRepository.findByRole(any(Role.class))).thenReturn(List.of());
    }

    // ─────────── createRole ───────────

    @Test
    @DisplayName("createRole - success")
    void createRole_success() {
        var req = new RoleRequest();
        req.setRoleName("MANAGER");
        req.setAppId(1L);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(roleRepository.save(any(Role.class))).thenAnswer(inv -> {
            Role r = inv.getArgument(0);
            r.setRoleId(2L);
            return r;
        });

        RoleDetailResponse response = roleService.createRole(req);

        assertThat(response).isNotNull();
        assertThat(response.getRoleName()).isEqualTo("MANAGER");
    }

    @Test
    @DisplayName("createRole - app not found: throws ApplicationNotFoundException")
    void createRole_appNotFound() {
        var req = new RoleRequest();
        req.setRoleName("MANAGER");
        req.setAppId(99L);

        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.createRole(req))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    // ─────────── getRoleById ───────────

    @Test
    @DisplayName("getRoleById - not found: throws RoleNotFoundException")
    void getRoleById_notFound() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getRoleById(99L))
                .isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    @DisplayName("getRoleById - success")
    void getRoleById_success() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        RoleDetailResponse response = roleService.getRoleById(1L);

        assertThat(response.getRoleId()).isEqualTo(1L);
        assertThat(response.getRoleName()).isEqualTo("ADMIN");
    }

    // ─────────── updateRole ───────────

    @Test
    @DisplayName("updateRole - success")
    void updateRole_success() {
        var req = new RoleRequest();
        req.setRoleName("SUPER_ADMIN");
        req.setAppId(1L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleDetailResponse response = roleService.updateRole(1L, req);

        assertThat(response).isNotNull();
        verify(roleRepository).save(any(Role.class));
    }

    // ─────────── deleteRole ───────────

    @Test
    @DisplayName("deleteRole - success")
    void deleteRole_success() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        roleService.deleteRole(1L);

        verify(roleRepository).delete(role);
    }

    @Test
    @DisplayName("deleteRole - not found: throws RoleNotFoundException")
    void deleteRole_notFound() {
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.deleteRole(99L))
                .isInstanceOf(RoleNotFoundException.class);
    }

    // ─────────── assignPermission ───────────

    @Test
    @DisplayName("assignPermission - success")
    void assignPermission_success() {
        var req = new AssignPermissionRequest();
        req.setPermissionId(1L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByRoleAndPermission(role, permission)).thenReturn(false);

        RoleDetailResponse response = roleService.assignPermission(1L, req);

        assertThat(response).isNotNull();
        verify(rolePermissionRepository).save(any(RolePermission.class));
    }

    @Test
    @DisplayName("assignPermission - already assigned: throws DuplicatePermissionAssignmentException")
    void assignPermission_duplicate() {
        var req = new AssignPermissionRequest();
        req.setPermissionId(1L);

        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByRoleAndPermission(role, permission)).thenReturn(true);

        assertThatThrownBy(() -> roleService.assignPermission(1L, req))
                .isInstanceOf(DuplicatePermissionAssignmentException.class);
    }

    // ─────────── removePermission ───────────

    @Test
    @DisplayName("removePermission - success")
    void removePermission_success() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));

        RoleDetailResponse response = roleService.removePermission(1L, 1L);

        assertThat(response).isNotNull();
        verify(rolePermissionRepository).deleteByRoleAndPermission(role, permission);
    }
}

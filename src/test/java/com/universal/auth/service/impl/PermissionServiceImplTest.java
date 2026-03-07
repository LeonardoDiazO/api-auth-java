package com.universal.auth.service.impl;

import com.universal.auth.domain.entities.ApplicationEntity;
import com.universal.auth.domain.entities.Permission;
import com.universal.auth.dto.request.PermissionRequest;
import com.universal.auth.dto.response.PermissionResponse;
import com.universal.auth.exception.ApplicationNotFoundException;
import com.universal.auth.exception.PermissionNotFoundException;
import com.universal.auth.repository.ApplicationRepository;
import com.universal.auth.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionServiceImpl Unit Tests")
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private ApplicationEntity app;
    private Permission permission;

    @BeforeEach
    void setUp() {
        app = new ApplicationEntity();
        app.setAppId(1L);
        app.setAppName("POS System");

        permission = new Permission();
        permission.setPermissionId(1L);
        permission.setPermissionName("READ");
        permission.setApplication(app);
    }

    @Test
    @DisplayName("createPermission - success")
    void createPermission_success() {
        var req = new PermissionRequest();
        req.setPermissionName("WRITE");
        req.setAppId(1L);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(permissionRepository.save(any(Permission.class))).thenAnswer(inv -> {
            Permission p = inv.getArgument(0);
            p.setPermissionId(2L);
            return p;
        });

        PermissionResponse response = permissionService.createPermission(req);

        assertThat(response).isNotNull();
        assertThat(response.getPermissionName()).isEqualTo("WRITE");
    }

    @Test
    @DisplayName("createPermission - app not found: throws ApplicationNotFoundException")
    void createPermission_appNotFound() {
        var req = new PermissionRequest();
        req.setPermissionName("WRITE");
        req.setAppId(99L);

        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.createPermission(req))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    @Test
    @DisplayName("getPermissionById - success")
    void getPermissionById_success() {
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));

        PermissionResponse response = permissionService.getPermissionById(1L);

        assertThat(response.getPermissionId()).isEqualTo(1L);
        assertThat(response.getPermissionName()).isEqualTo("READ");
    }

    @Test
    @DisplayName("getPermissionById - not found: throws PermissionNotFoundException")
    void getPermissionById_notFound() {
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.getPermissionById(99L))
                .isInstanceOf(PermissionNotFoundException.class);
    }

    @Test
    @DisplayName("getAllPermissions - returns list")
    void getAllPermissions_returnsList() {
        when(permissionRepository.findAll()).thenReturn(List.of(permission));

        List<PermissionResponse> result = permissionService.getAllPermissions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPermissionName()).isEqualTo("READ");
    }

    @Test
    @DisplayName("getPermissionsByApplicationId - returns filtered list")
    void getPermissionsByApplicationId_returnsList() {
        when(permissionRepository.findByApplication_AppId(1L)).thenReturn(List.of(permission));

        List<PermissionResponse> result = permissionService.getPermissionsByApplicationId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("updatePermission - success")
    void updatePermission_success() {
        var req = new PermissionRequest();
        req.setPermissionName("WRITE");
        req.setAppId(1L);

        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(permissionRepository.save(any(Permission.class))).thenReturn(permission);

        PermissionResponse response = permissionService.updatePermission(1L, req);

        assertThat(response).isNotNull();
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("deletePermission - success")
    void deletePermission_success() {
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(permission));

        permissionService.deletePermission(1L);

        verify(permissionRepository).delete(permission);
    }

    @Test
    @DisplayName("deletePermission - not found: throws PermissionNotFoundException")
    void deletePermission_notFound() {
        when(permissionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.deletePermission(99L))
                .isInstanceOf(PermissionNotFoundException.class);
    }
}

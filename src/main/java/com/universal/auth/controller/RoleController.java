package com.universal.auth.controller;

import com.universal.auth.dto.request.AssignPermissionRequest;
import com.universal.auth.dto.request.RoleRequest;
import com.universal.auth.dto.response.RoleDetailResponse;
import com.universal.auth.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Role Management
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleDetailResponse> createRole(@Valid @RequestBody RoleRequest request) {
        RoleDetailResponse response = roleService.createRole(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDetailResponse> getRoleById(@PathVariable Long id) {
        RoleDetailResponse response = roleService.getRoleById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RoleDetailResponse>> getAllRoles() {
        List<RoleDetailResponse> response = roleService.getAllRoles();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/application/{appId}")
    public ResponseEntity<List<RoleDetailResponse>> getRolesByApplicationId(@PathVariable Long appId) {
        List<RoleDetailResponse> response = roleService.getRolesByApplicationId(appId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDetailResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        RoleDetailResponse response = roleService.updateRole(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/permissions")
    public ResponseEntity<RoleDetailResponse> assignPermission(
            @PathVariable Long id,
            @Valid @RequestBody AssignPermissionRequest request) {
        RoleDetailResponse response = roleService.assignPermission(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleDetailResponse> removePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        RoleDetailResponse response = roleService.removePermission(roleId, permissionId);
        return ResponseEntity.ok(response);
    }
}

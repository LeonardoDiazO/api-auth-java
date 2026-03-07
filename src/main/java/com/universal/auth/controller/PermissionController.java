package com.universal.auth.controller;

import com.universal.auth.dto.request.PermissionRequest;
import com.universal.auth.dto.response.PermissionResponse;
import com.universal.auth.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Permission Management
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<PermissionResponse> createPermission(@Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.createPermission(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable Long id) {
        PermissionResponse response = permissionService.getPermissionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> response = permissionService.getAllPermissions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/application/{appId}")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByApplicationId(@PathVariable Long appId) {
        List<PermissionResponse> response = permissionService.getPermissionsByApplicationId(appId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request) {
        PermissionResponse response = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}

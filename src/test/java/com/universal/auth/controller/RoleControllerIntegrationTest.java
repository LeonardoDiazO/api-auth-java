package com.universal.auth.controller;

import com.universal.auth.dto.request.AssignPermissionRequest;
import com.universal.auth.dto.request.RoleRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("RoleController Integration Tests")
class RoleControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/roles - success: creates role")
    void createRole_success() throws Exception {
        var req = new RoleRequest();
        req.setRoleName("MANAGER");
        req.setAppId(testApp.getAppId());

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleId").isNumber())
                .andExpect(jsonPath("$.roleName").value("MANAGER"));
    }

    @Test
    @DisplayName("POST /api/roles - app not found: returns 404")
    void createRole_appNotFound() throws Exception {
        var req = new RoleRequest();
        req.setRoleName("MANAGER");
        req.setAppId(9999L);

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/roles/{id} - success")
    void getRoleById_success() throws Exception {
        mockMvc.perform(get("/api/roles/{id}", testRole.getRoleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId").value(testRole.getRoleId()))
                .andExpect(jsonPath("$.roleName").value("TEST_ROLE"));
    }

    @Test
    @DisplayName("GET /api/roles/{id} - not found: returns 404")
    void getRoleById_notFound() throws Exception {
        mockMvc.perform(get("/api/roles/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/roles - returns list")
    void getAllRoles_returnsList() throws Exception {
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/roles/application/{appId} - returns roles for app")
    void getRolesByApplicationId_returnsList() throws Exception {
        mockMvc.perform(get("/api/roles/application/{appId}", testApp.getAppId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("PUT /api/roles/{id} - success: updates role")
    void updateRole_success() throws Exception {
        var req = new RoleRequest();
        req.setRoleName("UPDATED_ROLE");
        req.setAppId(testApp.getAppId());

        mockMvc.perform(put("/api/roles/{id}", testRole.getRoleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("UPDATED_ROLE"));
    }

    @Test
    @DisplayName("POST /api/roles/{id}/permissions - success: assigns permission")
    void assignPermission_success() throws Exception {
        var req = new AssignPermissionRequest();
        req.setPermissionId(testPermission.getPermissionId());

        mockMvc.perform(post("/api/roles/{id}/permissions", testRole.getRoleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/roles/{id}/permissions - duplicate: returns 409")
    void assignPermission_duplicate() throws Exception {
        // First assignment
        var req = new AssignPermissionRequest();
        req.setPermissionId(testPermission.getPermissionId());
        mockMvc.perform(post("/api/roles/{id}/permissions", testRole.getRoleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));

        // Second assignment — should conflict
        mockMvc.perform(post("/api/roles/{id}/permissions", testRole.getRoleId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /api/roles/{id} - success: returns 204")
    void deleteRole_success() throws Exception {
        // Create a role with no FK dependencies to safely delete it
        var req = new RoleRequest();
        req.setRoleName("DELETE_ME");
        req.setAppId(testApp.getAppId());

        String resp = mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long roleId = objectMapper.readTree(resp).get("roleId").asLong();

        mockMvc.perform(delete("/api/roles/{id}", roleId))
                .andExpect(status().isNoContent());
    }
}

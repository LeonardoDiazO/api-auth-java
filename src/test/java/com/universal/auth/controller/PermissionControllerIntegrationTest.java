package com.universal.auth.controller;

import com.universal.auth.dto.request.PermissionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("PermissionController Integration Tests")
class PermissionControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/permissions - success: creates permission")
    void createPermission_success() throws Exception {
        var req = new PermissionRequest();
        req.setPermissionName("EXPORT");
        req.setAppId(testApp.getAppId());

        mockMvc.perform(authed(post("/api/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.permissionId").isNumber())
                .andExpect(jsonPath("$.permissionName").value("EXPORT"));
    }

    @Test
    @DisplayName("POST /api/permissions - app not found: returns 404")
    void createPermission_appNotFound() throws Exception {
        var req = new PermissionRequest();
        req.setPermissionName("EXPORT");
        req.setAppId(9999L);

        mockMvc.perform(authed(post("/api/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/permissions/{id} - success")
    void getPermissionById_success() throws Exception {
        mockMvc.perform(authed(get("/api/permissions/{id}", testPermission.getPermissionId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionId").value(testPermission.getPermissionId()))
                .andExpect(jsonPath("$.permissionName").value("TEST_PERM"));
    }

    @Test
    @DisplayName("GET /api/permissions/{id} - not found: returns 404")
    void getPermissionById_notFound() throws Exception {
        mockMvc.perform(authed(get("/api/permissions/{id}", 9999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/permissions - returns list")
    void getAllPermissions_returnsList() throws Exception {
        mockMvc.perform(authed(get("/api/permissions")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/permissions/application/{appId} - returns permissions for app")
    void getPermissionsByApplicationId_returnsList() throws Exception {
        mockMvc.perform(authed(get("/api/permissions/application/{appId}", testApp.getAppId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("PUT /api/permissions/{id} - success: updates permission")
    void updatePermission_success() throws Exception {
        var req = new PermissionRequest();
        req.setPermissionName("IMPORT");
        req.setAppId(testApp.getAppId());

        mockMvc.perform(authed(put("/api/permissions/{id}", testPermission.getPermissionId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissionName").value("IMPORT"));
    }

    @Test
    @DisplayName("DELETE /api/permissions/{id} - success: returns 204")
    void deletePermission_success() throws Exception {
        var req = new PermissionRequest();
        req.setPermissionName("DELETE_ME");
        req.setAppId(testApp.getAppId());

        String resp = mockMvc.perform(authed(post("/api/permissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long permId = objectMapper.readTree(resp).get("permissionId").asLong();

        mockMvc.perform(authed(delete("/api/permissions/{id}", permId)))
                .andExpect(status().isNoContent());
    }
}

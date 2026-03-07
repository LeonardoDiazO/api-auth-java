package com.universal.auth.controller;

import com.universal.auth.dto.request.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/users - success: creates user")
    void createUser_success() throws Exception {
        var req = new CreateUserRequest();
        req.setUsername("newuser");
        req.setEmail("newuser@example.com");
        req.setPassword("Pass123!");
        req.setFullName("New User");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    @DisplayName("POST /api/users - duplicate username: returns 409")
    void createUser_duplicateUsername() throws Exception {
        var req = new CreateUserRequest();
        req.setUsername("testuser"); // already exists from BaseIntegrationTest
        req.setEmail("other@example.com");
        req.setPassword("Pass123!");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/users - duplicate email: returns 409")
    void createUser_duplicateEmail() throws Exception {
        var req = new CreateUserRequest();
        req.setUsername("anotheruser");
        req.setEmail("test@example.com"); // already exists
        req.setPassword("Pass123!");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/users/{id} - success")
    void getUserById_success() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - not found: returns 404")
    void getUserById_notFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users - returns list")
    void getAllUsers_returnsList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - success: updates user")
    void updateUser_success() throws Exception {
        var req = new UpdateUserRequest();
        req.setFullName("Updated Name");
        req.setPhone("+15550001234"); // digits only, no hyphens

        mockMvc.perform(put("/api/users/{id}", testUser.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/users/{id}/roles - success: assigns role")
    void assignRole_success() throws Exception {
        // Create a new role (so it's not already assigned)
        var newRole = new com.universal.auth.domain.entities.Role();
        newRole.setRoleName("NEW_ROLE");
        newRole.setApplication(testApp);
        newRole = roleRepository.save(newRole);

        var req = new AssignRoleRequest();
        req.setRoleId(newRole.getRoleId());
        req.setAppId(testApp.getAppId());

        mockMvc.perform(post("/api/users/{id}/roles", testUser.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/users/{id}/change-password - success")
    void changePassword_success() throws Exception {
        var req = new ChangePasswordRequest();
        req.setCurrentPassword("Test123!");
        req.setNewPassword("NewPass456!");

        mockMvc.perform(post("/api/users/{id}/change-password", testUser.getUserId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/users/{id}/deactivate - success")
    void deactivateUser_success() throws Exception {
        mockMvc.perform(put("/api/users/{id}/deactivate", testUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    @DisplayName("PUT /api/users/{id}/activate - success")
    void activateUser_success() throws Exception {
        mockMvc.perform(put("/api/users/{id}/activate", testUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - success: returns 204")
    void deleteUser_success() throws Exception {
        // Create a throwaway user
        var req = new CreateUserRequest();
        req.setUsername("deleteme");
        req.setEmail("deleteme@example.com");
        req.setPassword("Pass123!");

        String resp = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long userId = objectMapper.readTree(resp).get("userId").asLong();

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());
    }
}

package com.universal.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/auth/login - success: returns JWT")
    void login_success() throws Exception {
        var body = Map.of(
                "username", "testuser",
                "password", "Test123!",
                "appId", testApp.getAppId());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").value(3600));
    }

    @Test
    @DisplayName("POST /api/auth/login - user not found: returns 401")
    void login_userNotFound() throws Exception {
        var body = Map.of(
                "username", "nonexistent",
                "password", "password",
                "appId", testApp.getAppId());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login - wrong password: returns 401")
    void login_wrongPassword() throws Exception {
        var body = Map.of(
                "username", "testuser",
                "password", "wrongpass",
                "appId", testApp.getAppId());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login - invalid appId: returns 400")
    void login_invalidAppId() throws Exception {
        var body = Map.of(
                "username", "testuser",
                "password", "Test123!",
                "appId", 9999L);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}

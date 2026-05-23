package com.universal.auth.controller;

import com.universal.auth.dto.request.ApplicationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ApplicationController Integration Tests")
class ApplicationControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/applications - success: creates application")
    void createApplication_success() throws Exception {
        var req = new ApplicationRequest();
        req.setAppName("Inventory System");
        req.setDescription("Stock management");

        mockMvc.perform(authed(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.appId").isNumber())
                .andExpect(jsonPath("$.appName").value("Inventory System"));
    }

    @Test
    @DisplayName("GET /api/applications/{id} - success")
    void getApplicationById_success() throws Exception {
        mockMvc.perform(authed(get("/api/applications/{id}", testApp.getAppId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appId").value(testApp.getAppId()))
                .andExpect(jsonPath("$.appName").value("Test App"));
    }

    @Test
    @DisplayName("GET /api/applications/{id} - not found: returns 404")
    void getApplicationById_notFound() throws Exception {
        mockMvc.perform(authed(get("/api/applications/{id}", 9999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/applications - returns list")
    void getAllApplications_returnsList() throws Exception {
        mockMvc.perform(authed(get("/api/applications")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("PUT /api/applications/{id} - success: updates application")
    void updateApplication_success() throws Exception {
        var req = new ApplicationRequest();
        req.setAppName("Updated App");
        req.setDescription("Updated description");

        mockMvc.perform(authed(put("/api/applications/{id}", testApp.getAppId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appName").value("Updated App"));
    }

    @Test
    @DisplayName("DELETE /api/applications/{id} - success: returns 204")
    void deleteApplication_success() throws Exception {
        var req = new ApplicationRequest();
        req.setAppName("App To Delete");

        String createResponse = mockMvc.perform(authed(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long appId = objectMapper.readTree(createResponse).get("appId").asLong();

        mockMvc.perform(authed(delete("/api/applications/{id}", appId)))
                .andExpect(status().isNoContent());
    }
}

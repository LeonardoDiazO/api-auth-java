package com.universal.auth.controller;

import com.universal.auth.dto.request.ApplicationRequest;
import com.universal.auth.dto.response.ApplicationResponse;
import com.universal.auth.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Application Management
 */
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(@Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.createApplication(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable Long id) {
        ApplicationResponse response = applicationService.getApplicationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        List<ApplicationResponse> response = applicationService.getAllApplications();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.updateApplication(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}

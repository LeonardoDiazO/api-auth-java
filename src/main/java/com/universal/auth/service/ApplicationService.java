package com.universal.auth.service;

import com.universal.auth.dto.request.ApplicationRequest;
import com.universal.auth.dto.response.ApplicationResponse;

import java.util.List;

public interface ApplicationService {

    ApplicationResponse createApplication(ApplicationRequest request);

    ApplicationResponse getApplicationById(Long id);

    List<ApplicationResponse> getAllApplications();

    ApplicationResponse updateApplication(Long id, ApplicationRequest request);

    void deleteApplication(Long id);
}

package com.universal.auth.service.impl;

import com.universal.auth.domain.entities.ApplicationEntity;
import com.universal.auth.dto.request.ApplicationRequest;
import com.universal.auth.dto.response.ApplicationResponse;
import com.universal.auth.exception.ApplicationNotFoundException;
import com.universal.auth.repository.ApplicationRepository;
import com.universal.auth.service.ApplicationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Override
    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request) {
        ApplicationEntity application = new ApplicationEntity();
        application.setAppName(request.getAppName());
        application.setDescription(request.getDescription());

        ApplicationEntity saved = applicationRepository.save(application);
        return mapToResponse(saved);
    }

    @Override
    public ApplicationResponse getApplicationById(Long id) {
        ApplicationEntity application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
        return mapToResponse(application);
    }

    @Override
    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplication(Long id, ApplicationRequest request) {
        ApplicationEntity application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));

        application.setAppName(request.getAppName());
        application.setDescription(request.getDescription());

        ApplicationEntity updated = applicationRepository.save(application);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteApplication(Long id) {
        ApplicationEntity application = applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
        applicationRepository.delete(application);
    }

    private ApplicationResponse mapToResponse(ApplicationEntity application) {
        return ApplicationResponse.builder()
                .appId(application.getAppId())
                .appName(application.getAppName())
                .description(application.getDescription())
                .build();
    }
}

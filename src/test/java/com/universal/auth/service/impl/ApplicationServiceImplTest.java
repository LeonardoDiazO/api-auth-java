package com.universal.auth.service.impl;

import com.universal.auth.domain.entities.ApplicationEntity;
import com.universal.auth.dto.request.ApplicationRequest;
import com.universal.auth.dto.response.ApplicationResponse;
import com.universal.auth.exception.ApplicationNotFoundException;
import com.universal.auth.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationServiceImpl Unit Tests")
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private ApplicationEntity app;

    @BeforeEach
    void setUp() {
        app = new ApplicationEntity();
        app.setAppId(1L);
        app.setAppName("POS System");
        app.setDescription("Point of Sale application");
    }

    @Test
    @DisplayName("createApplication - success")
    void createApplication_success() {
        var req = new ApplicationRequest();
        req.setAppName("New App");
        req.setDescription("Description");

        when(applicationRepository.save(any(ApplicationEntity.class))).thenAnswer(inv -> {
            ApplicationEntity a = inv.getArgument(0);
            a.setAppId(2L);
            return a;
        });

        ApplicationResponse response = applicationService.createApplication(req);

        assertThat(response).isNotNull();
        assertThat(response.getAppName()).isEqualTo("New App");
        assertThat(response.getAppId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getApplicationById - success")
    void getApplicationById_success() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        ApplicationResponse response = applicationService.getApplicationById(1L);

        assertThat(response.getAppId()).isEqualTo(1L);
        assertThat(response.getAppName()).isEqualTo("POS System");
    }

    @Test
    @DisplayName("getApplicationById - not found: throws ApplicationNotFoundException")
    void getApplicationById_notFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.getApplicationById(99L))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    @Test
    @DisplayName("getAllApplications - returns list")
    void getAllApplications_returnsList() {
        when(applicationRepository.findAll()).thenReturn(List.of(app));

        List<ApplicationResponse> result = applicationService.getAllApplications();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAppName()).isEqualTo("POS System");
    }

    @Test
    @DisplayName("updateApplication - success")
    void updateApplication_success() {
        var req = new ApplicationRequest();
        req.setAppName("Updated App");
        req.setDescription("Updated description");

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any(ApplicationEntity.class))).thenReturn(app);

        ApplicationResponse response = applicationService.updateApplication(1L, req);

        assertThat(response).isNotNull();
        verify(applicationRepository).save(any(ApplicationEntity.class));
    }

    @Test
    @DisplayName("updateApplication - not found: throws ApplicationNotFoundException")
    void updateApplication_notFound() {
        var req = new ApplicationRequest();
        req.setAppName("X");

        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.updateApplication(99L, req))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    @Test
    @DisplayName("deleteApplication - success")
    void deleteApplication_success() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        applicationService.deleteApplication(1L);

        verify(applicationRepository).delete(app);
    }

    @Test
    @DisplayName("deleteApplication - not found: throws ApplicationNotFoundException")
    void deleteApplication_notFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.deleteApplication(99L))
                .isInstanceOf(ApplicationNotFoundException.class);
    }
}

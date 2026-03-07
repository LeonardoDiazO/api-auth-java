package com.universal.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.universal.auth.domain.entities.*;
import com.universal.auth.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Base class for integration tests.
 * Uses H2 in-memory database with test profile.
 * Each test runs in a transaction that is rolled back after the test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected ApplicationRepository applicationRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected PermissionRepository permissionRepository;

    @Autowired
    protected RolePermissionRepository rolePermissionRepository;

    @Autowired
    protected UserRoleRepository userRoleRepository;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    protected ApplicationEntity testApp;
    protected User testUser;
    protected Role testRole;
    protected Permission testPermission;

    @BeforeEach
    void setUpBaseData() {
        // Application
        testApp = new ApplicationEntity();
        testApp.setAppName("Test App");
        testApp.setDescription("Integration test app");
        testApp = applicationRepository.save(testApp);

        // User
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("Test123!"));
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setLastPasswordChange(LocalDateTime.now());
        testUser.setFailedLoginAttempts(0);
        testUser = userRepository.save(testUser);

        // Role
        testRole = new Role();
        testRole.setRoleName("TEST_ROLE");
        testRole.setApplication(testApp);
        testRole = roleRepository.save(testRole);

        // Permission
        testPermission = new Permission();
        testPermission.setPermissionName("TEST_PERM");
        testPermission.setApplication(testApp);
        testPermission = permissionRepository.save(testPermission);

        // Assign role to user
        UserRole userRole = new UserRole();
        userRole.setUser(testUser);
        userRole.setRole(testRole);
        userRole.setApplication(testApp);
        userRoleRepository.save(userRole);
    }
}

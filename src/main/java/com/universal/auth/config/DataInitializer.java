package com.universal.auth.config;

import com.universal.auth.domain.entities.*;
import com.universal.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Initializes database with seed data for development/manual testing.
 * Runs only when NOT in the 'test' profile.
 * Only seeds if the database is empty (idempotent).
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (applicationRepository.count() > 0) {
            log.info("DataInitializer: database already seeded, skipping.");
            return;
        }

        log.info("DataInitializer: seeding initial data...");

        // --- Application ---
        ApplicationEntity app = new ApplicationEntity();
        app.setAppName("POS System");
        app.setDescription("Point of Sale application");
        final ApplicationEntity savedApp = applicationRepository.save(app);

        // --- Permissions ---
        List<String> permNames = List.of("READ", "WRITE", "DELETE", "REPORTS");
        List<Permission> permissions = permNames.stream().map(name -> {
            Permission p = new Permission();
            p.setPermissionName(name);
            p.setApplication(savedApp);
            return permissionRepository.save(p);
        }).toList();

        // --- Roles ---
        Role adminRole = createRole("ADMIN", savedApp);
        Role cashierRole = createRole("CASHIER", savedApp);
        Role supervisorRole = createRole("SUPERVISOR", savedApp);

        // Assign all permissions to ADMIN role
        for (Permission perm : permissions) {
            RolePermission rp = new RolePermission();
            rp.setRole(adminRole);
            rp.setPermission(perm);
            rolePermissionRepository.save(rp);
        }

        // Assign READ + WRITE to CASHIER
        permissions.stream()
                .filter(p -> List.of("READ", "WRITE").contains(p.getPermissionName()))
                .forEach(p -> {
                    RolePermission rp = new RolePermission();
                    rp.setRole(cashierRole);
                    rp.setPermission(p);
                    rolePermissionRepository.save(rp);
                });

        // Assign READ + REPORTS to SUPERVISOR
        permissions.stream()
                .filter(p -> List.of("READ", "REPORTS").contains(p.getPermissionName()))
                .forEach(p -> {
                    RolePermission rp = new RolePermission();
                    rp.setRole(supervisorRole);
                    rp.setPermission(p);
                    rolePermissionRepository.save(rp);
                });

        // --- Admin User ---
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@pos.com");
        admin.setPhone("+1-555-0100");
        admin.setFullName("System Administrator");
        admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
        admin.setIsActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setLastPasswordChange(LocalDateTime.now());
        admin.setFailedLoginAttempts(0);
        admin = userRepository.save(admin);

        // --- Assign admin role to admin user ---
        UserRole userRole = new UserRole();
        userRole.setUser(admin);
        userRole.setRole(adminRole);
        userRole.setApplication(app);
        userRoleRepository.save(userRole);

        log.info("DataInitializer: seeding complete!");
        log.info("  App:  'POS System' (id={})", app.getAppId());
        log.info("  User: admin / Admin123! → role ADMIN");
    }

    private Role createRole(String name, ApplicationEntity app) {
        Role role = new Role();
        role.setRoleName(name);
        role.setApplication(app);
        return roleRepository.save(role);
    }
}

package com.universal.auth.service.impl;

import com.universal.auth.domain.entities.*;
import com.universal.auth.dto.request.*;
import com.universal.auth.dto.response.UserResponse;
import com.universal.auth.exception.*;
import com.universal.auth.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private Role role;
    private ApplicationEntity app;

    @BeforeEach
    void setUp() {
        app = new ApplicationEntity();
        app.setAppId(1L);
        app.setAppName("POS System");

        role = new Role();
        role.setRoleId(1L);
        role.setRoleName("ADMIN");
        role.setApplication(app);

        user = new User();
        user.setUserId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPasswordHash("$2a$10$hashed");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);

        // Default: no roles
        when(userRoleRepository.findByUser(any())).thenReturn(List.of());
    }

    // ─────────── createUser ───────────

    @Test
    @DisplayName("createUser - success")
    void createUser_success() {
        var req = new CreateUserRequest();
        req.setUsername("newuser");
        req.setEmail("new@example.com");
        req.setPassword("password");
        req.setAppId(1L);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(userRepository.existsByUsernameAndApplication_AppId("newuser", 1L)).thenReturn(false);
        when(userRepository.existsByEmailAndApplication_AppId("new@example.com", 1L)).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setUserId(2L);
            return u;
        });

        UserResponse response = userService.createUser(req);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("createUser - duplicate username: throws DuplicateUsernameException")
    void createUser_duplicateUsername() {
        var req = new CreateUserRequest();
        req.setUsername("john");
        req.setEmail("other@example.com");
        req.setPassword("password");
        req.setAppId(1L);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(userRepository.existsByUsernameAndApplication_AppId("john", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(DuplicateUsernameException.class);
    }

    @Test
    @DisplayName("createUser - duplicate email: throws DuplicateEmailException")
    void createUser_duplicateEmail() {
        var req = new CreateUserRequest();
        req.setUsername("newuser");
        req.setEmail("john@example.com");
        req.setPassword("password");
        req.setAppId(1L);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(userRepository.existsByUsernameAndApplication_AppId("newuser", 1L)).thenReturn(false);
        when(userRepository.existsByEmailAndApplication_AppId("john@example.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(DuplicateEmailException.class);
    }

    // ─────────── getUserById ───────────

    @Test
    @DisplayName("getUserById - success")
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("john");
    }

    @Test
    @DisplayName("getUserById - not found: throws UserNotFoundException")
    void getUserById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ─────────── updateUser ───────────

    @Test
    @DisplayName("updateUser - success")
    void updateUser_success() {
        var req = new UpdateUserRequest();
        req.setFullName("John Updated");
        req.setPhone("+1-555-9999");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.updateUser(1L, req);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    // ─────────── deleteUser ───────────

    @Test
    @DisplayName("deleteUser - success")
    void deleteUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    // ─────────── assignRole ───────────

    @Test
    @DisplayName("assignRole - success")
    void assignRole_success() {
        var req = new AssignRoleRequest();
        req.setRoleId(1L);
        req.setAppId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(userRoleRepository.existsByUserAndRoleAndApplication(user, role, app)).thenReturn(false);

        UserResponse response = userService.assignRole(1L, req);

        assertThat(response).isNotNull();
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    @DisplayName("assignRole - already assigned: throws DuplicateRoleAssignmentException")
    void assignRole_duplicate() {
        var req = new AssignRoleRequest();
        req.setRoleId(1L);
        req.setAppId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(userRoleRepository.existsByUserAndRoleAndApplication(user, role, app)).thenReturn(true);

        assertThatThrownBy(() -> userService.assignRole(1L, req))
                .isInstanceOf(DuplicateRoleAssignmentException.class);
    }

    // ─────────── removeRole ───────────

    @Test
    @DisplayName("removeRole - success")
    void removeRole_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));

        userService.removeRole(1L, 1L, 1L);

        verify(userRoleRepository).deleteByUserAndRoleAndApplication(user, role, app);
    }

    // ─────────── changePassword ───────────

    @Test
    @DisplayName("changePassword - success")
    void changePassword_success() {
        var req = new ChangePasswordRequest();
        req.setCurrentPassword("oldpass");
        req.setNewPassword("newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldpass", user.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("$2a$10$newHashed");
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.changePassword(1L, req);

        verify(userRepository).save(argThat(u -> u.getFailedLoginAttempts() == 0));
    }

    @Test
    @DisplayName("changePassword - wrong current password: throws InvalidPasswordException")
    void changePassword_wrongPassword() {
        var req = new ChangePasswordRequest();
        req.setCurrentPassword("wrong");
        req.setNewPassword("newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, req))
                .isInstanceOf(InvalidPasswordException.class);
    }

    // ─────────── activate / deactivate ───────────

    @Test
    @DisplayName("activateUser - success")
    void activateUser_success() {
        user.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.activateUser(1L);

        assertThat(response).isNotNull();
        verify(userRepository).save(argThat(User::getIsActive));
    }

    @Test
    @DisplayName("deactivateUser - success")
    void deactivateUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.deactivateUser(1L);

        assertThat(response).isNotNull();
        verify(userRepository).save(argThat(u -> !u.getIsActive()));
    }
}

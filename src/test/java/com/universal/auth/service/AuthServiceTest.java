package com.universal.auth.service;

import com.universal.auth.domain.entities.ApplicationEntity;
import com.universal.auth.domain.entities.LoginLog;
import com.universal.auth.domain.entities.User;
import com.universal.auth.repository.LoginLogRepository;
import com.universal.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private LoginLogRepository loginLogRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private ApplicationEntity app;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setUserId(1L);
        activeUser.setUsername("testuser");
        activeUser.setEmail("test@example.com");
        activeUser.setPasswordHash("$2a$10$hashedpassword");
        activeUser.setIsActive(true);
        activeUser.setFailedLoginAttempts(0);

        app = new ApplicationEntity();
        app.setAppId(1L);
        app.setAppName("POS System");
    }

    @Test
    @DisplayName("authenticate - success: returns user and logs login")
    void authenticate_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("secret", activeUser.getPasswordHash())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        User result = authService.authenticate("testuser", "secret", app, "127.0.0.1", "JUnit");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getFailedLoginAttempts()).isZero();

        // Verify login log was saved
        ArgumentCaptor<LoginLog> logCaptor = ArgumentCaptor.forClass(LoginLog.class);
        verify(loginLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(logCaptor.getValue().getUserAgent()).isEqualTo("JUnit");
    }

    @Test
    @DisplayName("authenticate - user not found: throws UsernameNotFoundException")
    void authenticate_userNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate("unknown", "pass", app, "127.0.0.1", "JUnit"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(loginLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("authenticate - wrong password: throws BadCredentialsException and increments attempts")
    void authenticate_wrongPassword_incrementsAttempts() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", activeUser.getPasswordHash())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        assertThatThrownBy(() -> authService.authenticate("testuser", "wrong", app, "127.0.0.1", "JUnit"))
                .isInstanceOf(BadCredentialsException.class);

        // failedLoginAttempts should have been incremented to 1
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getFailedLoginAttempts()).isEqualTo(1);
        verify(loginLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("authenticate - inactive user: throws BadCredentialsException")
    void authenticate_inactiveUser() {
        activeUser.setIsActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.authenticate("testuser", "secret", app, "127.0.0.1", "JUnit"))
                .isInstanceOf(BadCredentialsException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(loginLogRepository, never()).save(any());
    }
}

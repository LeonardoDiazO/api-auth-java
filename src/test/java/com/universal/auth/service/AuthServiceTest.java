package com.universal.auth.service;

import com.universal.auth.config.AppProperties;
import com.universal.auth.domain.entities.ApplicationEntity;
import com.universal.auth.domain.entities.LoginLog;
import com.universal.auth.domain.entities.User;
import com.universal.auth.dto.response.AuthResponse;
import com.universal.auth.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private LoginLogRepository loginLogRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private SocialAccountRepository socialAccountRepository;
    @Mock private EmailVerificationRepository emailVerificationRepository;
    @Mock private PasswordResetRepository passwordResetRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenGenerator jwtTokenGenerator;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private GoogleTokenVerifier googleTokenVerifier;
    @Mock private AppProperties appProperties;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private ApplicationEntity app;
    private AppProperties.Jwt jwtProps;
    private AppProperties.Lockout lockoutProps;

    @BeforeEach
    void setUp() {
        app = new ApplicationEntity();
        app.setAppId(1L);
        app.setAppName("POS System");

        activeUser = new User();
        activeUser.setUserId(1L);
        activeUser.setUsername("testuser");
        activeUser.setEmail("test@example.com");
        activeUser.setPasswordHash("$2a$10$hashedpassword");
        activeUser.setIsActive(true);
        activeUser.setIsEmailVerified(true);
        activeUser.setFailedLoginAttempts(0);
        activeUser.setApplication(app);

        jwtProps = new AppProperties.Jwt();
        lockoutProps = new AppProperties.Lockout();

        when(appProperties.getJwt()).thenReturn(jwtProps);
        when(appProperties.getLockout()).thenReturn(lockoutProps);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(app));
        when(jwtTokenGenerator.generateToken(any(User.class), anyLong())).thenReturn("access-token");
        when(refreshTokenService.create(any(User.class), anyLong())).thenReturn("refresh-token");
    }

    @Test
    @DisplayName("authenticate - success: returns AuthResponse and logs login")
    void authenticate_success() {
        when(userRepository.findByUsernameAndApplication_AppId("testuser", 1L))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("secret", activeUser.getPasswordHash())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        AuthResponse result = authService.authenticate("testuser", "secret", 1L, "127.0.0.1", "JUnit");

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<LoginLog> logCaptor = ArgumentCaptor.forClass(LoginLog.class);
        verify(loginLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(logCaptor.getValue().getUserAgent()).isEqualTo("JUnit");
    }

    @Test
    @DisplayName("authenticate - user not found: throws BadCredentialsException")
    void authenticate_userNotFound() {
        when(userRepository.findByUsernameAndApplication_AppId("unknown", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate("unknown", "pass", 1L, "127.0.0.1", "JUnit"))
                .isInstanceOf(BadCredentialsException.class);

        verify(loginLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("authenticate - wrong password: throws BadCredentialsException and increments attempts")
    void authenticate_wrongPassword_incrementsAttempts() {
        when(userRepository.findByUsernameAndApplication_AppId("testuser", 1L))
                .thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", activeUser.getPasswordHash())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        assertThatThrownBy(() -> authService.authenticate("testuser", "wrong", 1L, "127.0.0.1", "JUnit"))
                .isInstanceOf(BadCredentialsException.class);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getFailedLoginAttempts()).isEqualTo(1);
        verify(loginLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("authenticate - inactive user: throws BadCredentialsException")
    void authenticate_inactiveUser() {
        activeUser.setIsActive(false);
        when(userRepository.findByUsernameAndApplication_AppId("testuser", 1L))
                .thenReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.authenticate("testuser", "secret", 1L, "127.0.0.1", "JUnit"))
                .isInstanceOf(BadCredentialsException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(loginLogRepository, never()).save(any());
    }
}

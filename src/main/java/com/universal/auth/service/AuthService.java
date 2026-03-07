package com.universal.auth.service;

import com.universal.auth.config.AppProperties;
import com.universal.auth.domain.entities.*;
import com.universal.auth.dto.response.AuthResponse;
import com.universal.auth.exception.*;
import com.universal.auth.exception.UserNotFoundException;
import com.universal.auth.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final ApplicationRepository applicationRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final RefreshTokenService refreshTokenService;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final AppProperties appProperties;

    // ──────────────────────────────────────────────
    // Username + Password login
    // ──────────────────────────────────────────────

    @Transactional
    public AuthResponse authenticate(String username, String password, Long appId,
                                     String ip, String userAgent) {
        User user = userRepository.findByUsernameAndApplication_AppId(username, appId)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Account is disabled");
        }

        checkLockout(user);

        if (!Boolean.TRUE.equals(user.getIsEmailVerified())) {
            throw new EmailNotVerifiedException();
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            registerFailedAttempt(user);
            throw new BadCredentialsException("Invalid credentials");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        saveLoginLog(user, appId, ip, userAgent);
        return buildAuthResponse(user, appId);
    }

    // ──────────────────────────────────────────────
    // Google OAuth2 login (Option B: token exchange)
    // ──────────────────────────────────────────────

    @Transactional
    public AuthResponse googleLogin(String idToken, Long appId, String ip, String userAgent) {
        GoogleTokenVerifier.GoogleUserInfo googleUser = googleTokenVerifier.verify(idToken);

        ApplicationEntity app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));

        Optional<SocialAccount> existingSocial =
                socialAccountRepository.findByProviderAndProviderUserId("google", googleUser.googleUserId());

        User user;
        if (existingSocial.isPresent()) {
            user = existingSocial.get().getUser();
        } else {
            // Find existing user by email in this app, or create a new one
            user = userRepository.findByEmailAndApplication_AppId(googleUser.email(), appId)
                    .orElseGet(() -> createGoogleUser(googleUser, app));

            linkSocialAccount(user, googleUser);
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Account is disabled");
        }

        saveLoginLog(user, appId, ip, userAgent);
        return buildAuthResponse(user, appId);
    }

    // ──────────────────────────────────────────────
    // Token refresh (sliding window)
    // ──────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(String rawRefreshToken, Long appId) {
        RefreshToken old = refreshTokenService.validateAndRevoke(rawRefreshToken, appId);
        User user = old.getUser();

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Account is disabled");
        }

        return buildAuthResponse(user, appId);
    }

    // ──────────────────────────────────────────────
    // Logout
    // ──────────────────────────────────────────────

    @Transactional
    public void logout(String rawRefreshToken, Long appId) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            try {
                refreshTokenService.validateAndRevoke(rawRefreshToken, appId);
            } catch (InvalidTokenException ignored) {
                // Already revoked or expired — logout proceeds anyway
            }
        }
    }

    // ──────────────────────────────────────────────
    // Email verification
    // ──────────────────────────────────────────────

    @Transactional
    public void verifyEmail(String token) {
        EmailVerification verification = emailVerificationRepository.findByTokenAndIsUsedFalse(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or already used verification link"));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification link has expired — please request a new one");
        }

        verification.setIsUsed(true);
        emailVerificationRepository.save(verification);

        User user = verification.getUser();
        user.setIsEmailVerified(true);
        userRepository.save(user);
    }

    // ──────────────────────────────────────────────
    // Forgot password
    // ──────────────────────────────────────────────

    @Transactional
    public void initPasswordReset(String email, Long appId, EmailService emailService) {
        // Intentionally opaque: always returns 200 to avoid email enumeration
        userRepository.findByEmailAndApplication_AppId(email, appId).ifPresent(user -> {
            if (Boolean.TRUE.equals(user.getIsEmailVerified())) {
                emailService.sendPasswordResetEmail(user);
            }
        });
    }

    // ──────────────────────────────────────────────
    // Reset password
    // ──────────────────────────────────────────────

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordReset reset = passwordResetRepository.findByResetTokenAndIsUsedFalse(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or already used reset link"));

        if (reset.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Reset link has expired — please request a new one");
        }

        reset.setIsUsed(true);
        passwordResetRepository.save(reset);

        User user = reset.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setLastPasswordChange(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    // ──────────────────────────────────────────────
    // Send verification email for newly registered user
    // ──────────────────────────────────────────────

    public void sendVerificationEmailForNewUser(Long userId, EmailService emailService) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        emailService.sendVerificationEmail(user);
    }

    // ──────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, Long appId) {
        String accessToken = jwtTokenGenerator.generateToken(user, appId);
        String refreshToken = refreshTokenService.create(user, appId);
        long expiresIn = appProperties.getJwt().getAccessTokenTtlMinutes() * 60;
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }

    private void checkLockout(User user) {
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new AccountLockedException(user.getLockedUntil());
        }
    }

    private void registerFailedAttempt(User user) {
        int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;

        if (attempts >= appProperties.getLockout().getMaxAttempts()) {
            user.setLockedUntil(LocalDateTime.now()
                    .plusMinutes(appProperties.getLockout().getDurationMinutes()));
            user.setFailedLoginAttempts(0);
        } else {
            user.setFailedLoginAttempts(attempts);
        }
        userRepository.save(user);
    }

    private User createGoogleUser(GoogleTokenVerifier.GoogleUserInfo googleUser, ApplicationEntity app) {
        String username = deriveUniqueUsername(googleUser.email(), app.getAppId());

        User user = new User();
        user.setUsername(username);
        user.setEmail(googleUser.email());
        user.setFullName(googleUser.name());
        user.setIsActive(true);
        user.setIsEmailVerified(true); // Google already verified the email
        user.setCreatedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setApplication(app);
        return userRepository.save(user);
    }

    private void linkSocialAccount(User user, GoogleTokenVerifier.GoogleUserInfo googleUser) {
        SocialAccount social = new SocialAccount();
        social.setUser(user);
        social.setProvider("google");
        social.setProviderUserId(googleUser.googleUserId());
        social.setEmail(googleUser.email());
        social.setCreatedAt(LocalDateTime.now());
        socialAccountRepository.save(social);
    }

    private String deriveUniqueUsername(String email, Long appId) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9._]", "_");
        String candidate = base;
        int counter = 1;
        while (userRepository.existsByUsernameAndApplication_AppId(candidate, appId)) {
            candidate = base + counter++;
        }
        return candidate;
    }

    private void saveLoginLog(User user, Long appId, String ip, String userAgent) {
        applicationRepository.findById(appId).ifPresent(app -> {
            LoginLog log = new LoginLog();
            log.setUser(user);
            log.setApplication(app);
            log.setLoginTime(LocalDateTime.now());
            log.setIpAddress(ip);
            log.setUserAgent(userAgent);
            loginLogRepository.save(log);
        });
    }
}

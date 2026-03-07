package com.universal.auth.controller;

import com.universal.auth.dto.request.CreateUserRequest;
import com.universal.auth.dto.response.AuthResponse;
import com.universal.auth.dto.response.UserResponse;
import com.universal.auth.service.AuthService;
import com.universal.auth.service.EmailService;
import com.universal.auth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final EmailService emailService;

    // ── Request records ──────────────────────────────────────────────────────

    record LoginRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotNull Long appId
    ) {}

    record GoogleLoginRequest(
            @NotBlank String idToken,
            @NotNull Long appId
    ) {}

    record RefreshRequest(
            @NotBlank String refreshToken,
            @NotNull Long appId
    ) {}

    record LogoutRequest(
            String refreshToken,
            @NotNull Long appId
    ) {}

    record ForgotPasswordRequest(
            @NotBlank String email,
            @NotNull Long appId
    ) {}

    record ResetPasswordRequest(
            @NotBlank String token,
            @NotBlank
            @Size(min = 8)
            @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                    message = "Password must contain uppercase, lowercase, digit and special character")
            String newPassword
    ) {}

    // ── Endpoints ────────────────────────────────────────────────────────────

    /**
     * POST /api/auth/login
     * Standard username + password login. Returns access + refresh tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                               HttpServletRequest servlet) {
        AuthResponse response = authService.authenticate(
                req.username(), req.password(), req.appId(),
                servlet.getRemoteAddr(), servlet.getHeader("User-Agent")
        );
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/google
     * Option B: client (Flutter/Angular) obtains idToken from Google SDK,
     * then exchanges it here for an app JWT + refresh token.
     * Body: { "idToken": "...", "appId": 1 }
     */
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest req,
                                                     HttpServletRequest servlet) {
        AuthResponse response = authService.googleLogin(
                req.idToken(), req.appId(),
                servlet.getRemoteAddr(), servlet.getHeader("User-Agent")
        );
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/refresh
     * Sliding window refresh: exchanges a valid refresh token for a new token pair.
     * If 60 minutes pass without a refresh call, the refresh token expires.
     * Body: { "refreshToken": "...", "appId": 1 }
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req.refreshToken(), req.appId()));
    }

    /**
     * POST /api/auth/logout
     * Revokes the refresh token. The access token expires naturally (15 min).
     * Body: { "refreshToken": "...", "appId": 1 }
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest req) {
        authService.logout(req.refreshToken(), req.appId());
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/auth/register
     * Self-registration. Creates the user and sends a verification email.
     * Login is blocked until the email is verified.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody CreateUserRequest req) {
        UserResponse user = userService.createUser(req);
        // Fetch the full entity to pass to email service (service returns DTO, so we use a proxy)
        // EmailService receives the User entity via a separate lookup inside AuthService
        authService.sendVerificationEmailForNewUser(user.getUserId(), emailService);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful. Please check your email to verify your account."));
    }

    /**
     * GET /api/auth/verify-email?token=...
     * Verifies the user's email address. After this, the user can log in.
     */
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully. You can now log in."));
    }

    /**
     * POST /api/auth/forgot-password
     * Sends a password reset link. Always returns 200 to prevent email enumeration.
     * Body: { "email": "...", "appId": 1 }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.initPasswordReset(req.email(), req.appId(), emailService);
        return ResponseEntity.ok(Map.of("message", "If that email exists, a reset link has been sent."));
    }

    /**
     * POST /api/auth/reset-password
     * Resets the password using the token received by email.
     * Body: { "token": "...", "newPassword": "..." }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now log in."));
    }
}

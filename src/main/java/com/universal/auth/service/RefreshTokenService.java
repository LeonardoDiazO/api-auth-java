package com.universal.auth.service;

import com.universal.auth.config.AppProperties;
import com.universal.auth.domain.entities.RefreshToken;
import com.universal.auth.domain.entities.User;
import com.universal.auth.exception.InvalidTokenException;
import com.universal.auth.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;

    /**
     * Creates a new refresh token for the user+app pair.
     * Revokes any existing active tokens first (single active token per user/app).
     *
     * @return the raw token value to send to the client (never stored in plain text)
     */
    @Transactional
    public String create(User user, Long appId) {
        refreshTokenRepository.revokeAllByUserIdAndAppId(user.getUserId(), appId);

        String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();
        String tokenHash = hash(rawToken);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setAppId(appId);
        token.setTokenHash(tokenHash);
        token.setExpiresAt(LocalDateTime.now()
                .plusMinutes(appProperties.getJwt().getRefreshTokenTtlMinutes()));
        token.setRevoked(false);
        token.setCreatedAt(LocalDateTime.now());

        refreshTokenRepository.save(token);
        return rawToken;
    }

    /**
     * Validates the token and revokes it (rotation: caller must issue a new one).
     *
     * @return the validated RefreshToken entity (contains user and appId)
     */
    @Transactional
    public RefreshToken validateAndRevoke(String rawToken, Long appId) {
        String tokenHash = hash(rawToken);

        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (Boolean.TRUE.equals(token.getRevoked())) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token has expired — please log in again");
        }
        if (!token.getAppId().equals(appId)) {
            throw new InvalidTokenException("Refresh token does not belong to this application");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);
        return token;
    }

    @Transactional
    public void revokeAll(Long userId, Long appId) {
        refreshTokenRepository.revokeAllByUserIdAndAppId(userId, appId);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

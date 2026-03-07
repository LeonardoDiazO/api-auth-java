package com.universal.auth.service;

import com.universal.auth.config.AppProperties;
import com.universal.auth.exception.InvalidTokenException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Validates a Google idToken obtained by the client (Option B: token exchange).
 * Uses Google's JWKS endpoint to verify the JWT signature without extra dependencies.
 */
@Service
@RequiredArgsConstructor
public class GoogleTokenVerifier {

    private static final String GOOGLE_JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String GOOGLE_ISSUER_1 = "https://accounts.google.com";
    private static final String GOOGLE_ISSUER_2 = "accounts.google.com";

    private final AppProperties appProperties;

    private JwtDecoder googleDecoder;

    @PostConstruct
    public void init() {
        this.googleDecoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS_URI).build();
    }

    public GoogleUserInfo verify(String idToken) {
        try {
            Jwt jwt = googleDecoder.decode(idToken);

            // Validate audience must be our Google client ID
            List<String> audience = jwt.getAudience();
            String clientId = appProperties.getGoogle().getClientId();
            if (clientId == null || clientId.isBlank()) {
                throw new InvalidTokenException("Google client ID is not configured");
            }
            if (!audience.contains(clientId)) {
                throw new InvalidTokenException("Google token audience does not match configured client ID");
            }

            // Validate issuer
            String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
            if (!GOOGLE_ISSUER_1.equals(issuer) && !GOOGLE_ISSUER_2.equals(issuer)) {
                throw new InvalidTokenException("Google token issuer is invalid");
            }

            String googleUserId = jwt.getSubject();
            String email = jwt.getClaimAsString("email");
            String name = jwt.getClaimAsString("name");
            Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");

            if (googleUserId == null || email == null) {
                throw new InvalidTokenException("Google token is missing required claims (sub, email)");
            }

            return new GoogleUserInfo(
                    googleUserId,
                    email,
                    name != null ? name : email.split("@")[0],
                    Boolean.TRUE.equals(emailVerified)
            );

        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid or expired Google token: " + e.getMessage());
        }
    }

    public record GoogleUserInfo(
            String googleUserId,
            String email,
            String name,
            boolean emailVerified
    ) {}
}

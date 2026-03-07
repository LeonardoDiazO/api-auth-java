package com.universal.auth.service;

import com.universal.auth.config.AppProperties;
import com.universal.auth.domain.entities.ApplicationEntity;
import com.universal.auth.domain.entities.User;
import com.universal.auth.domain.entities.UserRole;
import com.universal.auth.repository.ApplicationRepository;
import com.universal.auth.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtTokenGenerator {

    private final JwtEncoder jwtEncoder;
    private final UserRoleRepository userRoleRepository;
    private final ApplicationRepository applicationRepository;
    private final AppProperties appProperties;

    /**
     * Generates a short-lived access token (default: 15 minutes).
     * Contains user identity, roles and permissions for the given application.
     */
    public String generateToken(User user, Long appId) {
        Instant now = Instant.now();
        long ttlMinutes = appProperties.getJwt().getAccessTokenTtlMinutes();

        ApplicationEntity application = applicationRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + appId));

        List<UserRole> userRoles = userRoleRepository.findByUser(user).stream()
                .filter(ur -> ur.getApplication().getAppId().equals(appId))
                .collect(Collectors.toList());

        List<String> roles = userRoles.stream()
                .map(ur -> ur.getRole().getRoleName())
                .distinct()
                .collect(Collectors.toList());

        List<String> permissions = userRoles.stream()
                .flatMap(ur -> ur.getRole().getRolePermissions().stream())
                .map(rp -> rp.getPermission().getPermissionName())
                .distinct()
                .collect(Collectors.toList());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(appProperties.getJwt().getIssuer())
                .issuedAt(now)
                .expiresAt(now.plus(ttlMinutes, ChronoUnit.MINUTES))
                .subject(user.getEmail())
                .claim("userId", user.getUserId())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("appId", appId)
                .claim("appName", application.getAppName())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .audience(List.of(application.getAppName()))
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

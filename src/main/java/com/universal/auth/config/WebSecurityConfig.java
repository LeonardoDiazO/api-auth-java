package com.universal.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Main API security filter chain.
     * Public endpoints: all /api/auth/** routes + health.
     * Protected endpoints: everything else requires a valid Bearer JWT.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        http
            .securityMatcher("/api/**", "/actuator/**")
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/google",
                    "/api/auth/refresh",
                    "/api/auth/logout",
                    "/api/auth/register",
                    "/api/auth/verify-email",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/actuator/health"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.decoder(jwtDecoder))
            );

        return http.build();
    }
}

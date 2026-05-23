package com.universal.auth.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.UUID;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthorizationServerConfig {

    private final Jwks jwks;
    private final AppProperties appProperties;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * Authorization server filter chain — handles /oauth2/jwks endpoint only.
     * No OAuth2 grant flows are registered; we use our own /api/auth/* endpoints.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .build();
    }

    /**
     * Spring Authorization Server requires at least one registered client.
     * This app uses its own /api/auth/* endpoints; the auth server is only needed
     * to expose /oauth2/jwks for resource servers to validate JWTs.
     * The placeholder client below satisfies the requirement without enabling OAuth2 flows.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient placeholder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("internal-placeholder")
                .clientSecret("{noop}placeholder")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
        return new InMemoryRegisteredClientRepository(placeholder);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = jwks.generateRsa();
        JWKSet set = new JWKSet(rsaKey);
        return (j, sc) -> j.select(set);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(appProperties.getJwt().getIssuer())
                .build();
    }
}

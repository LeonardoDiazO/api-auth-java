package com.universal.auth.config;

import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Jwks {

    private final JwksPersistence jwksPersistence;

    public RSAKey generateRsa() {
        return jwksPersistence.loadOrGenerateRsaKey();
    }
}

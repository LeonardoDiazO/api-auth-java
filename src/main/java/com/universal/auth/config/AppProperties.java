package com.universal.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Lockout lockout = new Lockout();
    private Google google = new Google();
    private Mail mail = new Mail();

    @Getter
    @Setter
    public static class Jwt {
        private String issuer = "http://localhost:8081";
        private long accessTokenTtlMinutes = 15;
        private long refreshTokenTtlMinutes = 60;
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of(
                "http://localhost:4200",
                "http://localhost:3000",
                "http://localhost:8080"
        );
    }

    @Getter
    @Setter
    public static class Lockout {
        private int maxAttempts = 3;
        private long durationMinutes = 30;
    }

    @Getter
    @Setter
    public static class Google {
        private String clientId = "";
    }

    @Getter
    @Setter
    public static class Mail {
        private String from = "noreply@yourdomain.com";
        private String verificationBaseUrl = "http://localhost:8081/api/auth/verify-email";
        private String resetBaseUrl = "http://localhost:3000/reset-password";
    }
}

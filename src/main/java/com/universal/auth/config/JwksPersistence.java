package com.universal.auth.config;

import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Set;

/**
 * Loads or generates RSA key pair for JWT signing.
 *
 * Key loading priority:
 *  1. Environment variables JWT_PRIVATE_KEY_BASE64 + JWT_PUBLIC_KEY_BASE64
 *     Use in cloud/containers where filesystem is ephemeral. Store values in a
 *     secret manager (AWS Secrets Manager, GCP Secret Manager, K8s Secrets).
 *  2. File-based storage at jwt.keys.directory (default: ~/.auth-service/keys)
 *     Use in local development or when a persistent volume is mounted.
 *
 * Generate a base64 key pair:
 *   openssl genrsa -out private.pem 2048
 *   openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in private.pem -out private.der
 *   openssl rsa -in private.pem -pubout -outform DER -out public.der
 *   JWT_PRIVATE_KEY_BASE64=$(base64 -i private.der)
 *   JWT_PUBLIC_KEY_BASE64=$(base64 -i public.der)
 */
@Component
@Slf4j
public class JwksPersistence {

    @Value("${jwt.keys.directory:${user.home}/.auth-service/keys}")
    private String keysDirectory;

    @Value("${JWT_PRIVATE_KEY_BASE64:}")
    private String privateKeyBase64;

    @Value("${JWT_PUBLIC_KEY_BASE64:}")
    private String publicKeyBase64;

    private static final String PUBLIC_KEY_FILE = "public.key";
    private static final String PRIVATE_KEY_FILE = "private.key";

    public RSAKey loadOrGenerateRsaKey() {
        try {
            // Priority 1: load from environment variables (cloud/container deployments)
            if (!privateKeyBase64.isBlank() && !publicKeyBase64.isBlank()) {
                log.info("Loading RSA keys from environment variables");
                return loadFromEnv();
            }
            // Priority 2: load from or generate in filesystem (local dev / persistent volume)
            return loadOrGenerateFromFilesystem();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load or generate RSA keys", e);
        }
    }

    private RSAKey loadFromEnv() throws Exception {
        byte[] privateBytes = Base64.getDecoder().decode(privateKeyBase64.strip());
        byte[] publicBytes = Base64.getDecoder().decode(publicKeyBase64.strip());

        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(privateBytes));
        PublicKey publicKey = kf.generatePublic(new java.security.spec.X509EncodedKeySpec(publicBytes));

        return new RSAKey.Builder((RSAPublicKey) publicKey)
                .privateKey((RSAPrivateKey) privateKey)
                .keyID("auth-service-rsa-key")
                .build();
    }

    private RSAKey loadOrGenerateFromFilesystem() throws Exception {
        File keyDir = new File(keysDirectory);
        if (!keyDir.exists()) {
            keyDir.mkdirs();
        }

        File publicKeyFile = new File(keyDir, PUBLIC_KEY_FILE);
        File privateKeyFile = new File(keyDir, PRIVATE_KEY_FILE);

        if (publicKeyFile.exists() && privateKeyFile.exists()) {
            log.info("Loading RSA keys from {}", keysDirectory);
            PublicKey publicKey = loadPublicKey(publicKeyFile);
            PrivateKey privateKey = loadPrivateKey(privateKeyFile);

            return new RSAKey.Builder((RSAPublicKey) publicKey)
                    .privateKey((RSAPrivateKey) privateKey)
                    .keyID("auth-service-rsa-key")
                    .build();
        } else {
            log.info("Generating new RSA key pair in {}", keysDirectory);
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            saveKey(publicKeyFile, keyPair.getPublic());
            saveKey(privateKeyFile, keyPair.getPrivate());

            return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyID("auth-service-rsa-key")
                    .build();
        }
    }

    private void saveKey(File file, java.security.Key key) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
            String encoded = Base64.getEncoder().encodeToString(key.getEncoded());
            writer.write(encoded);
        }
        if (file.getName().equals(PRIVATE_KEY_FILE)) {
            restrictPrivateKeyPermissions(file);
        }
    }

    private void restrictPrivateKeyPermissions(File file) {
        try {
            Files.setPosixFilePermissions(file.toPath(),
                    PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException e) {
            // Windows: approximate POSIX rw------- with Java's ACL API
            file.setReadable(false, false);
            file.setReadable(true, true);
            file.setWritable(false, false);
            file.setWritable(true, true);
            file.setExecutable(false, false);
        } catch (IOException e) {
            log.warn("Could not restrict permissions on {}: {}", file.getPath(), e.getMessage());
        }
    }

    private PublicKey loadPublicKey(File file) throws Exception {
        String keyString = readKeyFile(file);
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(keyBytes);
        java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    private PrivateKey loadPrivateKey(File file) throws Exception {
        String keyString = readKeyFile(file);
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
        java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    private String readKeyFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            return reader.readLine();
        }
    }
}

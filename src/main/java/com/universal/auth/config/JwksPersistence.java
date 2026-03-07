package com.universal.auth.config;

import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@Component
public class JwksPersistence {

    @Value("${jwt.keys.directory:${user.home}/.auth-service/keys}")
    private String keysDirectory;

    private static final String PUBLIC_KEY_FILE = "public.key";
    private static final String PRIVATE_KEY_FILE = "private.key";

    public RSAKey loadOrGenerateRsaKey() {
        try {
            File keyDir = new File(keysDirectory);
            if (!keyDir.exists()) {
                keyDir.mkdirs();
            }

            File publicKeyFile = new File(keyDir, PUBLIC_KEY_FILE);
            File privateKeyFile = new File(keyDir, PRIVATE_KEY_FILE);

            if (publicKeyFile.exists() && privateKeyFile.exists()) {
                // Load existing keys
                PublicKey publicKey = loadPublicKey(publicKeyFile);
                PrivateKey privateKey = loadPrivateKey(privateKeyFile);

                return new RSAKey.Builder((RSAPublicKey) publicKey)
                        .privateKey((RSAPrivateKey) privateKey)
                        .keyID("auth-service-rsa-key")
                        .build();
            } else {
                // Generate new keys and save them
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
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load or generate RSA keys", e);
        }
    }

    private void saveKey(File file, java.security.Key key) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
            String encoded = Base64.getEncoder().encodeToString(key.getEncoded());
            writer.write(encoded);
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

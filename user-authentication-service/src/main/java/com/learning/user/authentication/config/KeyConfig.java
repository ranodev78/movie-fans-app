package com.learning.user.authentication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class KeyConfig {

    @Bean
    public RSAPublicKey publicKey(@Value("${jwt.public-key}") final Resource publicKeyResource)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (InputStream inputStream = publicKeyResource.getInputStream()) {
            final String key = new String(inputStream.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            final byte[] decoded = Base64.getDecoder().decode(key);
            final X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
        }
    }

    @Bean
    public RSAPrivateKey privateKey(@Value("${jwt.private-key}") final Resource privateKeyResource)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (InputStream inputStream = privateKeyResource.getInputStream()) {
            final String key = new String(inputStream.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            final byte[] decoded = Base64.getDecoder().decode(key);
            final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
        }
    }
}

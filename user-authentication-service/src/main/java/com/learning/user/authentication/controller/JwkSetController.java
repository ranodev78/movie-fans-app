package com.learning.user.authentication.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
public class JwkSetController {
    private final RSAPublicKey publicKey;

    public JwkSetController(final RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @GetMapping("/jwks")
    public Map<String, Object> getKeys() {
        final RSAKey key = new RSAKey.Builder(this.publicKey).build();
        return new JWKSet(key).toJSONObject();
    }
}

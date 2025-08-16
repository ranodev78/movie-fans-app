package com.learning.user.authentication.config.jwt;

import com.learning.user.authentication.config.AuthSecurityConfigurer;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * This class configures JWT based authentication with OAuth2 with validators for JWTs
 *
 * @author Rano J.
 */
@Component
@ConditionalOnProperty(name = "auth.mode", havingValue = "jwt", matchIfMissing = true)
public class JwtAuthSecurityConfigurer implements AuthSecurityConfigurer {
    private static final String JWT_LOGOUT_ERROR_RESPONSE_JSON = "{\"message\":\"Client should discard token. Logout successful\"}";
    private static final String AUTHORITIES_CLAIM_NAME = "roles";
    private static final String AUTHORITIES_PREFIX = "ROLE_";

    private final RSAPublicKey publicKey;

    @Autowired
    public JwtAuthSecurityConfigurer(final RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
                        .requestMatchers(
                                "/login",
                                         "/error",
                                         "/public",
                                         "/register",
                                         "/activate")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .logout(logoutConfigurer -> logoutConfigurer
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(this.jwtLogoutSuccessHandler()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer ->
                        jwtConfigurer.jwtAuthenticationConverter(jwtAuthConverter())))
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.issuer}") final String issuer,
                                 @Value("${jwt.audience:}") final String audience) {
        final OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        final OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

        final OAuth2TokenValidator<Jwt> validator = Optional.ofNullable(audience)
                .filter(Predicate.not(String::isBlank))
                .map(AudienceValidator::new)
                .map(audienceValidator -> new DelegatingOAuth2TokenValidator<>(withIssuer, withTimestamp, audienceValidator))
                .orElse(new DelegatingOAuth2TokenValidator<>(withIssuer, withTimestamp));

        final NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(this.publicKey).build();
        decoder.setJwtValidator(validator);

        return decoder;
    }

    static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
        private final String audience;

        AudienceValidator(final String audience) {
            this.audience = audience;
        }

        public OAuth2TokenValidatorResult validate(final Jwt jwt) {
            return jwt.getAudience().contains(audience)
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(
                            new OAuth2Error("invalid_token", "Missing required audience", null));
        }
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter() {
        final var converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName(AUTHORITIES_CLAIM_NAME);
        converter.setAuthorityPrefix(AUTHORITIES_PREFIX);

        final var jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);

        return jwtConverter;
    }

    @Bean
    public JwtEncoder jwtEncoder(final RSAPrivateKey privateKey) {
        final JWK jwk = new RSAKey.Builder(this.publicKey)
                .privateKey(privateKey)
                .build();

        final JWKSource<SecurityContext> source = new ImmutableJWKSet<>(new JWKSet(jwk));

        return new NimbusJwtEncoder(source);
    }

    @Bean
    public LogoutSuccessHandler jwtLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(JWT_LOGOUT_ERROR_RESPONSE_JSON);
        };
    }
}

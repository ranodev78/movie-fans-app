package com.learning.user.authentication.controller;

import com.learning.user.authentication.dto.LoginRequest;
import com.learning.user.authentication.dto.UserRegistrationRequest;
import com.learning.user.authentication.service.AccountManagementService;
import com.learning.user.authentication.service.ratelimiter.RegistrationRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Validated
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    public static final String ACTIVATE_API_PATH = "/activate";
    public static final String JWT_TOKEN_REQ_PARAM = "token";

    private static final String JWT_CLAIM_NAME = "scope";
    private static final String AUTHORITIES_COLLECTION_DELIMITER = " ";

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final AccountManagementService accountManagementService;
    private final RegistrationRateLimiter rateLimiter;
    private final String issuer;
    private final int jwtExpirationInMinutes;

    @Autowired
    public AuthController(final AuthenticationManager authenticationManager,
                          final JwtEncoder jwtEncoder,
                          final AccountManagementService accountManagementService,
                          final RegistrationRateLimiter rateLimiter,
                          @Value("${jwt.issuer}") final String issuer,
                          @Value("${jwt.expiration-minutes}") final int jwtExpirationInMinutes) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.accountManagementService = accountManagementService;
        this.rateLimiter = rateLimiter;
        this.issuer = issuer;
        this.jwtExpirationInMinutes = jwtExpirationInMinutes;
    }

    @PostMapping(value = "/login",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> login(@RequestBody @NotNull @Valid LoginRequest request) {
        LOGGER.info("Entering AuthController.login with user '{}'...", request.username());

        final Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        final String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(AUTHORITIES_COLLECTION_DELIMITER));

        final Instant now = Instant.now();

        final JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(this.issuer)
                .issuedAt(now)
                .expiresAt(now.plus(this.jwtExpirationInMinutes, ChronoUnit.MINUTES))
                .subject(authentication.getName())
                .claim(JWT_CLAIM_NAME, scope)
                .build();

        final String token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims))
                .getTokenValue();

        return ResponseEntity.ok(Map.of("access_token", token));
    }

    @PostMapping(value = "/register",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerUser(@RequestBody @NotNull @Valid UserRegistrationRequest request,
                                               HttpServletRequest httpServletRequest) {
        LOGGER.info("Entering AuthController.registerUser...");

        if (this.rateLimiter.tryConsume(httpServletRequest)) {
            this.accountManagementService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        LOGGER.warn("Too many requests coming from IP: {}", httpServletRequest.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @GetMapping(value = ACTIVATE_API_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> activateAccount(@RequestParam(JWT_TOKEN_REQ_PARAM) @NotBlank String token) {
        LOGGER.info("Entering AuthController.activateAccount...");
        this.accountManagementService.activateUser(token);
        return ResponseEntity.noContent().build();
    }
}

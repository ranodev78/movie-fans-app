package com.learning.movie.repository.client;

import com.learning.movie.dto.UserDetailsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Repository
public class AppUserClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppUserClient.class);

    private final WebClient webClient;
    private final String authServiceUsersPath;
    private final String authServiceUserByUsernamePath;

    @Autowired
    public AppUserClient(@Qualifier("userAuthenticationServiceClient") final WebClient webClient,
                         @Value("${auth-service.api.users.path}") final String authServiceUsersPath,
                         @Value("${auth-service.api.users.user-by-username.path}") final String authServiceUserByUsernamePath) {
        this.webClient = webClient;
        this.authServiceUsersPath = authServiceUsersPath;
        this.authServiceUserByUsernamePath = authServiceUserByUsernamePath;
    }

    public Mono<String> getPrincipal(final Jwt jwt) {
        LOGGER.info("Entering AppUserClient.getPrincipal...");

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(this.authServiceUsersPath)
                        .build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt.getTokenValue()))
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<UserDetailsResponse> getUserDetails(final Jwt jwt, final String username) {
        LOGGER.info("Entering AppUserClient.getUserDetails...");

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(this.authServiceUserByUsernamePath)
                        .build(username))
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt.getTokenValue()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(UserDetailsResponse.class);
    }
}

package com.learning.movie.repository.client;

import com.learning.movie.dto.UserDetailsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Repository
public class AppUserClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppUserClient.class);

    private static final String API_PATH = "/api/v1.0/users";
    private static final String USER_BY_USERNAME_PATH = API_PATH.concat("/{username}");

    private final WebClient webClient;

    @Autowired
    public AppUserClient(@Qualifier("userAuthenticationServiceClient") final WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> getPrincipal(final Jwt jwt) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(API_PATH)
                        .build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt.getTokenValue()))
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<UserDetailsResponse> getUserDetails(final Jwt jwt, final String username) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(USER_BY_USERNAME_PATH)
                        .build(username))
                .headers(httpHeaders -> httpHeaders.setBearerAuth(jwt.getTokenValue()))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(UserDetailsResponse.class);
    }
}

package com.learning.movie.repository.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IWebClient<T, R> {

    Mono<R> post(T request, MultiValueMap<String, String> headers, Map<String, String> queryParams);

    Mono<R> get(MultiValueMap<String, String> headers, Map<String, String> queryParams);

    default Mono<R> handleResponse(final WebClient.ResponseSpec responseSpec, final Class<R> responseType) {
        return responseSpec
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.error(
                                new RuntimeException(
                                        "Client error occurred, received HTTP response status code: %s"
                                                .formatted(clientResponse.statusCode()))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.error(
                                new RuntimeException(
                                        "Server error occurred, received HTTP response status code: %s"
                                                .formatted(clientResponse.statusCode()))))
                .bodyToMono(responseType);
    }
}

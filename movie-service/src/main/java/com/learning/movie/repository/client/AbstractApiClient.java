package com.learning.movie.repository.client;

import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public abstract class AbstractApiClient<T, R> implements IWebClient<T, R> {
    protected final WebClient webClient;

    protected AbstractApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    protected Mono<R> getSingleResource(final String resourcePath, final Map<String, List<String>> headersToInclude,
                                        final Class<R> responseBodyType) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder.path(resourcePath).build())
                .headers(httpHeaders -> httpHeaders.addAll(CollectionUtils.toMultiValueMap(headersToInclude)))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(responseBodyType);
    }

    protected Mono<R> post(final T request, final String resourcePath, final Map<String, List<String>> headersToInclude,
                           final Class<R> responseBodyType) {
        return this.webClient.post()
                .uri(uriBuilder -> uriBuilder.path(resourcePath).build())
                .headers(httpHeaders -> httpHeaders.addAll(CollectionUtils.toMultiValueMap(headersToInclude)))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(responseBodyType);
    }
}

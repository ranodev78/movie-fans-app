package com.learning.movie.config.retry;

import io.netty.channel.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.Set;
import java.util.concurrent.TimeoutException;

public class WebClientRetryer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebClientRetryer.class);

    private final RetryPolicy retryPolicy;
    private final Set<HttpStatus> errorStatuses;

    public WebClientRetryer(RetryPolicy retryPolicy, HttpStatus... errorStatuses) {
        this.retryPolicy = retryPolicy;
        this.errorStatuses = Set.of(errorStatuses);
    }

    public <T> Mono<RetryResult<T> > execute(Mono<T> request) {
        return request.retryWhen(
                Retry.fixedDelay(this.retryPolicy.getRetryAttempts(), this.retryPolicy.getRetryDelay())
                        .filter(this::isRetryable)
                        .onRetryExhaustedThrow((spec, retrySignal) -> retrySignal.failure()))
                .map(RetryResult::of)
                .defaultIfEmpty(RetryResult.of(null))
                .doOnError(ex -> LOGGER.error(ex.getMessage()))
                .onErrorResume(this::handleExhaustion);
    }

    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            return isTimeout(ex) || this.isErrorStatus(ex);
        } else if (throwable instanceof WebClientRequestException ex) {
            return isTimeout(ex);
        }

        return false;
    }

    private boolean isErrorStatus(WebClientResponseException ex) {
        return this.errorStatuses.stream()
                .anyMatch(errorStatus -> ex.getStatusCode().isSameCodeAs(errorStatus));
    }

    private static boolean isTimeout(WebClientException ex) {
        return ex.contains(TimeoutException.class) || ex.contains(ConnectTimeoutException.class);
    }

    private <T> Mono<RetryResult<T>> handleExhaustion(Throwable throwable) {
        return Mono.just(
                throwable instanceof WebClientResponseException ex && !isTimeout(ex)
                    ? RetryResult.of(ex.getStatusCode(), ex)
                    : RetryResult.of(null, throwable));
    }
}

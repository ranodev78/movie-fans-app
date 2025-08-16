package com.learning.movie.config.retry;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration(proxyBeanMethods = false)
public class RetryConfig {

    @Bean
    @ConfigurationProperties(prefix = "retry.config")
    public RetryPolicy defaultRetryConfig() {
        return new RetryPolicy();
    }

    @Bean
    public WebClientRetryer defaultWebClientRetryer(RetryPolicy defaultRetryConfig) {
        return new WebClientRetryer(defaultRetryConfig, HttpStatus.SERVICE_UNAVAILABLE);
    }
}

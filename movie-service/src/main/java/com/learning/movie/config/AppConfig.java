package com.learning.movie.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learning.movie.dto.tmdb.serializer.GenreSetSerializer;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.net.URI;
import java.time.Duration;

@Configuration
public class AppConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    private static final String AUTH_PREFIX = "Bearer ";
    private static final String OMDB_API_KEY_QUERY_PARAM = "apiKey";

    @Value("${spring.web.reactive.WebClient.max.connections:50}")
    private int maxConnections;

    @Value("${spring.web.reactive.WebClient.max.idle.time:2}")
    private long maxIdleTimeInMins;

    @Value("${spring.web.reactive.WebClient.eviction.threshold:10}")
    private long maxTimeInMinsBeforeEviction;

    @Value("${spring.web.reactive.WebClient.response.timeout:5}")
    private int responseTimeout;

    @Value("${spring.web.reactive.WebClient.connection.timeout:5000}")
    private int connectionTimeout;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.serializers(new GenreSetSerializer());
    }

    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("movie-service")
                .maxConnections(this.maxConnections) // Maximum number of connections in the pool
                .maxIdleTime(Duration.ofMinutes(this.maxIdleTimeInMins)) // How long an idle connections stays open before being closed
                .evictInBackground(Duration.ofMinutes(this.maxTimeInMinsBeforeEviction)) // Eviction strategy for expired connections
                .build();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.create(this.connectionProvider())
                .responseTimeout(Duration.ofSeconds(this.responseTimeout))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.connectionTimeout);
    }

    @Bean
    public ReactorClientHttpConnector reactorClientHttpConnector() {
        return new ReactorClientHttpConnector(this.httpClient());
    }

    @Bean
    public WebClient omdbApiClient(@Value("${omdbapi.base.url:http://www.omdbapi.com}") final String omdbApiBaseUrl,
                                   @Value("${omdbapi.api.key}") final String omdbApiKey) {
        return this.initWebClientBuilderWithBaseConfig(omdbApiBaseUrl)
                .filter((request, next) -> {
                    final URI apiKeyAppendedUri = UriComponentsBuilder.fromUri(request.url())
                            .queryParam(OMDB_API_KEY_QUERY_PARAM, omdbApiKey)
                            .build(true)
                            .toUri();

                    final ClientRequest updatedRequest = ClientRequest.from(request)
                            .url(apiKeyAppendedUri)
                            .build();

                    return next.exchange(updatedRequest);
                })
                .build();
    }

    @Bean
    public WebClient tmdbApiClient(@Value("${tmdb.base.url:https://api.themoviedb.org}") final String tmdbApiBaseUrl,
                                   @Value("${tmdb.api.access.key}") final String tmdbApiAccessKey) {
        final String authorizationToken = AUTH_PREFIX.concat(tmdbApiAccessKey);

        return this.initWebClientBuilderWithBaseConfig(tmdbApiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationToken)
                .build();
    }

    @Bean
    public WebClient userAuthenticationServiceClient(
            @Value("${user-authentication-service.base.url:http://localhost:8080}") final String userAuthenticationServiceBaseUrl
    ) {
        return this.initWebClientBuilderWithBaseConfig(userAuthenticationServiceBaseUrl)
                .build();
    }

    @Bean
    public WebClient twilioSendGridEmailApiClient(
            @Value("${twilio.sendgrid.email.url}") final String twilioSendGridEmailUrl,
            @Value("${twilio.sendgrid.email.api.key}") final String twilioSendGridEmailApiKey) {
        return this.initWebClientBuilderWithBaseConfig(twilioSendGridEmailUrl)
                .defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(twilioSendGridEmailApiKey))
                .build();
    }

    @Bean
    public WebClient openAiClient(@Value("${openai.base.url}") final String openAiBaseUrl,
                                  @Value("${openai.api.key}") final String openAiApiKey) {
        return this.initWebClientBuilderWithBaseConfig(openAiBaseUrl)
                .defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(openAiApiKey))
                .build();
    }

    private WebClient.Builder initWebClientBuilderWithBaseConfig(final String hostName) {
        return WebClient.builder()
                .baseUrl(hostName)
                .filter(((request, next) -> {
                    LOGGER.info("WebClient {} request to: {}", request.method(), request.url());
                    return next.exchange(request);
                }))
                .clientConnector(this.reactorClientHttpConnector());
    }
}

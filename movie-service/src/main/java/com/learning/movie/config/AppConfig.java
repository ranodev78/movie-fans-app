package com.learning.movie.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learning.movie.config.properties.TwilioSendGridEmailApiProperties;
import com.learning.movie.config.properties.WebClientProperties;
import com.learning.movie.dto.tmdb.serializer.GenreSetSerializer;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.net.URI;

@Configuration
public class AppConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    private final WebClientProperties webClientProperties;
    private final ReactorClientHttpConnector reactorClientHttpConnector;

    @Autowired
    public AppConfig(final WebClientProperties webClientProperties,
                     final ReactorClientHttpConnector reactorClientHttpConnector) {
        this.webClientProperties = webClientProperties;
        this.reactorClientHttpConnector = reactorClientHttpConnector;
    }

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
        return ConnectionProvider.builder(this.webClientProperties.getConnectionProviderName())
                .maxConnections(this.webClientProperties.getMaxConnections()) // Maximum number of connections in the pool
                .maxIdleTime(this.webClientProperties.getMaxIdleTimeDuration()) // How long an idle connections stays open before being closed
                .evictInBackground(this.webClientProperties.getEvictionThresholdDuration()) // Eviction strategy for expired connections
                .build();
    }

    @Bean
    public HttpClient httpClient(final ConnectionProvider connectionProvider) {
        return HttpClient.create(connectionProvider)
                .responseTimeout(this.webClientProperties.getResponseTimeoutDuration())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, this.webClientProperties.getConnectionTimeout());
    }

    @Bean
    public ReactorClientHttpConnector reactorClientHttpConnector(final HttpClient httpClient) {
        return new ReactorClientHttpConnector(httpClient);
    }

    @Bean
    public WebClient omdbApiClient(@Value("${omdbapi.base.url}") final String omdbApiBaseUrl,
                                   @Value("${omdbapi.api.key}") final String omdbApiKey,
                                   @Value("${omdbapi.api.key.query-param}") final String omdbApiKeyQueryParam) {
        return this.initWebClientBuilderWithBaseConfig(omdbApiBaseUrl)
                .filter((request, next) -> {
                    final URI apiKeyAppendedUri = UriComponentsBuilder.fromUri(request.url())
                            .queryParam(omdbApiKeyQueryParam, omdbApiKey)
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
    public WebClient tmdbApiClient(@Value("${tmdb.base.url}") final String tmdbApiBaseUrl,
                                   @Value("${tmdb.api.access.key}") final String tmdbApiAccessKey) {
        return this.initWebClientBuilderWithBaseConfig(tmdbApiBaseUrl)
                .defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(tmdbApiAccessKey))
                .build();
    }

    @Bean
    public WebClient userAuthenticationServiceClient(@Value("${auth-service.base.url}") final String authServiceBaseUrl) {
        return this.initWebClientBuilderWithBaseConfig(authServiceBaseUrl)
                .build();
    }

    @Bean
    public WebClient twilioSendGridEmailApiClient(final TwilioSendGridEmailApiProperties twilioSendGridEmailApiProperties) {
        return this.initWebClientBuilderWithBaseConfig(twilioSendGridEmailApiProperties.getUrl())
                .defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(twilioSendGridEmailApiProperties.getKey()))
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
                .clientConnector(this.reactorClientHttpConnector);
    }
}

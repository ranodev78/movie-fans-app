package com.learning.movie.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.movie.dto.PaginatedMoviesResponse;
import com.learning.movie.dto.tmdb.NewlyReleasedMoviesResponse;
import com.learning.movie.dto.tmdb.provider.WatchProvidersResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    private final ObjectMapper objectMapper;

    @Autowired
    public RedisConfig(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(@Value("${spring.redis.host}") final String host,
                                                                         @Value("${spring.redis.port}") final int port) {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public ReactiveRedisTemplate<String, NewlyReleasedMoviesResponse> newlyReleasedMoviesRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        return this.createRedisTemplate(factory, NewlyReleasedMoviesResponse.class);
    }

    @Bean
    public ReactiveRedisTemplate<String, PaginatedMoviesResponse> paginatedSearchMovieResultRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        return this.createRedisTemplate(factory, PaginatedMoviesResponse.class);
    }

    @Bean
    public ReactiveRedisTemplate<String, WatchProvidersResponse> watchProvidersRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        return this.createRedisTemplate(factory, WatchProvidersResponse.class);
    }

    private <V> ReactiveRedisTemplate<String, V> createRedisTemplate(final ReactiveRedisConnectionFactory factory,
                                                                     final Class<V> valueType) {
        final RedisSerializer<String> keySerializer = new StringRedisSerializer();
        final Jackson2JsonRedisSerializer<V> valueSerializer = new Jackson2JsonRedisSerializer<>(this.objectMapper, valueType);

        final RedisSerializationContext<String, V> context =
                RedisSerializationContext.<String, V>newSerializationContext(keySerializer)
                        .key(keySerializer)
                        .value(valueSerializer)
                        .hashKey(keySerializer)
                        .hashValue(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}

package com.learning.movie.service;

import com.learning.movie.dto.tmdb.NewlyReleasedMoviesResponse;
import com.learning.movie.repository.TmdbRepository;
import com.learning.movie.utility.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DiscoverMovieServiceImpl implements DiscoverMovieService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoverMovieServiceImpl.class);

    private static final String REDIS_PREFIX_NAMESPACE = "newlyReleasedMovies:";

    private final TmdbRepository tmdbRepository;
    private final ReactiveRedisTemplate<String, NewlyReleasedMoviesResponse> reactiveRedisTemplate;
    private final ConcurrentHashMap<String, Mono<NewlyReleasedMoviesResponse>> lockMap = new ConcurrentHashMap<>();

    @Autowired
    public DiscoverMovieServiceImpl(
            final TmdbRepository tmdbRepository,
            @Qualifier("newlyReleasedMoviesRedisTemplate") final ReactiveRedisTemplate<String, NewlyReleasedMoviesResponse> reactiveRedisTemplate) {
        this.tmdbRepository = tmdbRepository;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public Mono<NewlyReleasedMoviesResponse> getNewMovies() {
        final LocalDate today = LocalDate.now(ZoneOffset.UTC);
        final String cacheKey = REDIS_PREFIX_NAMESPACE.concat(today.format(DateTimeFormatter.ISO_DATE));

        return this.reactiveRedisTemplate.opsForValue()
                .get(cacheKey)
                .doOnNext(response -> LOGGER.info("Cache hit for key: {}", cacheKey))
                .switchIfEmpty(Mono.defer(() -> this.lockMap.computeIfAbsent(
                                cacheKey,
                                key -> this.fetchThenCacheNewMovies(today, cacheKey)
                                        .doOnSuccess(response -> LOGGER.info("Caching new movies with key: {}", cacheKey))
                                        .doFinally(signal -> this.lockMap.remove(cacheKey))
                                        .cache())
                ));
    }

    private Mono<NewlyReleasedMoviesResponse> fetchThenCacheNewMovies(final LocalDate today, final String cacheKey) {
        return this.tmdbRepository.getNewMovies(today, today, null)
                .filter(firstPageResult -> !CollectionUtils.isEmpty(firstPageResult.getResults()))
                .flatMap(firstPageResult -> {
                    final int pageCount = Utility.calculatePageCount(firstPageResult.getTotalResults(), 10);

                    return Flux.range(2, Math.max(0, pageCount - 1))
                            .flatMap(pageNumber -> this.tmdbRepository.getNewMovies(today, today, pageNumber), 10)
                            .collectList()
                            .map(aggregatedNewMovies -> {
                                aggregatedNewMovies.add(0, firstPageResult);

                                return aggregatedNewMovies.stream()
                                        .flatMap(remainingPage -> remainingPage.getResults().stream())
                                        .collect(Collectors.collectingAndThen(
                                                Collectors.toList(),
                                                newMovies -> new NewlyReleasedMoviesResponse(null, newMovies, null, null)
                                        ));
                            });
                })
                .flatMap(aggregatedNewMovies -> this.reactiveRedisTemplate.opsForValue()
                        .set(cacheKey, aggregatedNewMovies, Duration.ofDays(1L))
                        .thenReturn(aggregatedNewMovies));
    }
}

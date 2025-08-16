package com.learning.movie.service.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.movie.service.DiscoverMovieService;
import com.learning.movie.service.publisher.MoviePublisherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MoviePollingScheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoviePollingScheduler.class);

    private final DiscoverMovieService discoverMovieService;
    private final MoviePublisherService moviePublisherService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MoviePollingScheduler(final DiscoverMovieService discoverMovieService,
                                 final MoviePublisherService moviePublisherService,
                                 final ObjectMapper objectMapper) {
        this.discoverMovieService = discoverMovieService;
        this.moviePublisherService = moviePublisherService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "*/30 * * * * ?", zone = "UTC")
    public void pollForNewMovies() {
        this.discoverMovieService.getNewMovies()
                .doOnSubscribe(subscription -> LOGGER.info("Preloading new movies"))
                .doOnNext(newMovies -> {
                    try {
                        final String newMoviesJson = this.objectMapper.writeValueAsString(newMovies);
                        this.moviePublisherService.publishDailyNewMovie(newMoviesJson);
                        LOGGER.info("Successfully published new movies");
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Failed to serialize movie: {}", e.getMessage());
                    }
                })
                .doOnError(error -> LOGGER.error("Error while preloading newly released movies due to: {}", error.getMessage()))
                .subscribe();
    }
}

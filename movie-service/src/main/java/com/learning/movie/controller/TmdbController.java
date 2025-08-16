package com.learning.movie.controller;

import com.learning.movie.dto.tmdb.NewlyReleasedMoviesResponse;
import com.learning.movie.dto.tmdb.provider.WatchProvidersResponse;
import com.learning.movie.repository.TmdbRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1.0/tmdb/movies")
public class TmdbController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TmdbController.class);

    private final TmdbRepository tmdbRepository;

    @Autowired
    public TmdbController(final TmdbRepository tmdbRepository) {
        this.tmdbRepository = tmdbRepository;
    }

    @GetMapping(value = "/{ttId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> findByTtId(@PathVariable("ttId") String ttId) {
        LOGGER.info("Entering TmdbController.findByTtId: {}", ttId);
        return this.tmdbRepository.findByTtId(ttId);
    }

    @GetMapping(value = "/{movieId}/watch-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<WatchProvidersResponse> getMovieProviders(@PathVariable("movieId") String movieId) {
        LOGGER.info("Entering TmdbController.getMovieProviders for movie with ID: {}", movieId);
        return this.tmdbRepository.getMovieWatchProviders(movieId);
    }

    @GetMapping(value = "/new")
    public Mono<NewlyReleasedMoviesResponse> getNewMovies() {
        LOGGER.info("Entering TmdbController.getNewMovies");
        final LocalDate today = LocalDate.now();
        return this.tmdbRepository.getNewMovies(today, today, null);
    }
}

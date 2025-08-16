package com.learning.movie.controller;

import com.learning.movie.dto.tmdb.NewlyReleasedMoviesResponse;
import com.learning.movie.dto.tmdb.provider.WatchProvidersResponse;
import com.learning.movie.repository.TmdbRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1.0/tmdb/movies")
public class TmdbController {
    private static final Logger LOGGER = Logger.getLogger(TmdbController.class.getSimpleName());

    private final TmdbRepository tmdbRepository;

    public TmdbController(final TmdbRepository tmdbRepository) {
        this.tmdbRepository = tmdbRepository;
    }

    @GetMapping(value = "/{ttId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> findByTtId(@PathVariable("ttId") String ttId) {
        return this.tmdbRepository.findByTtId(ttId);
    }

    @GetMapping(value = "/{movieId}/watch-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<WatchProvidersResponse> getMovieProviders(@PathVariable("movieId") String movieId) {
        return this.tmdbRepository.getMovieWatchProviders(movieId);
    }

    @GetMapping(value = "/new")
    public Mono<NewlyReleasedMoviesResponse> getNewMovies() {
        final LocalDate today = LocalDate.now();

        return this.tmdbRepository.getNewMovies(today, today, null);
    }
}

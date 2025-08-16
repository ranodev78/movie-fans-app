package com.learning.movie.controller;

import com.learning.movie.dto.tmdb.TmdbMovie;
import com.learning.movie.dto.tmdb.provider.WatchProvidersResponse;
import com.learning.movie.dto.tmdb.search.MovieSearchResponse;
import com.learning.movie.service.DiscoverMovieService;
import com.learning.movie.service.tmdb.TmdbMovieService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1.0/movies/tmdb")
@Validated
public class MovieDiscoveryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovieDiscoveryController.class);

    private final TmdbMovieService tmdbMovieService;
    private final DiscoverMovieService discoverMovieService;

    @Autowired
    public MovieDiscoveryController(TmdbMovieService tmdbMovieService, DiscoverMovieService discoverMovieService) {
        this.tmdbMovieService = tmdbMovieService;
        this.discoverMovieService = discoverMovieService;
    }

    @GetMapping(value = "/{movieId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TmdbMovie> getMovieDetails(@PathVariable Long movieId) {
        LOGGER.info("Entering MovieDiscoveryController.getMovieDetails...");
        return this.tmdbMovieService.getTmdbMovieById(movieId);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MovieSearchResponse> searchMovies(@RequestParam("q") @NotBlank String text) {
        LOGGER.info("Entering MovieDiscoveryController.searchMovies...");
        return this.tmdbMovieService.searchForMovies(text);
    }

    @GetMapping(value = "/{movieId}/watch-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<WatchProvidersResponse> getWatchProviders(@PathVariable @NotNull Long movieId) {
        LOGGER.info("Entering MovieDiscoveryController.getWatchProviders for movie: [{}]", movieId);
        return this.tmdbMovieService.getMovieWatchProviders(movieId);
    }

    @GetMapping(value = "/{movieId}/reviews", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> getSummarizedMovieReviews(@PathVariable @NotNull Long movieId,
                                                  @RequestParam("name") @NotBlank String name) {
        LOGGER.info("Entering MovieDiscoveryController.getSummarizedMovieReviews...");
        return this.tmdbMovieService.getMovieReviews(movieId, name);
    }
}

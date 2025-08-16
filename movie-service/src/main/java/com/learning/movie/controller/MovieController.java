package com.learning.movie.controller;

import com.learning.movie.dto.CreateMovieRequest;
import com.learning.movie.dto.MovieDto;
import com.learning.movie.dto.UpdateMovieRatingRequest;
import com.learning.movie.dto.subscription.StreamingReleaseSubscriptionRequest;
import com.learning.movie.dto.tmdb.NewlyReleasedMoviesResponse;
import com.learning.movie.model.enums.FilmMediaType;
import com.learning.movie.service.DiscoverMovieService;
import com.learning.movie.service.MovieService;
import com.learning.movie.service.subscription.MovieStreamingReleaseSubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1.0/movies")
@Validated
public class MovieController {
    private static final Logger LOGGER = Logger.getLogger(MovieController.class.getSimpleName());

    private static final String MOVIES_PATH_PREFIX = "/movies";

    private final MovieService movieService;
    private final DiscoverMovieService discoverMovieService;
    private final MovieStreamingReleaseSubscriptionService subscriptionService;

    @Autowired
    public MovieController(final MovieService movieService,
                           final DiscoverMovieService discoverMovieService,
                           final MovieStreamingReleaseSubscriptionService subscriptionService) {
        this.movieService = movieService;
        this.discoverMovieService = discoverMovieService;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MovieDto>> addMovie(@RequestBody @NotNull @Valid CreateMovieRequest createMovieRequest) {
        LOGGER.info("Entering MovieController.addMovie...");
        return this.movieService.addMovie(createMovieRequest)
                .map(savedMovie -> ResponseEntity
                        .created(URI.create(MOVIES_PATH_PREFIX + savedMovie.getMovieId()))
                        .body(savedMovie));
    }

    @PostMapping(value = "/test", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MovieDto> addMovieTest(@RequestBody @NotNull @Valid CreateMovieRequest createMovieRequest) {
        LOGGER.info("Entering MovieController.addMovie...");
        return this.movieService.addMovie(createMovieRequest);
    }

    @PutMapping(value = "/{movieId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MovieDto> updateMovieRating(@PathVariable @NotBlank UUID movieId,
                                            @RequestBody @NotNull @Valid UpdateMovieRatingRequest request) {
        LOGGER.info("Entering MovieController.updateMovieRating...");
        return this.movieService.updateMovieRating(movieId, request);
    }

    @GetMapping(value = "/{movieId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MovieDto> getMovieById(@PathVariable @NotBlank UUID movieId) {
        LOGGER.info("Entering MovieController.getMovieById...");
        return this.movieService.getMovieById(movieId);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<?>> searchForMovies(@RequestParam @NotBlank String s,
                                                   @RequestParam(name = "t", required = false) String type,
                                                   @RequestParam(name = "y", required = false) String year,
                                                   @RequestParam(name = "page", required = false) Integer page) {
        LOGGER.info("Entering MovieController.searchForMovies");

        return (page == null
                    ? this.movieService.showAllSearchResults(s, FilmMediaType.fromValue(type), year)
                    : this.movieService.showSearchResultWithPageNumber(s, FilmMediaType.fromValue(type), year, page))
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/daily-new", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<NewlyReleasedMoviesResponse> getDailyNewMovies() {
        LOGGER.info("Entering MovieController.getDailyNewMovies");
        return this.discoverMovieService.getNewMovies();
    }

    @PostMapping(value = "/subscriptions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Void> subscribe(@RequestBody @NotNull StreamingReleaseSubscriptionRequest request,
                                @AuthenticationPrincipal Jwt principal) {
        return subscriptionService.subscribe(request.getMovieId(), request.getMovieName(), Set.of(request.getStreamingPlatform()));
    }
}

package com.learning.movie.controller;

import com.learning.movie.dto.omdbapi.OmdbApiPaginatedSearchResponse;
import com.learning.movie.dto.omdbapi.OmdbApiResponse;
import com.learning.movie.model.enums.FilmMediaType;
import com.learning.movie.repository.OmdbApiRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1.0/omdbapi")
@Validated
public class OmdbiApiController {
    private static final Logger LOGGER = Logger.getLogger(OmdbiApiController.class.getSimpleName());

    private final OmdbApiRepository omdbApiRepository;

    public OmdbiApiController(final OmdbApiRepository omdbApiRepository) {
        this.omdbApiRepository = omdbApiRepository;
    }

    @GetMapping(value = "/{ttId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<OmdbApiResponse> getMovieByTtId(@PathVariable @NotBlank String ttId) {
        LOGGER.info("Entering MovieController.getMovieByTtId");
        return this.omdbApiRepository.getMovieByTtId(ttId);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<OmdbApiPaginatedSearchResponse> searchForMoviesFromOmdbApi(@RequestParam @NotBlank String s,
                                                                           @RequestParam(name = "t", required = false) String type,
                                                                           @RequestParam(name = "y", required = false) String year,
                                                                           @RequestParam(name = "page", required = false) Integer page) {
        LOGGER.info("Entering MovieController.searchForMovies");
        return this.omdbApiRepository.findMovieByQueryParameters(s, FilmMediaType.fromValue(type), year, null);
    }
}

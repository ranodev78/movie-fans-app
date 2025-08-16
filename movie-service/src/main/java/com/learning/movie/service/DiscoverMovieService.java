package com.learning.movie.service;

import com.learning.movie.dto.tmdb.NewlyReleasedMoviesResponse;
import reactor.core.publisher.Mono;

public interface DiscoverMovieService {

    Mono<NewlyReleasedMoviesResponse> getNewMovies();
}

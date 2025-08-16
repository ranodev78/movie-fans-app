package com.learning.movie.service;

import java.util.List;
import java.util.UUID;

import com.learning.movie.dto.CachedPaginatedResponse;
import com.learning.movie.dto.CreateMovieRequest;
import com.learning.movie.dto.MovieDto;
import com.learning.movie.dto.PaginatedMoviesResponse;
import com.learning.movie.dto.UpdateMovieRatingRequest;
import com.learning.movie.dto.omdbapi.OmdbApiResponse;
import com.learning.movie.model.enums.FilmMediaType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovieService {

    Mono<MovieDto> addMovie(CreateMovieRequest createMovieRequest);

    void getMovies();

    Mono<MovieDto> getMovieById(UUID movieId);

    Mono<PaginatedMoviesResponse> showAllSearchResults(String search, FilmMediaType type, String year);

    Mono<CachedPaginatedResponse> showSearchResultWithPageNumber(String search, FilmMediaType type, String year,
                                                                 Integer pageNumber);

    Mono<Void> deleteMovieById(UUID movieId);

    Flux<MovieDto> getMoviesByIdInBatch(List<UUID> movieIds);

    Mono<MovieDto> updateMovieRating(UUID movieId, UpdateMovieRatingRequest request);

    Mono<OmdbApiResponse> validateMovie(CreateMovieRequest createMovieRequest);
}

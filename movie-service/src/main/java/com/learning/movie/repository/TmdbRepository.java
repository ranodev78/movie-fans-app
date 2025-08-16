package com.learning.movie.repository;

import com.learning.movie.dto.tmdb.NewlyReleasedMoviesResponse;
import com.learning.movie.dto.tmdb.TmdbMovie;
import com.learning.movie.dto.tmdb.provider.WatchProvidersResponse;
import com.learning.movie.dto.tmdb.review.TmdbReviewResponse;
import com.learning.movie.dto.tmdb.search.MovieSearchResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Repository
public class TmdbRepository {

    // Paths
    private static final String DISCOVER_PATH = "/3/discover/movie";
    private static final String FIND_BY_TTID_PATH = "/3/find/{external_id}";
    private static final String GET_MOVIE_WATCH_PROVIDERS_PATH = "/3/movie/{movie_id}/watch/providers";
    private static final String GET_MOVIE_DETAILS_BY_TTID_PATH = "/3/movie/{movie_id}";
    private static final String SEARCH_MOVIE_PATH = "/3/search/movie";
    private static final String GET_MOVIE_REVIEWS = GET_MOVIE_DETAILS_BY_TTID_PATH.concat("/reviews");

    // Query parameters
    private static final String EXTERNAL_SOURCE_QUERY_PARAM = "external_source";
    private static final String EXTERNAL_SOURCE_VALUE = "imdb_id";
    private static final String START_DATE_QUERY_PARAM = "primary_release_date.lte";
    private static final String END_DATE_QUERY_PARAM = "primary_release_date.gte";
    private static final String PAGE_QUERY_PARAM = "page";
    private static final String QUERY_PARAM = "query";
    private static final String YEAR_QUERY_PARAM = "year";

    private final WebClient webClient;

    public TmdbRepository(final @Qualifier("tmdbApiClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> findByTtId(final String ttId) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(FIND_BY_TTID_PATH)
                        .queryParam(EXTERNAL_SOURCE_QUERY_PARAM, EXTERNAL_SOURCE_VALUE)
                        .build(ttId))
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<WatchProvidersResponse> getMovieWatchProviders(String movieId) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(GET_MOVIE_WATCH_PROVIDERS_PATH)
                        .build(movieId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(WatchProvidersResponse.class);
    }

    public Mono<NewlyReleasedMoviesResponse> getNewMovies(LocalDate startDate, LocalDate endDate, Integer page) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(DISCOVER_PATH)
                        .queryParam(START_DATE_QUERY_PARAM, startDate.format(DateTimeFormatter.ISO_DATE))
                        .queryParam(END_DATE_QUERY_PARAM, endDate.format(DateTimeFormatter.ISO_DATE))
                        .queryParamIfPresent(PAGE_QUERY_PARAM, Optional.ofNullable(page))
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(NewlyReleasedMoviesResponse.class);
    }

    public Mono<TmdbMovie> getMovieDetailsByTtId(final String ttId) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(GET_MOVIE_DETAILS_BY_TTID_PATH)
                        .build(ttId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TmdbMovie.class);
    }

    public Mono<MovieSearchResponse> searchForMovies(String query, String year) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(SEARCH_MOVIE_PATH)
                        .queryParam(QUERY_PARAM, query)
                        .queryParamIfPresent(YEAR_QUERY_PARAM, Optional.ofNullable(year))
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(MovieSearchResponse.class);
    }

    public Mono<TmdbReviewResponse> getMovieReviews(String movieId, int page) {
        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(GET_MOVIE_REVIEWS)
                        .queryParam(PAGE_QUERY_PARAM, page)
                        .build(movieId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TmdbReviewResponse.class);
    }
}

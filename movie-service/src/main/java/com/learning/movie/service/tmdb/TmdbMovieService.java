package com.learning.movie.service.tmdb;

import com.learning.movie.dto.tmdb.TmdbMovie;
import com.learning.movie.dto.tmdb.provider.WatchProvidersResponse;
import com.learning.movie.dto.tmdb.review.AggregatedTmdbReviews;
import com.learning.movie.dto.tmdb.review.TmdbReview;
import com.learning.movie.dto.tmdb.review.TmdbReviewResponse;
import com.learning.movie.dto.tmdb.search.MovieSearchResponse;
import com.learning.movie.repository.TmdbRepository;
import com.learning.movie.service.openapi.OpenAiService;
import com.learning.movie.service.preprocessor.SearchParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TmdbMovieService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TmdbMovieService.class);

    private final TmdbRepository tmdbRepository;
    private final OpenAiService openAiService;

    @Autowired
    public TmdbMovieService(TmdbRepository tmdbRepository, OpenAiService openAiService) {
        this.tmdbRepository = tmdbRepository;
        this.openAiService = openAiService;
    }

    public Mono<TmdbMovie> getTmdbMovieById(final Long movieId) {
        return this.tmdbRepository.getMovieDetailsByTtId(String.valueOf(movieId));
    }

    public Mono<MovieSearchResponse> searchForMovies(String text) {
        final Map<String, String> parsedParams = SearchParser.parseSearch(text);
        final String query = parsedParams.get(SearchParser.PARSE_KEY_QUERY);
        final String year = parsedParams.get(SearchParser.PARSE_KEY_YEAR);

        return this.tmdbRepository.searchForMovies(query, year);
    }

    public Mono<WatchProvidersResponse> getMovieWatchProviders(Long movieId) {
        return this.tmdbRepository.getMovieWatchProviders(String.valueOf(movieId));
    }

    public Mono<String> getMovieReviews(Long movieId, String movieName) {
        final String tmdbMovieId = String.valueOf(movieId);

        return this.tmdbRepository.getMovieReviews(tmdbMovieId, 1)
                .flatMap(pageOneReview ->
                    pageOneReview.totalPages() > 1
                            ? this.callTmdbReviewsApiInParallel(pageOneReview, movieName, tmdbMovieId)
                            : Mono.just(new AggregatedTmdbReviews(movieName, List.of(pageOneReview.results().get(0).content())))
                )
                .flatMap(this.openAiService::summarizeMovieReviews);
    }

    private Mono<AggregatedTmdbReviews> callTmdbReviewsApiInParallel(TmdbReviewResponse pageOneReview, String movieName,
                                                                    String tmdbMovieId) {
        final int totalPages = pageOneReview.totalPages();

        return Flux.range(2, totalPages - 1)
                .flatMap(pageNum -> this.tmdbRepository.getMovieReviews(tmdbMovieId, pageNum))
                .collectList()
                .doOnNext(movieReviews -> movieReviews.add(pageOneReview))
                .map(aggregatedMovieReviewsResponse ->
                        aggregatedMovieReviewsResponse.stream()
                                .flatMap(movieReviewResponse -> movieReviewResponse.results().stream())
                                .map(TmdbReview::content)
                                .collect(Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        movieReviews -> new AggregatedTmdbReviews(movieName, movieReviews)
                        )));
    }
}

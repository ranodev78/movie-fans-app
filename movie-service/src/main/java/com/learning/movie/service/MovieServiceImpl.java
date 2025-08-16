package com.learning.movie.service;

import com.learning.movie.dto.CachedPaginatedResponse;
import com.learning.movie.dto.CreateMovieRequest;
import com.learning.movie.dto.MovieDto;
import com.learning.movie.dto.MovieSummaryDto;
import com.learning.movie.dto.PaginatedMoviesResponse;
import com.learning.movie.dto.UpdateMovieRatingRequest;
import com.learning.movie.dto.omdbapi.OmdbApiResponse;
import com.learning.movie.exception.MovieNotFoundException;
import com.learning.movie.mapper.MovieMapper;
import com.learning.movie.model.Movie;
import com.learning.movie.model.enums.FilmMediaType;
import com.learning.movie.repository.MovieRepository;
import com.learning.movie.repository.OmdbApiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MovieServiceImpl implements MovieService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovieServiceImpl.class.getName());

    private static final int MAX_CONCURRENT_API_CALLS = 10;
    private static final int PAGE_SIZE = 10;

    private static final String INSERTION_SQL_STATEMENT = "INSERT INTO movie_details (id, title, ttid) VALUES (:id, :title, :ttid)";
    private static final String SELECTION_SQL_STATEMENT = "SELECT * FROM movie_details WHERE id = :id";

    private static final String MOVIE_ID_COLUMN = "id";
    private static final String TITLE_COLUMN = "title";
    private static final String TTID_COLUMN = "ttid";

    private static final PaginatedMoviesResponse DEFAULT_BLANK_MOVIES_RESPONSE = new PaginatedMoviesResponse(
            0,  Collections.emptyList());

    private final MovieRepository movieRepository;
    private final OmdbApiRepository omdbApiRepository;
    private final DatabaseClient databaseClient;
    private final ReactiveRedisTemplate<String, PaginatedMoviesResponse> reactiveRedisTemplate;

    @Autowired
    public MovieServiceImpl(final MovieRepository movieRepository,
                            final OmdbApiRepository omdbApiRepository,
                            final DatabaseClient databaseClient,
                            @Qualifier("paginatedSearchMovieResultRedisTemplate") final ReactiveRedisTemplate<String, PaginatedMoviesResponse> reactiveRedisTemplate) {
        this.movieRepository = movieRepository;
        this.omdbApiRepository = omdbApiRepository;
        this.databaseClient = databaseClient;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public Mono<MovieDto> addMovie(final CreateMovieRequest createMovieRequest) {
        return this.movieRepository.findByTtId(createMovieRequest.getTtId())
                .hasElement()
                .flatMap(didFindMovie -> Boolean.TRUE.equals(didFindMovie)
                        ? Mono.defer(() -> Mono.error(
                                new Exception("Movie already exists - title: %s | ttId: %s"
                                        .formatted(createMovieRequest.getTitle(), createMovieRequest.getTtId()))))
                        : this.validateMovie(createMovieRequest)
                            .flatMap(omdbApiMovie -> this.movieRepository.findByTtId(omdbApiMovie.getImdbID()))
                            .switchIfEmpty(Mono.defer(() -> {
                                final String movieId = UUID.randomUUID().toString();

                                return this.databaseClient.sql(INSERTION_SQL_STATEMENT)
                                        .bind(MOVIE_ID_COLUMN, movieId)
                                        .bind(TITLE_COLUMN, createMovieRequest.getTitle())
                                        .bind(TTID_COLUMN, createMovieRequest.getTtId())
                                        .fetch()
                                        .rowsUpdated()
                                        .filter(rows -> rows > 0)
                                        .switchIfEmpty(Mono.error(new IllegalStateException("Failed to insert movie")))
                                        .flatMap(rows -> this.databaseClient.sql(SELECTION_SQL_STATEMENT)
                                                .bind(MOVIE_ID_COLUMN, movieId)
                                                .map((row, metadata) -> {
                                                    final Movie rawMovie = new Movie(
                                                            row.get(TITLE_COLUMN, String.class),
                                                            row.get(TTID_COLUMN, String.class));

                                                    rawMovie.setMovieId(movieId);

                                                    return rawMovie;
                                                })
                                                .one());
                            }))
                            .doOnSuccess(savedMovie -> LOGGER.info("Persisted movie with ID: [{}] into the database", savedMovie.getMovieId()))
                            .map(MovieMapper::movieEntityToMovieResponseDTO));
    }

    @Override
    public void getMovies() {

    }

    @Override
    public Mono<MovieDto> getMovieById(final UUID movieId) {
        return this.movieRepository.findById(movieId)
                .switchIfEmpty(Mono.error(new MovieNotFoundException(movieId)))
                .map(MovieMapper::movieEntityToMovieResponseDTO)
                .doOnSuccess(fetchedMovie -> LOGGER.info("Retrieved movie with ID: {}", movieId));
    }

    @Override
    public Mono<PaginatedMoviesResponse> showAllSearchResults(final String search, final FilmMediaType type, final String year) {
        final String key = "movieapp:%s:%s:%s".formatted(search.toLowerCase(), type.getValue(), year);

        return this.reactiveRedisTemplate.opsForValue()
                .get(key)
                .switchIfEmpty(Mono.defer(() -> this.searchByQueryParams(search, type, year))
                        .flatMap(movieSearchResult -> this.reactiveRedisTemplate.opsForValue()
                                .set(key, movieSearchResult)
                                .thenReturn(movieSearchResult)));
    }

    @Override
    public Mono<CachedPaginatedResponse> showSearchResultWithPageNumber(String search, FilmMediaType type, String year,
                                                                        Integer pageNumber) {
        final String key = "movieapp:%s:%s:%s".formatted(search.toLowerCase(), type.getValue(), year);

        return this.reactiveRedisTemplate.opsForValue()
                .get(key)
                .map(paginatedMoviesResponse -> new CachedPaginatedResponse(
                        pageNumber, getPageResult(pageNumber, paginatedMoviesResponse.getMovies())))
                .switchIfEmpty(
                        Mono.defer(() -> this.omdbApiRepository
                                .findMovieByQueryParameters(search, type, year, pageNumber)
                                .map(omdbApiPaginatedSearchResponse -> omdbApiPaginatedSearchResponse.getSearch()
                                        .stream()
                                        .map(MovieMapper::fromOmdbApiMovieSummary)
                                        .collect(Collectors.collectingAndThen(
                                                Collectors.toList(),
                                                movieSummaries -> new CachedPaginatedResponse(pageNumber, movieSummaries))))
                ));
    }

    private static List<MovieSummaryDto> getPageResult(int pageNumber, List<MovieSummaryDto> allMovies) {
        final int totalItems = allMovies.size();
        final int fromIndex = (pageNumber - 1) * PAGE_SIZE;
        if (fromIndex >= totalItems) {
            return Collections.emptyList();
        }

        final int toIndex = Math.min(fromIndex + 10, totalItems);

        return allMovies.subList(fromIndex, toIndex);
    }

    private Mono<PaginatedMoviesResponse> searchByQueryParams(final String search, final FilmMediaType type,
                                                              final String year) {
        return this.omdbApiRepository.findMovieByQueryParameters(search, type, year, null)
                .filter(firstPageResult -> firstPageResult.getTotalResults() != null && firstPageResult.getTotalResults() > 0)
                .flatMap(firstPageResult -> {
                    final int pageCount = !firstPageResult.getSearch().isEmpty()
                            ? (int) Math.ceil((double) firstPageResult.getTotalResults() / PAGE_SIZE)
                            : 1;

                    return Flux.fromStream(IntStream.rangeClosed(2, pageCount).boxed())
                            .flatMap(pageNumber -> this.omdbApiRepository.findMovieByQueryParameters(search, type, year, pageNumber),
                                     MAX_CONCURRENT_API_CALLS)
                            .collectList()
                            .map(omdbApiPaginatedSearchResponses -> {
                                omdbApiPaginatedSearchResponses.add(0, firstPageResult);

                                return omdbApiPaginatedSearchResponses.stream()
                                        .flatMap(omdbApiResponse -> omdbApiResponse.getSearch().stream()
                                                .filter(movieSummary -> type == null || type == movieSummary.getType())
                                                .map(MovieMapper::fromOmdbApiMovieSummary))
                                        .collect(Collectors.collectingAndThen(
                                                Collectors.toList(),
                                                movieSummaries -> new PaginatedMoviesResponse(pageCount, movieSummaries)));
                            });
                })
                .defaultIfEmpty(DEFAULT_BLANK_MOVIES_RESPONSE);
    }

    @Override
    public Mono<Void> deleteMovieById(final UUID movieId) {
        return this.movieRepository.deleteById(movieId)
                .switchIfEmpty(Mono.error(new MovieNotFoundException(movieId)))
                .doOnSuccess(deleteResponse -> LOGGER.info("Successfully deleted movie with ID: {}", movieId));
    }

    @Override
    public Flux<MovieDto> getMoviesByIdInBatch(final List<UUID> movieIds) {
        return Flux.fromIterable(movieIds)
                .flatMap(movieId -> this.getMovieById(movieId)
                        .materialize()
                        .doOnNext(signal -> {
                            if (signal.isOnError()) {
                                LOGGER.warn("Could not retrieve movie with ID: {}", movieId);
                            }
                        })
                        .filter(Signal::isOnNext)
                        .map(Signal::get));
    }

    @Override
    public Mono<MovieDto> updateMovieRating(final UUID movieId, final UpdateMovieRatingRequest request) {
        return this.movieRepository.findById(movieId)
                .switchIfEmpty(Mono.error(new MovieNotFoundException(movieId)))
                .doOnSuccess(matchingMovie -> matchingMovie.setRating(request.getRating()))
                .flatMap(this.movieRepository::save)
                .map(MovieMapper::movieEntityToMovieResponseDTO)
                .doOnSuccess(updateResponse -> LOGGER.info("Updated rating to {} for {}", request.getRating(), movieId));
    }

    @Override
    public Mono<OmdbApiResponse> validateMovie(CreateMovieRequest createMovieRequest) {
        return this.omdbApiRepository.getMovieByTtId(createMovieRequest.getTtId())
                .switchIfEmpty(Mono.error(new Exception("No movie with ttID: [%s] was found".formatted(createMovieRequest.getTtId()))))
                .filter(omdbApiResponse -> verifyMovieInformation(createMovieRequest, omdbApiResponse))
                .switchIfEmpty(Mono.error(new Exception("Requested movie's information does not match with OMDB API's records")))
                .doOnSuccess(response -> LOGGER.info("Requested movie's information is valid"));
    }

    private static boolean verifyMovieInformation(CreateMovieRequest createMovieRequest, OmdbApiResponse omdbApiResponse) {
        return createMovieRequest.getTitle().equals(omdbApiResponse.getTitle()) &&
               (!StringUtils.hasText(createMovieRequest.getDirector()) || createMovieRequest.getDirector().equals(omdbApiResponse.getDirector())) &&
               (!StringUtils.hasText(createMovieRequest.getGenre()) || createMovieRequest.getGenre().equals(omdbApiResponse.getGenre())) &&
               (createMovieRequest.getReleasedYear() == null || createMovieRequest.getReleasedYear().toString().equals(omdbApiResponse.getYear()));
    }
}

package com.learning.movie.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.movie.dto.omdbapi.OmdbApiResponse;
import com.learning.movie.dto.omdbapi.OmdbApiPaginatedSearchResponse;
import com.learning.movie.model.enums.FilmMediaType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.logging.Logger;

@Repository
public class OmdbApiRepository {
    private static final Logger LOGGER = Logger.getLogger(OmdbApiRepository.class.getSimpleName());

    private static final String TT_ID_QUERY_PARAM = "i";
    private static final String SEARCH_QUERY_PARAM = "s";
    private static final String TYPE_QUERY_PARAM = "t";
    private static final String YEAR_QUERY_PARAM = "y";
    private static final String PAGE_QUERY_PARAM = "page";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OmdbApiRepository(final @Qualifier("omdbApiClient") WebClient webClient,
                             final ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public Mono<OmdbApiResponse> getMovieByTtId(final String ttId) {
        LOGGER.info("Entering OmdbApiClient.getMovieByTtId...");

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam(TT_ID_QUERY_PARAM, ttId)
                        .build())
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseText -> this.mapApplicationTextToOmdbDto(responseText, OmdbApiResponse.class));
    }

    public Mono<OmdbApiPaginatedSearchResponse> findMovieByQueryParameters(final String search, final FilmMediaType mediaType,
                                                                           final String year, final Integer page) {
        LOGGER.info("Entering OmdbApiClient.findMovieByQueryParameters with query parameters, title: %s | type: %s | year: %s | page: %s"
                    .formatted(search, mediaType, year, page));

        final String type = mediaType == null ? null : mediaType.getValue();

        return this.webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam(SEARCH_QUERY_PARAM, search)
                        .queryParamIfPresent(TYPE_QUERY_PARAM, Optional.ofNullable(type))
                        .queryParamIfPresent(YEAR_QUERY_PARAM, Optional.ofNullable(year))
                        .queryParamIfPresent(PAGE_QUERY_PARAM, Optional.ofNullable(page))
                        .build())
                .accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToMono(String.class)
                .map(responseText -> this.mapApplicationTextToOmdbDto(responseText, OmdbApiPaginatedSearchResponse.class));
    }

    private <T> T mapApplicationTextToOmdbDto(String responseText, Class<T> responseType) {
        try {
            return this.objectMapper.readValue(responseText, responseType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

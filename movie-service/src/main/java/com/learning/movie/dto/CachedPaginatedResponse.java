package com.learning.movie.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CachedPaginatedResponse {
    private final int pageNumber;
    private final List<MovieSummaryDto> movies;

    @JsonCreator
    public CachedPaginatedResponse(@JsonProperty("pageNumber") final int pageNumber,
                                   @JsonProperty("movies") final List<MovieSummaryDto> movies) {
        this.pageNumber = pageNumber;
        this.movies = movies;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public List<MovieSummaryDto> getMovies() {
        return movies;
    }
}

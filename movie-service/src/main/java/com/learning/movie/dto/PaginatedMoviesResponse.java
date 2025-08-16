package com.learning.movie.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class PaginatedMoviesResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int totalPages;
    private final List<MovieSummaryDto> movies;

    @JsonCreator
    public PaginatedMoviesResponse(@JsonProperty("totalPages") final int totalPages,
                                   @JsonProperty("movies") final List<MovieSummaryDto> movies) {
        this.totalPages = totalPages;
        this.movies = movies;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<MovieSummaryDto> getMovies() {
        return movies;
    }
}

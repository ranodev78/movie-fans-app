package com.learning.movie.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewlyReleasedMoviesResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer page;
    private final List<NewlyReleasedMovie> results;
    private final Integer totalPages;
    private final Integer totalResults;

    @JsonCreator
    public NewlyReleasedMoviesResponse(@JsonProperty("page") final Integer page,
                                       @JsonProperty("results") final List<NewlyReleasedMovie> results,
                                       @JsonProperty("total_pages") final Integer totalPages,
                                       @JsonProperty("total_results") final Integer totalResults) {
        this.page = page;
        this.results = results;
        this.totalPages = totalPages;
        this.totalResults = totalResults;
    }

    public Integer getPage() {
        return page;
    }

    public List<NewlyReleasedMovie> getResults() {
        return results;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Integer getTotalResults() {
        return totalResults;
    }
}

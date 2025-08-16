package com.learning.movie.dto.omdbapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OmdbApiPaginatedSearchResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("Search")
    private List<OmdbApiMovieSummary> search;

    @JsonProperty("totalResults")
    private Integer totalResults;

    @JsonProperty("Response")
    private Boolean response;

    public List<OmdbApiMovieSummary> getSearch() {
        return this.search;
    }

    public Integer getTotalResults() {
        return this.totalResults;
    }

    public Boolean getResponse() {
        return this.response;
    }
}

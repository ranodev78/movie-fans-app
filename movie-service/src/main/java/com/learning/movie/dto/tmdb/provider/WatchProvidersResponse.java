package com.learning.movie.dto.tmdb.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WatchProvidersResponse {
    private Long id;
    private Map<String, WatchProviderRegion> results;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, WatchProviderRegion> getResults() {
        return results;
    }

    public void setResults(Map<String, WatchProviderRegion> results) {
        this.results = results;
    }
}

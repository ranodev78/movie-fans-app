package com.learning.movie.dto.tmdb.review;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TmdbReviewResponse(
        int id,
        int page,
        List<TmdbReview> results,
        @JsonProperty("total_pages") int totalPages,
        @JsonProperty("total_results") int totalResults
) {}

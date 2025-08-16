package com.learning.movie.dto.tmdb.review;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record TmdbReview(
        String author,
        @JsonProperty("author_details") AuthorDetails authorDetails,
        String content,
        @JsonProperty("created_at")OffsetDateTime createdAt,
        String id,
        String url
) {}

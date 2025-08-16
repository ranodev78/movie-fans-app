package com.learning.movie.dto.tmdb.review;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorDetails(
        String name,
        String username,
        @JsonProperty("avatar_path") String avatarPath,
        Double rating
) {}

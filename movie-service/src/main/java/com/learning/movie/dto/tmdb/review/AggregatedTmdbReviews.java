package com.learning.movie.dto.tmdb.review;

import java.util.List;

public record AggregatedTmdbReviews(String movieName, List<String> reviews) {
}

package com.learning.movie.dto.tmdb.review;

import java.util.List;

public record TmdbReviewSummary(List<String> positives, List<String> negatives) {
}

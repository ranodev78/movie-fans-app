package com.learning.movie.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateMovieRatingRequest {

    @NotNull
    @Min(0)
    @Max(10)
    @Digits(integer = 2, fraction = 1)
    private final Double rating;

    public UpdateMovieRatingRequest(final Double rating) {
        this.rating = rating;
    }

    public Double getRating() {
        return this.rating;
    }
}

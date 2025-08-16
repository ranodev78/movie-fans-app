package com.learning.movie.exception;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public final class ErrorResponse {

    @NotNull
    @Min(1)
    private final List<String> errors;

    public ErrorResponse(final List<String> errors) {
        this.errors = errors;
    }
}

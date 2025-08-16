package com.learning.movie.exception;

import java.util.UUID;

public final class MovieNotFoundException extends RuntimeException {
    public MovieNotFoundException(final UUID movieId) {
        super("Could not find movie with ID: %d".formatted(movieId));
    }
}

package com.learning.movie.dto.subscription;

import jakarta.validation.constraints.NotBlank;

public final class StreamingReleaseSubscriptionRequest {

    @NotBlank
    private Long movieId;

    @NotBlank
    private String movieName;

    @NotBlank
    private StreamingPlatform streamingPlatform;

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public StreamingPlatform getStreamingPlatform() {
        return streamingPlatform;
    }

    public void setStreamingProvider(StreamingPlatform streamingPlatform) {
        this.streamingPlatform = streamingPlatform;
    }
}

package com.learning.movie.model.subscription;

import jakarta.validation.constraints.Email;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("movie_streaming_release_subscriptions")
public class MovieReleaseSubscription {

    @Id
    @Column("id")
    private Long subscriptionId;

    private String userId;

    @Email
    private String email;

    @Column("tmdb_movie_id")
    private Long tmdbMovieId;

    private String movieName;
    private LocalDateTime createdAt = LocalDateTime.now();

    public MovieReleaseSubscription(String userId, String email, Long tmdbMovieId, String movieName) {
        this.userId = userId;
        this.email = email;
        this.tmdbMovieId = tmdbMovieId;
        this.movieName = movieName;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getTmdbMovieId() {
        return tmdbMovieId;
    }

    public void setTmdbMovieId(Long tmdbMovieId) {
        this.tmdbMovieId = tmdbMovieId;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

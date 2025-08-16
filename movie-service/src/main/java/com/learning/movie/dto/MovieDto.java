package com.learning.movie.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Year;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieDto {
    private String movieId;

    @NotBlank
    private final String title;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Year releasedYear;

    private String director;
    private String genre;

    private final String ttId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal rentalCost;

    public MovieDto(final String movieId, final String title, final String ttId) {
        this.movieId = movieId;
        this.title = title;
        this.ttId = ttId;
    }

    // Getters
    public String getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public Year getReleasedYear() {
        return releasedYear;
    }

    public String getDirector() {
        return director;
    }

    public String getGenre() {
        return genre;
    }

    public String getTtId() {
        return ttId;
    }

    public BigDecimal getRentalCost() {
        return rentalCost;
    }

    // Setters
    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public void setReleasedYear(Year releasedYear) {
        this.releasedYear = releasedYear;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setRentalCost(BigDecimal rentalCost) {
        this.rentalCost = rentalCost;
    }
}

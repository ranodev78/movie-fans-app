package com.learning.movie.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateMovieRequest {

    @NotBlank
    private final String title;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Year releasedYear;

    private String director;
    private String genre;

    @NotBlank
    private final String ttId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal rentalCost;

    private Set<String> awards;

    @Digits(integer = 2, fraction = 1)
    private Double rating;

    @JsonCreator
    public CreateMovieRequest(@JsonProperty("title") final String title,
                              @JsonProperty("ttId") final String ttId) {
        this.title = title;
        this.ttId = ttId;
    }

    // Getters
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

    public Set<String> getAwards() {
        return awards;
    }

    public Double getRating() {
        return rating;
    }

    // Setters
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

    public void setAwards(Set<String> awards) {
        this.awards = awards;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}

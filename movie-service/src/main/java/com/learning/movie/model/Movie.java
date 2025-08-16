package com.learning.movie.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Set;

@Table("movie_details")
public class Movie {

    @Id
    @Column("id")
    private String movieId;

    @Column("title")
    private final String title;

    @Column("released_year")
    private Year releasedYear;

    private String director;
    private String genre;

    @Column("ttid")
    private final String ttId;

    @Column("rental_cost")
    private BigDecimal rentalCost;

    @Column("awards")
    private String awardsCsv;

    @Transient
    private Set<String> awards;

    private Double rating;

    public Movie(final String title, final String ttId) {
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

    public String getAwardsCsv() {
        return awardsCsv;
    }

    @Transient
    public Set<String> getAwards() {
        return awards;
    }

    public Double getRating() {
        return rating;
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

    public void setAwards(String awards) {
        this.awardsCsv = awards;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}

package com.learning.movie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.learning.movie.model.enums.FilmMediaType;

import java.io.Serializable;

public final class MovieSummaryDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private String year;

    @JsonProperty("Type")
    private FilmMediaType type;

    @JsonProperty("Poster")
    private String poster;

    public MovieSummaryDto(String title, String year, FilmMediaType type, String poster) {
        this.title = title;
        this.year = year;
        this.type = type;
        this.poster = poster;
    }

    public MovieSummaryDto() {}

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setType(FilmMediaType type) {
        this.type = type;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }
}

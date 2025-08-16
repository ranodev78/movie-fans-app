package com.learning.movie.dto.omdbapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.learning.movie.dto.omdbapi.deserializer.ActorsDeserializer;
import com.learning.movie.dto.omdbapi.deserializer.RuntimeDeserializer;
import com.learning.movie.dto.omdbapi.serializer.RuntimeSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class OmdbApiResponse {

    @JsonProperty("Title")
    private final String title;

    @JsonProperty("Year")
    private final String year;

    @JsonProperty("Released")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd MMM yyyy")
    private final LocalDate released;

    @JsonProperty("Runtime")
    @JsonDeserialize(using = RuntimeDeserializer.class)
    @JsonSerialize(using = RuntimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    private final Duration runtime;

    @JsonProperty("Genre")
    private final String genre;

    @JsonProperty("Director")
    private final String director;

    @JsonProperty("Writer")
    private final String writer;

    @JsonProperty("Actors")
    @JsonDeserialize(using = ActorsDeserializer.class)
    private final List<String> actors;

    @JsonProperty("Plot")
    private final String plot;

    @JsonProperty("Language")
    private final String language;

    @JsonProperty("Country")
    private final String country;

    @JsonProperty("Awards")
    private final String awards;

    @JsonProperty("Metascore")
    private final String metascore;

    private final String imdbRating;
    private final String imdbVotes;
    private final String imdbID;

    @JsonProperty("Response")
    private final String response;

    @JsonCreator
    public OmdbApiResponse(@JsonProperty("Title") final String title,
                           @JsonProperty("Year") final String year,
                           @JsonProperty("Released") final LocalDate released,
                           @JsonProperty("Runtime") final Duration runtime,
                           @JsonProperty("Genre") final String genre,
                           @JsonProperty("Director") final String director,
                           @JsonProperty("Writer") final String writer,
                           @JsonProperty("Actors") final List<String> actors,
                           @JsonProperty("Plot") final String plot,
                           @JsonProperty("Language") final String language,
                           @JsonProperty("Country") final String country,
                           @JsonProperty("Awards") final String awards,
                           @JsonProperty("Metascore") final String metascore,
                           @JsonProperty("imdbRating") final String imdbRating,
                           @JsonProperty("imdbVotes") final String imdbVotes,
                           @JsonProperty("imdbID") final String imdbID,
                           @JsonProperty("Response") final String response) {
        this.title = title;
        this.year = year;
        this.released = released;
        this.runtime = runtime;
        this.genre = genre;
        this.director = director;
        this.writer = writer;
        this.actors = actors;
        this.plot = plot;
        this.language = language;
        this.country = country;
        this.awards = awards;
        this.metascore = metascore;
        this.imdbRating = imdbRating;
        this.imdbVotes = imdbVotes;
        this.imdbID = imdbID;
        this.response = response;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public LocalDate getReleased() {
        return released;
    }

    public Duration getRuntime() {
        return runtime;
    }

    public String getGenre() {
        return genre;
    }

    public String getDirector() {
        return director;
    }

    public String getWriter() {
        return writer;
    }

    public List<String> getActors() {
        return actors;
    }

    public String getPlot() {
        return plot;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    public String getAwards() {
        return awards;
    }

    public String getMetascore() {
        return metascore;
    }

    public String getImdbRating() {
        return imdbRating;
    }

    public String getImdbVotes() {
        return imdbVotes;
    }

    public String getImdbID() {
        return imdbID;
    }

    public String getResponse() {
        return response;
    }
}

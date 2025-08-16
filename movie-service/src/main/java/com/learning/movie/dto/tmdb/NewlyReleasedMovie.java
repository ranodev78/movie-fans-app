package com.learning.movie.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.learning.movie.dto.tmdb.deserializer.GenreSetDeserializer;
import com.learning.movie.dto.tmdb.serializer.GenreSetSerializer;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewlyReleasedMovie implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Boolean adult;
    private final Long id;
    private final String originalLanguage;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate releaseDate;

    @JsonDeserialize(using = GenreSetDeserializer.class)
    @JsonSerialize(using = GenreSetSerializer.class)
    private final Set<Genre> genreIds;

    private final String overview;
    private final Double popularity;
    private final String title;
    private final Double voteAverage;
    private final Integer voteCount;
    private final String posterPath;

    @JsonCreator
    public NewlyReleasedMovie(@JsonProperty("adult") final Boolean adult,
                              @JsonProperty("id") final Long id,
                              @JsonProperty("original_language") final String originalLanguage,
                              @JsonProperty("release_date") final LocalDate releaseDate,
                              @JsonProperty("genre_ids") final Set<Genre> genreIds,
                              @JsonProperty("overview") final String overview,
                              @JsonProperty("popularity") final Double popularity,
                              @JsonProperty("title") final String title,
                              @JsonProperty("vote_average") final Double voteAverage,
                              @JsonProperty("vote_count") final Integer voteCount,
                              @JsonProperty("poster_path") final String posterPath) {
        this.adult = adult;
        this.id = id;
        this.originalLanguage = originalLanguage;
        this.releaseDate = releaseDate;
        this.genreIds = genreIds;
        this.overview = overview;
        this.popularity = popularity;
        this.title = title;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.posterPath = posterPath;
    }

    public Boolean getAdult() {
        return adult;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public Set<Genre> getGenreIds() {
        return genreIds;
    }

    public String getOverview() {
        return overview;
    }

    public Double getPopularity() {
        return popularity;
    }

    public String getTitle() {
        return title;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public String getPosterPath() {
        return posterPath;
    }
}

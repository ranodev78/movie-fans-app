package com.learning.movie.mapper;

import com.learning.movie.dto.CreateMovieRequest;
import com.learning.movie.dto.MovieDto;
import com.learning.movie.dto.MovieSummaryDto;
import com.learning.movie.dto.omdbapi.OmdbApiMovieSummary;
import com.learning.movie.dto.omdbapi.OmdbApiResponse;
import com.learning.movie.model.Movie;
import org.springframework.util.CollectionUtils;

import java.time.Year;
import java.util.UUID;

public final class MovieMapper {
    private static final String COMMA_DELIMITER = ",";

    private MovieMapper() {}

    public static Movie createMovieRequestToMovieEntity(CreateMovieRequest createMovieRequest) {
        final Movie movieToSave = new Movie(createMovieRequest.getTitle(), createMovieRequest.getTtId());
        movieToSave.setReleasedYear(createMovieRequest.getReleasedYear());
        movieToSave.setDirector(createMovieRequest.getDirector());
        movieToSave.setGenre(createMovieRequest.getGenre());
        movieToSave.setRentalCost(createMovieRequest.getRentalCost());

        movieToSave.setAwards(
                CollectionUtils.isEmpty(createMovieRequest.getAwards())
                        ? null
                        : String.join(COMMA_DELIMITER, createMovieRequest.getAwards()));

        movieToSave.setRating(createMovieRequest.getRating());

        return movieToSave;
    }

    public static MovieDto movieEntityToMovieResponseDTO(Movie movie) {
        final MovieDto movieDto = new MovieDto(movie.getMovieId(), movie.getTitle(), movie.getTtId());
        movieDto.setMovieId(movie.getMovieId());
        movieDto.setReleasedYear(movie.getReleasedYear());
        movieDto.setDirector(movie.getDirector());
        movieDto.setGenre(movie.getGenre());
        movieDto.setRentalCost(movie.getRentalCost());

        return movieDto;
    }

    public static Movie fromOmdbApiMoveToEntity(OmdbApiResponse omdbApiMovie) {
        final Movie movieToSave = new Movie(omdbApiMovie.getTitle(), omdbApiMovie.getImdbID());
        movieToSave.setMovieId(UUID.randomUUID().toString());
        movieToSave.setReleasedYear(Year.parse(omdbApiMovie.getYear()));
        movieToSave.setDirector(omdbApiMovie.getDirector());
        movieToSave.setGenre(omdbApiMovie.getGenre());

        return movieToSave;
    }

    public static MovieSummaryDto fromOmdbApiMovieSummary(OmdbApiMovieSummary omdbApiMovieSummary) {
        return new MovieSummaryDto(omdbApiMovieSummary.getTitle(), omdbApiMovieSummary.getYear(),
                                   omdbApiMovieSummary.getType(), omdbApiMovieSummary.getPoster());
    }
}

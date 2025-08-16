package com.learning.movie.dto.tmdb;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Genre {
    ACTION(28, "Action"),
    ADVENTURE(12, "Adventure"),
    ANIMATION(16, "Animation"),
    COMEDY(35, "Comedy"),
    CRIME(80, "Crime"),
    DOCUMENTARY(99, "Documentary"),
    DRAMA(18, "Drama"),
    FAMILY(10751, "Family"),
    FANTASY(14, "Fantasy"),
    HISTORY(36, "History"),
    HORROR(27, "Horror"),
    MUSIC(10402, "Music"),
    MYSTERY(9648, "Mystery"),
    ROMANCE(10749, "Romance"),
    SCIENCE_FICTION(878, "Science Fiction"),
    TV_MOVIE(10770, "TV Movie"),
    THRILLER(53, "Thriller"),
    WAR(10752, "War"),
    WESTERN(37, "Western");

    private static final Map<Integer, Genre> ID_MAP = Arrays
            .stream(Genre.values())
            .collect(Collectors.toMap(Genre::getId, Function.identity()));

    private static final Map<String, Genre> NAME_MAP = Arrays
            .stream(Genre.values())
            .collect(Collectors.toMap(Genre::getDisplayName, Function.identity()));

    private final int id;
    private final String displayName;

    Genre(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<Genre> fromId(int id) {
        return Optional.ofNullable(ID_MAP.get(id));
    }

    public static Optional<Genre> fromName(String name) {
        return Optional.ofNullable(NAME_MAP.get(name));
    }

    public static List<String> resolveDisplayNames(List<Integer> ids) {
        return ids.stream()
                .map(Genre::fromId)
                .flatMap(Optional::stream)
                .map(Genre::getDisplayName)
                .toList();
    }
}

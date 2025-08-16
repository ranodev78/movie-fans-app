package com.learning.movie.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum FilmMediaType {

    MOVIE("movie"),
    SERIES("series"),
    EPISODE("episode");

    private static final Map<String, FilmMediaType> VALUE_TO_ENUM;

    static {
        VALUE_TO_ENUM = new HashMap<>(FilmMediaType.values().length);

        for (FilmMediaType type : FilmMediaType.values()) {
            VALUE_TO_ENUM.put(type.value, type);
        }
    }

    private final String value;

    FilmMediaType(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FilmMediaType fromValue(final String value) {
        return VALUE_TO_ENUM.get(value);
    }
}

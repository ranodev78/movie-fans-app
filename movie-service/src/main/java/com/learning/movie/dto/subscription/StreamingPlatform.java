package com.learning.movie.dto.subscription;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum StreamingPlatform {
    APPLE_TV("Apple TV"),
    DISNEY_PLUS("Disney Plus"),
    HBO_MAX("HBO Max"),
    HULU("Hulu"),
    NETFLIX("Netflix"),
    PARAMOUNT("Paramount Plus"),
    PEACOCK("Peacock"),
    PRIME("Amazon Prime");

    private static final Map<String, StreamingPlatform> DISPLAY_NAME_MAP = Arrays
            .stream(StreamingPlatform.values())
            .collect(Collectors.toMap(StreamingPlatform::getDisplayName, Function.identity()));

    private final String displayName;

    StreamingPlatform(final String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return this.displayName;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StreamingPlatform fromDisplayName(final String displayName) {
        return DISPLAY_NAME_MAP.get(displayName);
    }
}

package com.learning.movie.dto.omdbapi.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;

public final class RuntimeDeserializer extends JsonDeserializer<Duration> {
    private static final String MINUTE_SUFFIX = " min";
    private static final int MINUTE_SUFFIX_LENGTH = MINUTE_SUFFIX.length();

    @Override
    public Duration deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String durationText = jsonParser.getText().trim();
        if (durationText.endsWith(MINUTE_SUFFIX)) {
            // Substring and trim text to get the numerical minutes
            durationText = durationText.substring(0, durationText.length() - MINUTE_SUFFIX_LENGTH).trim();
        } else {
            throw new IllegalArgumentException("JSON text for \"Runtime\" did not have the expected \"min\" suffix");
        }

        return Duration.ofMinutes(Long.parseLong(durationText));
    }
}

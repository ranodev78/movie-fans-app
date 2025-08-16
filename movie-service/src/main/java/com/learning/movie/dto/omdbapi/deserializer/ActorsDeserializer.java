package com.learning.movie.dto.omdbapi.deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ActorsDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        final String actorsText = jsonParser.getText().trim();

        if (actorsText.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(actorsText.split(","))
                     .map(String::trim)
                     .toList();
    }
}

package com.learning.movie.dto.tmdb.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.learning.movie.dto.tmdb.Genre;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public final class GenreSetDeserializer extends JsonDeserializer<Set<Genre>> {

    @Override
    public Set<Genre> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        final EnumSet<Genre> genres = EnumSet.noneOf(Genre.class);

        for (JsonNode item : node) {
            if (item.isInt()) {
                Genre.fromId(item.intValue()).ifPresent(genres::add);
            } else if (item.isTextual()) {
                Genre.fromName(item.textValue()).ifPresent(genres::add);
            }
        }

        return genres;
    }
}

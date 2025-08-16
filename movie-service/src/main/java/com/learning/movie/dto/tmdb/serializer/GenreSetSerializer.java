package com.learning.movie.dto.tmdb.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.learning.movie.dto.tmdb.Genre;

import java.io.IOException;
import java.util.Set;

public final class GenreSetSerializer extends JsonSerializer<Set<Genre>> {

    @Override
    public void serialize(Set<Genre> genres, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartArray();

        for (Genre genre : genres) {
            jsonGenerator.writeString(genre.getDisplayName());
        }

        jsonGenerator.writeEndArray();
    }
}

package com.learning.movie.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Converter
public final class StringSetConverter implements AttributeConverter<Set<String>, String> {

    @Override
    public String convertToDatabaseColumn(Set<String> strings) {
        return strings == null || strings.isEmpty() ? null : String.join(",", strings);
    }

    @Override
    public Set<String> convertToEntityAttribute(String s) {
        return s == null || s.isBlank() ? Collections.emptySet() : new HashSet<>(List.of(s.split(",")));
    }
}

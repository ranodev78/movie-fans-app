package com.learning.movie.service.preprocessor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SearchParser {
    private static final Pattern YEAR_PATTERN = Pattern.compile("\\b(19|20)\\d{2}\\b");

    public static final String PARSE_KEY_QUERY = "query";
    public static final String PARSE_KEY_YEAR = "year";

    public static Map<String, String> parseSearch(String text) {
        final Map<String, String> params = new HashMap<>();

        if (text == null || text.isBlank()) {
            params.put(PARSE_KEY_QUERY, "");
            return params;
        }

        final Matcher regexMatcher = YEAR_PATTERN.matcher(text);
        String year;

        if (regexMatcher.find()) {
            year = regexMatcher.group();
            text = text.replace(year, "").trim();
            params.put(PARSE_KEY_YEAR, year);
        }

        params.put(PARSE_KEY_QUERY, text);

        return params;
    }
}

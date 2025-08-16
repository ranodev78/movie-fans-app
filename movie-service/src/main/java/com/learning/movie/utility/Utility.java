package com.learning.movie.utility;

public final class Utility {
    private Utility() {}

    public static int calculatePageCount(final int totalResults, final int pageSize) {
        return pageSize > 0 ? (int) Math.ceil((double) totalResults / pageSize) : 1;
    }
}

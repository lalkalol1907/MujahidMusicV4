package com.lalkalol.mujahid.util;

import java.util.regex.Pattern;

public final class Identifiers {
    private static final Pattern URL = Pattern.compile("^https?://.*", Pattern.CASE_INSENSITIVE);

    private Identifiers() {
    }

    public static String fromQuery(String query) {
        String trimmed = query.trim();
        if (URL.matcher(trimmed).matches()) {
            return trimmed;
        }
        return "ytsearch:" + trimmed;
    }
}

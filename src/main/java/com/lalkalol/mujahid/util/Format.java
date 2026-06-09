package com.lalkalol.mujahid.util;

import java.time.Duration;

public final class Format {
    private Format() {
    }

    public static String duration(long ms) {
        return duration(ms, false);
    }

    public static String duration(long ms, boolean isStream) {
        if (isStream) {
            return "LIVE";
        }
        Duration d = Duration.ofMillis(ms);
        long hours = d.toHours();
        int minutes = d.toMinutesPart();
        int seconds = d.toSecondsPart();
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    public static String progressBar(long position, long total) {
        return progressBar(position, total, 18);
    }

    public static String progressBar(long position, long total, int length) {
        if (total <= 0) {
            return "🔘" + "▬".repeat(length - 1);
        }
        double ratio = Math.clamp(position / (double) total, 0.0, 1.0);
        int pos = (int) (ratio * (length - 1));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(i == pos ? "🔘" : "▬");
        }
        return sb.toString();
    }

    public static Long parseTimestamp(String input) {
        String[] parts = input.trim().split(":");
        for (String part : parts) {
            if (part.isBlank()) {
                return null;
            }
            try {
                Long.parseLong(part);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        long seconds;
        switch (parts.length) {
            case 1 -> seconds = Long.parseLong(parts[0]);
            case 2 -> seconds = Long.parseLong(parts[0]) * 60 + Long.parseLong(parts[1]);
            case 3 -> seconds = Long.parseLong(parts[0]) * 3600
                    + Long.parseLong(parts[1]) * 60
                    + Long.parseLong(parts[2]);
            default -> {
                return null;
            }
        }
        return seconds * 1000;
    }
}

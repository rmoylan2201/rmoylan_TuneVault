package com.example.tunevaultfx.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Display-oriented snapshot of a row in {@code user_genre_discovery}.
 */
public record UserGenreDiscoverySummary(
        String topGenre,
        String secondGenre,
        String thirdGenre,
        String quizMode) {

    public boolean isEmpty() {
        return blendParts().isEmpty();
    }

    public List<String> blendParts() {
        List<String> out = new ArrayList<>(3);
        addIfPresent(out, topGenre);
        addIfPresent(out, secondGenre);
        addIfPresent(out, thirdGenre);
        return out;
    }

    public String blendLine() {
        return String.join(" \u00B7 ", blendParts());
    }

    /** e.g. "Quick" / "Full" for captions */
    public String quizModeLabel() {
        if (quizMode == null || quizMode.isBlank()) {
            return "";
        }
        return switch (quizMode.trim().toUpperCase()) {
            case "QUICK" -> "Quick";
            case "FULL" -> "Full";
            default -> quizMode.trim();
        };
    }

    private static void addIfPresent(List<String> out, String s) {
        if (s != null && !s.isBlank()) {
            out.add(s.trim());
        }
    }
}

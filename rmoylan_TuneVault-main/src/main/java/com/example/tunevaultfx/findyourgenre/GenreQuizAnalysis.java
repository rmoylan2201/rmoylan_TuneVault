package com.example.tunevaultfx.findyourgenre;

import java.util.List;
import java.util.Map;

/**
 * Analyzes raw questionnaire scores into display copy, ranked genres, and recommendation boosts.
 *
 * <p>Separates “analysis” from UI wiring so persistence ({@link com.example.tunevaultfx.db.UserGenreDiscoveryDAO})
 * and {@link com.example.tunevaultfx.wrapped.WrappedStatsService} can describe the same logic consistently.</p>
 */
public final class GenreQuizAnalysis {

    private GenreQuizAnalysis() {}

    /**
     * @param rawScores genre key → weighted tally from each answered question (may be empty)
     */
    public static AnalysisResult analyze(Map<String, Integer> rawScores) {
        if (rawScores == null || rawScores.isEmpty()) {
            return new AnalysisResult(
                    List.of(),
                    "We need a clearer read",
                    "Try again and tap the answers that feel closest today — or skip less so we get a stronger signal.",
                    Map.of());
        }

        List<String> top = GenreQuiz.topGenres(rawScores, 3);
        Map<String, Double> boosts = GenreQuiz.toRecommendationBoosts(rawScores);

        String headline;
        String summary;
        if (top.isEmpty()) {
            headline = "We need a clearer read";
            summary =
                    "Try again and tap the answers that feel closest today — or skip less so we get a stronger signal.";
        } else if (top.size() == 1) {
            headline = top.get(0);
            summary =
                    "We’ll steer playlists, suggestions, and search toward this world. Change your mind anytime with Try again.";
        } else {
            headline = "Your blend";
            summary =
                    "Recommendations will mix these together. Nothing is locked — redo the quiz whenever your taste shifts.";
        }

        return new AnalysisResult(top, headline, summary, boosts);
    }

    /**
     * Outcome of analyzing quiz responses: human-readable strings plus boosts for the engine.
     *
     * @param topGenresDisplay up to three title-cased genres for chips / headlines
     * @param headline         primary result title
     * @param summaryForUi     supporting explanation shown under the headline
     * @param recommendationBoosts normalized genre keys → weights for {@link com.example.tunevaultfx.recommendation.RecommendationEngine}
     */
    public record AnalysisResult(
            List<String> topGenresDisplay,
            String headline,
            String summaryForUi,
            Map<String, Double> recommendationBoosts) {

        public boolean isEmptySignal() {
            return topGenresDisplay == null || topGenresDisplay.isEmpty();
        }
    }
}

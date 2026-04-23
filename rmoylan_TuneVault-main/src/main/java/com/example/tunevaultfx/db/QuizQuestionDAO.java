package com.example.tunevaultfx.db;

import com.example.tunevaultfx.findyourgenre.GenreQuiz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads genre-discovery quiz questions and their answers from the database.
 *
 * <p>Tables used: {@code quiz_question} and {@code quiz_answer}.
 * Each answer row carries a {@code genre_name} that is matched against
 * {@link com.example.tunevaultfx.core.Song#genre()} at scoring time,
 * and a {@code weight} (1 = normal, 2 = double-scored).</p>
 *
 * <p>The QUICK mode returns only rows where {@code quiz_question.quiz_mode = 'QUICK'}
 * (display_order 1-5). The FULL mode returns all active rows ordered by
 * {@code display_order}.</p>
 */
public final class QuizQuestionDAO {

    /**
     * Fetches quiz questions for the requested mode.
     *
     * @param mode {@link GenreQuiz.QuizMode#QUICK} returns only QUICK-flagged questions;
     *             {@link GenreQuiz.QuizMode#FULL} returns all active questions.
     * @return ordered list of {@link GenreQuiz.Question}, never null, may be empty if the
     *         quiz tables have not been seeded yet.
     * @throws SQLException on any DB error
     */
    public List<GenreQuiz.Question> loadQuestions(GenreQuiz.QuizMode mode) throws SQLException {
        // 1. Load question rows
        String questionSql = buildQuestionSql(mode);
        List<QuestionRow> rows = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(questionSql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new QuestionRow(rs.getInt("question_id"), rs.getString("prompt")));
                }
            }
        }

        if (rows.isEmpty()) {
            return List.of();
        }

        // 2. Load answers for all fetched question IDs in one query
        Map<Integer, List<AnswerRow>> answersByQuestion = loadAnswers(rows);

        // 3. Assemble GenreQuiz.Question records
        List<GenreQuiz.Question> questions = new ArrayList<>(rows.size());
        for (QuestionRow qr : rows) {
            List<AnswerRow> answers = answersByQuestion.getOrDefault(qr.id, List.of());
            // We need exactly 4 answers; skip questions that are incomplete
            if (answers.size() < 4) {
                continue;
            }
            // answers are already sorted by answer_order (from loadAnswers)
            AnswerRow a1 = answers.get(0);
            AnswerRow a2 = answers.get(1);
            AnswerRow a3 = answers.get(2);
            AnswerRow a4 = answers.get(3);

            questions.add(new GenreQuiz.Question(
                    qr.prompt,
                    a1.text, a2.text, a3.text, a4.text,
                    a1.genreName, a2.genreName, a3.genreName, a4.genreName,
                    a1.weight, a2.weight, a3.weight, a4.weight
            ));
        }
        return List.copyOf(questions);
    }

    private static String buildQuestionSql(GenreQuiz.QuizMode mode) {
        if (mode == GenreQuiz.QuizMode.QUICK) {
            return """
                    SELECT question_id, prompt
                    FROM quiz_question
                    WHERE is_active = 1 AND quiz_mode = 'QUICK'
                    ORDER BY display_order ASC
                    """;
        }
        // FULL — all active questions regardless of mode flag
        return """
                SELECT question_id, prompt
                FROM quiz_question
                WHERE is_active = 1
                ORDER BY display_order ASC
                """;
    }

    /**
     * Bulk-loads answers for all question IDs in a single round-trip.
     * Returns a map keyed by question_id, values sorted by answer_order.
     */
    private static Map<Integer, List<AnswerRow>> loadAnswers(List<QuestionRow> questions)
            throws SQLException {
        // Build placeholder list  (?,?,?…)
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < questions.size(); i++) {
            if (i > 0) placeholders.append(',');
            placeholders.append('?');
        }

        String sql = "SELECT question_id, answer_text, genre_name, weight, answer_order "
                + "FROM quiz_answer "
                + "WHERE question_id IN (" + placeholders + ") "
                + "ORDER BY question_id ASC, answer_order ASC";

        Map<Integer, List<AnswerRow>> map = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < questions.size(); i++) {
                ps.setInt(i + 1, questions.get(i).id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int qid = rs.getInt("question_id");
                    map.computeIfAbsent(qid, k -> new ArrayList<>())
                            .add(new AnswerRow(
                                    rs.getString("answer_text"),
                                    rs.getString("genre_name"),
                                    Math.max(1, rs.getInt("weight"))
                            ));
                }
            }
        }
        return map;
    }

    // -------------------------------------------------------------------------
    // Private data holders
    // -------------------------------------------------------------------------

    private record QuestionRow(int id, String prompt) {}

    private record AnswerRow(String text, String genreName, int weight) {}
}

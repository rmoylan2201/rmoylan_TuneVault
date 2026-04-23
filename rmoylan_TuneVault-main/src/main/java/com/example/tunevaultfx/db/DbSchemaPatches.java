package com.example.tunevaultfx.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Idempotent patches for databases created before newer columns were added to {@code schema.sql}.
 */
public final class DbSchemaPatches {

    private DbSchemaPatches() {}

    /** Ensures {@code app_user} can store the profile avatar media key. */
    public static void ensureAppUserProfileMediaColumns() {
        try (Connection conn = DBConnection.getConnection()) {
            ensureColumn(
                    conn,
                    "app_user",
                    "profile_avatar_key",
                    "VARCHAR(512) NULL COMMENT 'Relative path under ~/.tunevaultfx/profile-media'");
            ensureColumn(
                    conn,
                    "playlist",
                    "pin_order",
                    "TINYINT UNSIGNED NULL COMMENT '1–3 user pins; NULL = unpinned'");
            ensureColumn(
                    conn,
                    "playlist",
                    "is_public",
                    "TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1 = visible on profile & search (non-system only)'");
            ensureUserFollowTable(conn);
            ensureUserFollowsArtistTable(conn);
            ensureQuizTables(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates {@code quiz_question} and {@code quiz_answer} if absent and seeds
     * the default question bank when the tables are newly created.
     */
    private static void ensureQuizTables(Connection conn) throws SQLException {
        boolean questionTableCreated = createQuizQuestionTable(conn);
        boolean answerTableCreated   = createQuizAnswerTable(conn);
        if (questionTableCreated && answerTableCreated) {
            seedQuizData(conn);
        }
    }

    private static boolean createQuizQuestionTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS quiz_question (
                    question_id   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    prompt        VARCHAR(512) NOT NULL,
                    quiz_mode     ENUM('QUICK','FULL') NOT NULL DEFAULT 'FULL',
                    display_order TINYINT UNSIGNED NOT NULL DEFAULT 0,
                    is_active     TINYINT(1) NOT NULL DEFAULT 1,
                    CONSTRAINT uq_quiz_question_order UNIQUE (display_order)
                ) ENGINE=InnoDB
                """;
        try (Statement st = conn.createStatement()) {
            // executeUpdate returns 0 for DDL; use a metadata check to detect creation
            st.executeUpdate(sql);
        }
        // Return true if no rows exist yet (i.e., we just created it or it was empty)
        try (Statement st = conn.createStatement();
             var rs = st.executeQuery("SELECT COUNT(*) FROM quiz_question")) {
            return rs.next() && rs.getLong(1) == 0;
        }
    }

    private static boolean createQuizAnswerTable(Connection conn) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS quiz_answer (
                    answer_id    INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                    question_id  INT UNSIGNED NOT NULL,
                    answer_text  VARCHAR(255) NOT NULL,
                    genre_name   VARCHAR(128) NOT NULL,
                    weight       TINYINT UNSIGNED NOT NULL DEFAULT 1,
                    answer_order TINYINT UNSIGNED NOT NULL DEFAULT 1,
                    CONSTRAINT fk_qa_question FOREIGN KEY (question_id)
                        REFERENCES quiz_question (question_id)
                        ON DELETE CASCADE ON UPDATE CASCADE,
                    INDEX idx_qa_question (question_id)
                ) ENGINE=InnoDB
                """;
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
        try (Statement st = conn.createStatement();
             var rs = st.executeQuery("SELECT COUNT(*) FROM quiz_answer")) {
            return rs.next() && rs.getLong(1) == 0;
        }
    }

    /**
     * Inserts the default 10-question bank (5 QUICK + 5 FULL) matching the
     * hardcoded bank in GenreQuiz.java.  Only called when tables were just created.
     */
    private static void seedQuizData(Connection conn) throws SQLException {
        // Questions
        String qSql = "INSERT INTO quiz_question (question_id, prompt, quiz_mode, display_order, is_active) VALUES (?,?,?,?,1)";
        Object[][] questions = {
                {1, "What energy do you want from music right now?", "QUICK", 1},
                {2, "What hooks you first?",                         "QUICK", 2},
                {3, "Where do you listen most?",                     "QUICK", 3},
                {4, "Pick a sound \"colour\":",                      "QUICK", 4},
                {5, "Old or new sounds?",                            "QUICK", 5},
                {6, "Song structure you like:",                      "FULL",  6},
                {7, "How important are vocals?",                     "FULL",  7},
                {8, "Social vibe when you press play:",              "FULL",  8},
                {9, "Emotional lane today:",                         "FULL",  9},
                {10,"Secret-weapon instrument:",                     "FULL", 10},
        };
        try (PreparedStatement ps = conn.prepareStatement(qSql)) {
            for (Object[] row : questions) {
                ps.setInt(1, (int) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setInt(4, (int) row[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }

        // Answers: {questionId, text, genreName, weight, answerOrder}
        String aSql = "INSERT INTO quiz_answer (question_id, answer_text, genre_name, weight, answer_order) VALUES (?,?,?,?,?)";
        Object[][] answers = {
                // Q1 — energy
                {1, "Soft & calm",              "Classical",  1, 1},
                {1, "Steady groove",            "R&B",        1, 2},
                {1, "Up & dancey",              "Pop",        2, 3},
                {1, "Loud & intense",           "Metal",      2, 4},
                // Q2 — entry point
                {2, "The words",                "Folk",       1, 1},
                {2, "The rhythm",               "Hip Hop",    1, 2},
                {2, "The melody",               "Pop",        1, 3},
                {2, "The atmosphere",           "Electronic", 1, 4},
                // Q3 — space
                {3, "Cozy at home",             "Indie",      1, 1},
                {3, "Night drive",              "Rock",       1, 2},
                {3, "With friends",             "Pop",        1, 3},
                {3, "Deep focus",               "Electronic", 1, 4},
                // Q4 — colour
                {4, "Warm & smoky",             "Jazz",       1, 1},
                {4, "Cool & smooth",            "R&B",        1, 2},
                {4, "Bright & sparkly",         "Pop",        1, 3},
                {4, "Raw & organic",            "Folk",       1, 4},
                // Q5 — era
                {5, "Timeless classics",        "Classical",  1, 1},
                {5, "Throwback eras",           "Rock",       1, 2},
                {5, "Fresh releases",           "Pop",        1, 3},
                {5, "Mix it all",               "Electronic", 1, 4},
                // Q6 — structure (weighted)
                {6, "Catchy & tight",           "Pop",        2, 1},
                {6, "Room to breathe",          "Jazz",       1, 2},
                {6, "Long builds",              "Electronic", 2, 3},
                {6, "Loops that hit",           "Hip Hop",    1, 4},
                // Q7 — vocals
                {7, "Sing-along essential",     "Pop",        1, 1},
                {7, "Nice when they're there",  "Indie",      1, 2},
                {7, "Background is fine",       "Rock",       1, 3},
                {7, "Instrumentals rule",       "Electronic", 1, 4},
                // Q8 — social
                {8, "Solo reset",               "Classical",  1, 1},
                {8, "Small hangout",            "Indie",      1, 2},
                {8, "Big crowd energy",         "Rock",       1, 3},
                {8, "Study / work flow",        "Electronic", 1, 4},
                // Q9 — emotion
                {9, "Happy boost",              "Pop",        1, 1},
                {9, "Soft & reflective",        "R&B",        1, 2},
                {9, "Angry catharsis",          "Metal",      1, 3},
                {9, "Hopeful glow",             "Indie",      1, 4},
                // Q10 — instrument
                {10,"Strings",                  "Classical",  1, 1},
                {10,"Drums & bass",             "Hip Hop",    1, 2},
                {10,"Synths & pads",            "Electronic", 1, 3},
                {10,"Guitar forward",           "Rock",       1, 4},
        };
        try (PreparedStatement ps = conn.prepareStatement(aSql)) {
            for (Object[] row : answers) {
                ps.setInt(1, (int) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setInt(4, (int) row[3]);
                ps.setInt(5, (int) row[4]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void ensureUserFollowTable(Connection conn) throws SQLException {
        String sql =
                """
                CREATE TABLE IF NOT EXISTS user_follow (
                    follower_user_id INT UNSIGNED NOT NULL,
                    followee_user_id INT UNSIGNED NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (follower_user_id, followee_user_id),
                    CONSTRAINT fk_user_follow_follower FOREIGN KEY (follower_user_id) REFERENCES app_user (user_id)
                        ON DELETE CASCADE ON UPDATE CASCADE,
                    CONSTRAINT fk_user_follow_followee FOREIGN KEY (followee_user_id) REFERENCES app_user (user_id)
                        ON DELETE CASCADE ON UPDATE CASCADE,
                    INDEX idx_user_follow_followee (followee_user_id)
                ) ENGINE=InnoDB
                """;
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private static void ensureUserFollowsArtistTable(Connection conn) throws SQLException {
        String sql =
                """
                CREATE TABLE IF NOT EXISTS user_follows_artist (
                    user_id INT UNSIGNED NOT NULL,
                    artist_id INT UNSIGNED NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (user_id, artist_id),
                    CONSTRAINT fk_ufa_user FOREIGN KEY (user_id) REFERENCES app_user (user_id)
                        ON DELETE CASCADE ON UPDATE CASCADE,
                    CONSTRAINT fk_ufa_artist FOREIGN KEY (artist_id) REFERENCES artist (artist_id)
                        ON DELETE CASCADE ON UPDATE CASCADE,
                    INDEX idx_ufa_artist (artist_id)
                ) ENGINE=InnoDB
                """;
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private static void ensureColumn(Connection conn, String table, String column, String ddlTail)
            throws SQLException {
        if (columnExists(conn, table, column)) {
            return;
        }
        String sql = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + ddlTail;
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            if (isDuplicateColumn(e)) {
                return;
            }
            throw e;
        }
    }

    private static boolean columnExists(Connection conn, String table, String column)
            throws SQLException {
        String sql =
                """
                SELECT 1 FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                LIMIT 1
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean isDuplicateColumn(SQLException e) {
        if (e.getMessage() != null && e.getMessage().contains("Duplicate column name")) {
            return true;
        }
        return "42S21".equals(e.getSQLState()) && e.getErrorCode() == 1060;
    }
}

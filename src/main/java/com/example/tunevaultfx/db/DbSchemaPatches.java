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
        } catch (SQLException e) {
            e.printStackTrace();
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

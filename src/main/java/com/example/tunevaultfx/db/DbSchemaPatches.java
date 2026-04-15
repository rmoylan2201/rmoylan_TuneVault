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
        } catch (SQLException e) {
            e.printStackTrace();
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

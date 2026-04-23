package com.example.tunevaultfx.profile;

import java.sql.SQLException;

/** User-facing copy when genre discovery metadata cannot be loaded. */
final class ProfileGenreMessages {

    private ProfileGenreMessages() {}

    static String loadFailureHint(SQLException e) {
        if (isMissingGenreTable(e)) {
            return "Your MySQL database is missing the user_genre_discovery table (or it failed to open). "
                    + "Run the CREATE TABLE block for user_genre_discovery from schema.sql, then open Profile again. "
                    + "After that, run Find Your Genre once from the sidebar to fill this section.";
        }
        String msg = e.getMessage();
        if (msg != null && !msg.isBlank()) {
            String shortMsg = msg.length() > 220 ? msg.substring(0, 217) + "\u2026" : msg;
            return "Genre blend could not be read: " + shortMsg;
        }
        return "Genre blend could not be read. Check the database connection and that schema.sql is applied.";
    }

    private static boolean isMissingGenreTable(SQLException e) {
        if ("42S02".equals(e.getSQLState())) {
            return true;
        }
        String m = e.getMessage();
        if (m == null) {
            return false;
        }
        return m.contains("user_genre_discovery")
                && (m.contains("doesn't exist") || m.contains("does not exist") || m.contains("Unknown table"));
    }
}

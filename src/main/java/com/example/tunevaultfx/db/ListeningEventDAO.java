package com.example.tunevaultfx.db;

import com.example.tunevaultfx.core.Song;

import java.sql.*;

/**
 * Saves and updates listening analytics in real time.
 */
public class ListeningEventDAO {

    public Integer startListeningSession(String username, Song song) {
        if (username == null || username.isBlank() || song == null || song.songId() <= 0) {
            return null;
        }

        try {
            Integer userId = findUserIdByUsername(username);
            if (userId == null) {
                return null;
            }

            int duration = Math.max(song.durationSeconds(), 0);

            String sql = """
                INSERT INTO listening_event (
                    user_id,
                    song_id,
                    action_type,
                    played_seconds,
                    song_duration_seconds,
                    completion_ratio,
                    count_as_play
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, song.songId());
                stmt.setString(3, "PLAY");
                stmt.setInt(4, 0);
                stmt.setInt(5, duration);
                stmt.setDouble(6, 0.0);
                stmt.setBoolean(7, false);
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void updateListeningSession(Integer eventId,
                                       int playedSeconds,
                                       int songDurationSeconds,
                                       double completionRatio,
                                       boolean countAsPlay) {
        if (eventId == null) {
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE listening_event SET played_seconds = ?, song_duration_seconds = ?, completion_ratio = ?, count_as_play = ? WHERE event_id = ?")) {
            stmt.setInt(1, Math.max(0, playedSeconds));
            stmt.setInt(2, Math.max(0, songDurationSeconds));
            stmt.setDouble(3, Math.max(0.0, Math.min(1.0, completionRatio)));
            stmt.setBoolean(4, countAsPlay);
            stmt.setInt(5, eventId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void finalizeListeningSession(Integer eventId,
                                         String actionType,
                                         int playedSeconds,
                                         int songDurationSeconds,
                                         double completionRatio,
                                         boolean countAsPlay) {
        if (eventId == null) {
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE listening_event SET action_type = ?, played_seconds = ?, song_duration_seconds = ?, completion_ratio = ?, count_as_play = ? WHERE event_id = ?")) {
            stmt.setString(1, actionType);
            stmt.setInt(2, Math.max(0, playedSeconds));
            stmt.setInt(3, Math.max(0, songDurationSeconds));
            stmt.setDouble(4, Math.max(0.0, Math.min(1.0, completionRatio)));
            stmt.setBoolean(5, countAsPlay);
            stmt.setInt(6, eventId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordLike(String username, Song song) {
        recordSimpleInteraction(username, song, "LIKE");
    }

    public void recordUnlike(String username, Song song) {
        recordSimpleInteraction(username, song, "UNLIKE");
    }

    public void recordPlaylistAdd(String username, Song song) {
        recordSimpleInteraction(username, song, "PLAYLIST_ADD");
    }

    public void recordPlaylistRemove(String username, Song song) {
        recordSimpleInteraction(username, song, "PLAYLIST_REMOVE");
    }

    private void recordSimpleInteraction(String username, Song song, String actionType) {
        if (username == null || username.isBlank() || song == null || song.songId() <= 0) {
            return;
        }

        try {
            Integer userId = findUserIdByUsername(username);
            if (userId == null) {
                return;
            }

            int duration = Math.max(song.durationSeconds(), 0);

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO listening_event (user_id, song_id, action_type, played_seconds, song_duration_seconds, completion_ratio, count_as_play) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setInt(1, userId);
                stmt.setInt(2, song.songId());
                stmt.setString(3, actionType);
                stmt.setInt(4, 0);
                stmt.setInt(5, duration);
                stmt.setDouble(6, 0.0);
                stmt.setBoolean(7, false);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Integer findUserIdByUsername(String username) throws SQLException {
        String sql = "SELECT user_id FROM app_user WHERE username = ? LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("user_id") : null;
            }
        }
    }
}
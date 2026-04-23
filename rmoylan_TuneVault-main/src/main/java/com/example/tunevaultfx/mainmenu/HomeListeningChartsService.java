package com.example.tunevaultfx.mainmenu;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads ranked “charts” for the home dashboard from aggregate listening time
 * ({@code listening_event.played_seconds}).
 */
public final class HomeListeningChartsService {

    public record ChartSongEntry(Song song, int listenedSeconds) {}

    public record ChartArtistEntry(String artistName, int listenedSeconds) {}

    public List<ChartSongEntry> loadTopSongs(String username, int limit) throws SQLException {
        List<ChartSongEntry> out = new ArrayList<>();
        if (username == null || username.isBlank() || limit <= 0) {
            return out;
        }
        Integer userId = findUserIdByUsername(username.trim());
        if (userId == null) {
            return out;
        }

        String sql =
                """
                SELECT s.song_id,
                       s.title,
                       COALESCE(a.name, '') AS artist_name,
                       '' AS album_name,
                       COALESCE(g.genre_name, '') AS genre_name,
                       COALESCE(s.duration_seconds, 0) AS duration_seconds,
                       COALESCE(SUM(le.played_seconds), 0) AS listened_seconds
                FROM listening_event le
                JOIN song s ON s.song_id = le.song_id
                JOIN artist a ON a.artist_id = s.artist_id
                LEFT JOIN genre g ON g.genre_id = s.genre_id
                WHERE le.user_id = ?
                GROUP BY s.song_id, s.title, a.name, g.genre_name, s.duration_seconds
                HAVING listened_seconds > 0
                ORDER BY listened_seconds DESC, s.title ASC
                LIMIT ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Song song =
                            new Song(
                                    rs.getInt("song_id"),
                                    rs.getString("title"),
                                    rs.getString("artist_name"),
                                    rs.getString("album_name"),
                                    rs.getString("genre_name"),
                                    rs.getInt("duration_seconds"));
                    out.add(new ChartSongEntry(song, rs.getInt("listened_seconds")));
                }
            }
        }
        return out;
    }

    public List<ChartArtistEntry> loadTopArtists(String username, int limit) throws SQLException {
        List<ChartArtistEntry> out = new ArrayList<>();
        if (username == null || username.isBlank() || limit <= 0) {
            return out;
        }
        Integer userId = findUserIdByUsername(username.trim());
        if (userId == null) {
            return out;
        }

        String sql =
                """
                SELECT a.name AS artist_name,
                       COALESCE(SUM(le.played_seconds), 0) AS listened_seconds
                FROM listening_event le
                JOIN song s ON s.song_id = le.song_id
                JOIN artist a ON a.artist_id = s.artist_id
                WHERE le.user_id = ?
                GROUP BY a.artist_id, a.name
                HAVING listened_seconds > 0
                ORDER BY listened_seconds DESC, a.name ASC
                LIMIT ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    out.add(new ChartArtistEntry(rs.getString("artist_name"), rs.getInt("listened_seconds")));
                }
            }
        }
        return out;
    }

    /**
     * Top tracks for the current calendar day only ({@code DATE(event_timestamp) = CURDATE()} in DB
     * server time). Same ranking metric as all-time charts: sum of {@code played_seconds}.
     */
    public List<ChartSongEntry> loadTopSongsToday(String username, int limit) throws SQLException {
        List<ChartSongEntry> out = new ArrayList<>();
        if (username == null || username.isBlank() || limit <= 0) {
            return out;
        }
        Integer userId = findUserIdByUsername(username.trim());
        if (userId == null) {
            return out;
        }

        String sql =
                """
                SELECT s.song_id,
                       s.title,
                       COALESCE(a.name, '') AS artist_name,
                       '' AS album_name,
                       COALESCE(g.genre_name, '') AS genre_name,
                       COALESCE(s.duration_seconds, 0) AS duration_seconds,
                       COALESCE(SUM(le.played_seconds), 0) AS listened_seconds
                FROM listening_event le
                JOIN song s ON s.song_id = le.song_id
                JOIN artist a ON a.artist_id = s.artist_id
                LEFT JOIN genre g ON g.genre_id = s.genre_id
                WHERE le.user_id = ?
                  AND DATE(le.event_timestamp) = CURDATE()
                GROUP BY s.song_id, s.title, a.name, g.genre_name, s.duration_seconds
                HAVING listened_seconds > 0
                ORDER BY listened_seconds DESC, s.title ASC
                LIMIT ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Song song =
                            new Song(
                                    rs.getInt("song_id"),
                                    rs.getString("title"),
                                    rs.getString("artist_name"),
                                    rs.getString("album_name"),
                                    rs.getString("genre_name"),
                                    rs.getInt("duration_seconds"));
                    out.add(new ChartSongEntry(song, rs.getInt("listened_seconds")));
                }
            }
        }
        return out;
    }

    public List<ChartArtistEntry> loadTopArtistsToday(String username, int limit) throws SQLException {
        List<ChartArtistEntry> out = new ArrayList<>();
        if (username == null || username.isBlank() || limit <= 0) {
            return out;
        }
        Integer userId = findUserIdByUsername(username.trim());
        if (userId == null) {
            return out;
        }

        String sql =
                """
                SELECT a.name AS artist_name,
                       COALESCE(SUM(le.played_seconds), 0) AS listened_seconds
                FROM listening_event le
                JOIN song s ON s.song_id = le.song_id
                JOIN artist a ON a.artist_id = s.artist_id
                WHERE le.user_id = ?
                  AND DATE(le.event_timestamp) = CURDATE()
                GROUP BY a.artist_id, a.name
                HAVING listened_seconds > 0
                ORDER BY listened_seconds DESC, a.name ASC
                LIMIT ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    out.add(new ChartArtistEntry(rs.getString("artist_name"), rs.getInt("listened_seconds")));
                }
            }
        }
        return out;
    }

    private Integer findUserIdByUsername(String username) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement("SELECT user_id FROM app_user WHERE username = ? LIMIT 1")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("user_id") : null;
            }
        }
    }
}

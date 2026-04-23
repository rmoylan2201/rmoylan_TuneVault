package com.example.tunevaultfx.db;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.search.SearchRecentItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class SearchHistoryDAO {

    public ObservableList<SearchRecentItem> loadRecentSearches(String username) throws SQLException {
        ObservableList<SearchRecentItem> items = FXCollections.observableArrayList();

        Integer userId = findUserIdByUsername(username);
        if (userId == null) {
            return items;
        }

        String sql = """
                SELECT sh.item_type,
                       sh.artist_name,
                       s.song_id,
                       s.title,
                       COALESCE(a.name, '') AS artist_name_for_song,
                       '' AS album_name,
                       COALESCE(g.genre_name, '') AS genre_name,
                       COALESCE(s.duration_seconds, 0) AS duration_seconds
                FROM search_history sh
                LEFT JOIN song s ON s.song_id = sh.song_id
                LEFT JOIN artist a ON a.artist_id = s.artist_id
                LEFT JOIN genre g ON g.genre_id = s.genre_id
                WHERE sh.user_id = ?
                ORDER BY sh.searched_at DESC, sh.search_history_id DESC
                LIMIT 20
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("item_type");

                    if ("SONG".equals(type) && rs.getObject("song_id") != null) {
                        Song song = new Song(
                                rs.getInt("song_id"),
                                rs.getString("title"),
                                rs.getString("artist_name_for_song"),
                                rs.getString("album_name"),
                                rs.getString("genre_name"),
                                rs.getInt("duration_seconds")
                        );
                        items.add(SearchRecentItem.song(song));
                    } else if ("ARTIST".equals(type)) {
                        String artistName = rs.getString("artist_name");
                        if (artistName != null && !artistName.isBlank()) {
                            items.add(SearchRecentItem.artist(artistName));
                        }
                    }
                }
            }
        }

        return items;
    }

    public void addRecentSearch(String username, SearchRecentItem item) throws SQLException {
        Integer userId = findUserIdByUsername(username);
        if (userId == null || item == null) {
            return;
        }

        removeDuplicate(userId, item);

        String sql = """
                INSERT INTO search_history (user_id, item_type, song_id, artist_name)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, item.getType().name());

            if (item.getType() == SearchRecentItem.Type.SONG && item.getSong() != null) {
                stmt.setInt(3, item.getSong().songId());
                stmt.setNull(4, Types.VARCHAR);
            } else if (item.getType() == SearchRecentItem.Type.ARTIST) {
                stmt.setNull(3, Types.INTEGER);
                stmt.setString(4, item.getArtistName());
            } else {
                stmt.setNull(3, Types.INTEGER);
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.executeUpdate();
        }

        trimTo20(userId);
    }

    public void clearRecentSearches(String username) throws SQLException {
        Integer userId = findUserIdByUsername(username);
        if (userId == null) {
            return;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM search_history WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private void removeDuplicate(int userId, SearchRecentItem item) throws SQLException {
        String sqlSong = "DELETE FROM search_history WHERE user_id = ? AND item_type = 'SONG' AND song_id = ?";
        String sqlArtist = "DELETE FROM search_history WHERE user_id = ? AND item_type = 'ARTIST' AND LOWER(artist_name) = LOWER(?)";

        try (Connection conn = DBConnection.getConnection()) {
            if (item.getType() == SearchRecentItem.Type.SONG && item.getSong() != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlSong)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, item.getSong().songId());
                    stmt.executeUpdate();
                }
            } else if (item.getType() == SearchRecentItem.Type.ARTIST && item.getArtistName() != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlArtist)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, item.getArtistName());
                    stmt.executeUpdate();
                }
            }
        }
    }

    private void trimTo20(int userId) throws SQLException {
        String sql = """
                DELETE FROM search_history
                WHERE user_id = ?
                  AND search_history_id NOT IN (
                      SELECT keep_id FROM (
                          SELECT search_history_id AS keep_id
                          FROM search_history
                          WHERE user_id = ?
                          ORDER BY searched_at DESC, search_history_id DESC
                          LIMIT 20
                      ) x
                  )
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private Integer findUserIdByUsername(String username) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT user_id FROM app_user WHERE username = ? LIMIT 1")) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("user_id") : null;
            }
        }
    }
}
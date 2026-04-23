package com.example.tunevaultfx.db;

import com.example.tunevaultfx.core.Song;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Loads songs from the database.
 */
public class SongDAO {

    public ObservableList<Song> getAllSongs() throws SQLException {
        ObservableList<Song> songs = FXCollections.observableArrayList();

        String sql = """
                SELECT s.song_id,
                       s.title,
                       COALESCE(a.name, '') AS artist_name,
                       '' AS album_name,
                       COALESCE(g.genre_name, '') AS genre_name,
                       COALESCE(s.duration_seconds, 0) AS duration_seconds
                FROM song s
                LEFT JOIN artist a ON a.artist_id = s.artist_id
                LEFT JOIN genre g ON g.genre_id = s.genre_id
                ORDER BY s.title
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                songs.add(new Song(
                        rs.getInt("song_id"),
                        rs.getString("title"),
                        rs.getString("artist_name"),
                        rs.getString("album_name"),
                        rs.getString("genre_name"),
                        rs.getInt("duration_seconds")
                ));
            }
        }

        return songs;
    }

    /**
     * Loads one song by primary key, or {@code null} if missing.
     */
    public Song findById(int songId) throws SQLException {
        if (songId <= 0) {
            return null;
        }
        String sql = """
                SELECT s.song_id,
                       s.title,
                       COALESCE(a.name, '') AS artist_name,
                       '' AS album_name,
                       COALESCE(g.genre_name, '') AS genre_name,
                       COALESCE(s.duration_seconds, 0) AS duration_seconds
                FROM song s
                LEFT JOIN artist a ON a.artist_id = s.artist_id
                LEFT JOIN genre g ON g.genre_id = s.genre_id
                WHERE s.song_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, songId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Song(
                        rs.getInt("song_id"),
                        rs.getString("title"),
                        rs.getString("artist_name"),
                        rs.getString("album_name"),
                        rs.getString("genre_name"),
                        rs.getInt("duration_seconds"));
            }
        }
    }

    /**
     * Returns the {@code artist_id} for a song row, or {@code null} if missing or invalid.
     */
    public Integer findArtistIdBySongId(int songId) throws SQLException {
        if (songId <= 0) {
            return null;
        }
        String sql = "SELECT artist_id FROM song WHERE song_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, songId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                int id = rs.getInt("artist_id");
                return rs.wasNull() ? null : id;
            }
        }
    }
}
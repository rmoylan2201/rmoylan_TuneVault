package com.example.tunevaultfx.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Logged-in user follows an artist (artist profile button). */
public final class ArtistFollowDAO {

    private final PlaylistDAO playlistDAO = new PlaylistDAO();

    public void followArtist(String username, int artistId) throws SQLException {
        if (username == null || username.isBlank() || artistId <= 0) {
            return;
        }
        Integer uid = playlistDAO.findUserIdByUsername(username.trim());
        if (uid == null) {
            return;
        }
        String sql =
                """
                INSERT INTO user_follows_artist (user_id, artist_id) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE artist_id = artist_id
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            stmt.setInt(2, artistId);
            stmt.executeUpdate();
        }
    }

    public void unfollowArtist(String username, int artistId) throws SQLException {
        Integer uid = playlistDAO.findUserIdByUsername(username == null ? "" : username.trim());
        if (uid == null || artistId <= 0) {
            return;
        }
        String sql = "DELETE FROM user_follows_artist WHERE user_id = ? AND artist_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            stmt.setInt(2, artistId);
            stmt.executeUpdate();
        }
    }

    public boolean isFollowingArtist(String username, int artistId) throws SQLException {
        Integer uid = playlistDAO.findUserIdByUsername(username == null ? "" : username.trim());
        if (uid == null || artistId <= 0) {
            return false;
        }
        String sql = "SELECT 1 FROM user_follows_artist WHERE user_id = ? AND artist_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            stmt.setInt(2, artistId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}

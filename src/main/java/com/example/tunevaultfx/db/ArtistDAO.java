package com.example.tunevaultfx.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Artist table lookups (e.g. follow-by-id). */
public final class ArtistDAO {

    public Optional<Integer> findArtistIdByNameIgnoreCase(String name) throws SQLException {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT artist_id FROM artist WHERE LOWER(name) = LOWER(?) LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(rs.getInt("artist_id")) : Optional.empty();
            }
        }
    }
}

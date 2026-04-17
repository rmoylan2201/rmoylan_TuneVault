package com.example.tunevaultfx.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Social graph: one user follows another (profiles, not artists). */
public final class UserFollowDAO {

    private final PlaylistDAO playlistDAO = new PlaylistDAO();

    public void follow(String followerUsername, String followeeUsername) throws SQLException {
        if (followerUsername == null
                || followeeUsername == null
                || followerUsername.isBlank()
                || followeeUsername.isBlank()) {
            return;
        }
        if (followerUsername.equalsIgnoreCase(followeeUsername)) {
            return;
        }
        Integer a = playlistDAO.findUserIdByUsername(followerUsername.trim());
        Integer b = playlistDAO.findUserIdByUsername(followeeUsername.trim());
        if (a == null || b == null) {
            return;
        }
        String sql =
                """
                INSERT INTO user_follow (follower_user_id, followee_user_id) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE followee_user_id = followee_user_id
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a);
            stmt.setInt(2, b);
            stmt.executeUpdate();
        }
    }

    public void unfollow(String followerUsername, String followeeUsername) throws SQLException {
        Integer a = playlistDAO.findUserIdByUsername(followerUsername == null ? "" : followerUsername.trim());
        Integer b = playlistDAO.findUserIdByUsername(followeeUsername == null ? "" : followeeUsername.trim());
        if (a == null || b == null) {
            return;
        }
        String sql = "DELETE FROM user_follow WHERE follower_user_id = ? AND followee_user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a);
            stmt.setInt(2, b);
            stmt.executeUpdate();
        }
    }

    public boolean isFollowing(String followerUsername, String followeeUsername) throws SQLException {
        Integer a = playlistDAO.findUserIdByUsername(followerUsername == null ? "" : followerUsername.trim());
        Integer b = playlistDAO.findUserIdByUsername(followeeUsername == null ? "" : followeeUsername.trim());
        if (a == null || b == null) {
            return false;
        }
        String sql =
                "SELECT 1 FROM user_follow WHERE follower_user_id = ? AND followee_user_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a);
            stmt.setInt(2, b);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countFollowers(String subjectUsername) throws SQLException {
        Integer uid = playlistDAO.findUserIdByUsername(subjectUsername == null ? "" : subjectUsername.trim());
        if (uid == null) {
            return 0;
        }
        String sql = "SELECT COUNT(*) AS c FROM user_follow WHERE followee_user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("c") : 0;
            }
        }
    }

    public int countFollowing(String subjectUsername) throws SQLException {
        Integer uid = playlistDAO.findUserIdByUsername(subjectUsername == null ? "" : subjectUsername.trim());
        if (uid == null) {
            return 0;
        }
        String sql = "SELECT COUNT(*) AS c FROM user_follow WHERE follower_user_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt("c") : 0;
            }
        }
    }

    public List<String> listFollowingUsernames(String subjectUsername, int limit) throws SQLException {
        return listUsernamesByDirection(subjectUsername, true, limit);
    }

    public List<String> listFollowerUsernames(String subjectUsername, int limit) throws SQLException {
        return listUsernamesByDirection(subjectUsername, false, limit);
    }

    private List<String> listUsernamesByDirection(String subjectUsername, boolean following, int limit)
            throws SQLException {
        int lim = Math.max(1, Math.min(limit, 500));
        Integer uid = playlistDAO.findUserIdByUsername(subjectUsername == null ? "" : subjectUsername.trim());
        if (uid == null) {
            return List.of();
        }
        String sql =
                following
                        ? """
                        SELECT u.username FROM user_follow f
                        JOIN app_user u ON u.user_id = f.followee_user_id
                        WHERE f.follower_user_id = ?
                        ORDER BY u.username ASC
                        LIMIT """
                                + lim
                        : """
                        SELECT u.username FROM user_follow f
                        JOIN app_user u ON u.user_id = f.follower_user_id
                        WHERE f.followee_user_id = ?
                        ORDER BY u.username ASC
                        LIMIT """
                                + lim;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, uid);
            try (ResultSet rs = stmt.executeQuery()) {
                List<String> out = new ArrayList<>();
                while (rs.next()) {
                    String n = rs.getString("username");
                    if (n != null && !n.isBlank()) {
                        out.add(n);
                    }
                }
                return out;
            }
        }
    }
}

package com.example.tunevaultfx.session;
/**
 * Stores temporary session information for the current app run.
 * Keeps track of who is currently logged in.
 */
public final class Session {
    private static Integer currentUserId;
    private static String currentUsername;

    private Session() {}

    public static void login(int userId, String username) {
        currentUserId = userId;
        currentUsername = username;
    }

    public static void logout() {
        currentUserId = null;
        currentUsername = null;
    }

    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static boolean isLoggedIn() {
        return currentUserId != null;
    }
}
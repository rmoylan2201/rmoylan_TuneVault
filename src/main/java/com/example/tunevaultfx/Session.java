package com.example.tunevaultfx;

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
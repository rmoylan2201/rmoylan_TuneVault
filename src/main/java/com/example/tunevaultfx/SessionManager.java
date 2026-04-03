package com.example.tunevaultfx;

public class SessionManager {

    private static User currentUser;
    private static UserProfile currentUserProfile;
    private static String requestedPlaylistToOpen;

    public static void startSession(User user) {
        currentUser = user;
        currentUserProfile = UserProfileStore.loadProfile(user.getUsername());
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static UserProfile getCurrentUserProfile() {
        return currentUserProfile;
    }

    public static void saveCurrentProfile() {
        if (currentUserProfile != null) {
            UserProfileStore.saveProfile(currentUserProfile);
        }
    }

    public static void requestPlaylistToOpen(String playlistName) {
        requestedPlaylistToOpen = playlistName;
    }

    public static String consumeRequestedPlaylistToOpen() {
        String temp = requestedPlaylistToOpen;
        requestedPlaylistToOpen = null;
        return temp;
    }

    public static void logout() {
        saveCurrentProfile();
        currentUser = null;
        currentUserProfile = null;
        requestedPlaylistToOpen = null;
    }
}
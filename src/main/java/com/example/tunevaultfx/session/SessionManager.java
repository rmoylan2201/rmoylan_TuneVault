package com.example.tunevaultfx.session;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.user.UserProfile;
import com.example.tunevaultfx.user.UserProfileStore;

/**
 * Manages the current logged-in user and their loaded profile.
 * Provides shared access to session-related app state.
 */
public class SessionManager {

    private static String currentUsername;
    private static UserProfile currentUserProfile;
    private static String requestedPlaylistToOpen;
    private static Song selectedSong;

    private SessionManager() {
    }

    public static void startSession(String username) {
        currentUsername = username;
        currentUserProfile = UserProfileStore.loadProfile(username);
    }

    public static void logout() {
        currentUsername = null;
        currentUserProfile = null;
        requestedPlaylistToOpen = null;
        selectedSong = null;
    }

    public static String getCurrentUsername() {
        return currentUsername;
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
        String value = requestedPlaylistToOpen;
        requestedPlaylistToOpen = null;
        return value;
    }

    public static void setSelectedSong(Song song) {
        selectedSong = song;
    }

    public static Song getSelectedSong() {
        return selectedSong;
    }
}
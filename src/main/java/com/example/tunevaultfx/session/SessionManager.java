package com.example.tunevaultfx.session;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.db.SearchHistoryDAO;
import com.example.tunevaultfx.db.UserProfileDAO;
import com.example.tunevaultfx.search.SearchRecentItem;
import com.example.tunevaultfx.user.UserProfile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Manages the current logged-in user and shared session state.
 * Now loads and saves profile data from the database.
 */
public class SessionManager {

    private static String currentUsername;
    private static UserProfile currentUserProfile;
    private static String requestedPlaylistToOpen;
    private static Song selectedSong;
    private static String selectedArtist;

    private static final ObservableList<SearchRecentItem> recentSearches = FXCollections.observableArrayList();
    private static final SearchHistoryDAO searchHistoryDAO = new SearchHistoryDAO();
    private static final UserProfileDAO userProfileDAO = new UserProfileDAO();

    private SessionManager() {
    }

    public static void startSession(String username) {
        currentUsername = username;

        try {
            currentUserProfile = userProfileDAO.loadProfile(username);
        } catch (Exception e) {
            e.printStackTrace();
            currentUserProfile = new UserProfile(username);
        }

        try {
            recentSearches.setAll(searchHistoryDAO.loadRecentSearches(username));
        } catch (Exception e) {
            e.printStackTrace();
            recentSearches.clear();
        }

        selectedSong = null;
        selectedArtist = null;
        requestedPlaylistToOpen = null;
    }

    public static void logout() {
        currentUsername = null;
        currentUserProfile = null;
        requestedPlaylistToOpen = null;
        selectedSong = null;
        selectedArtist = null;
        recentSearches.clear();
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static UserProfile getCurrentUserProfile() {
        return currentUserProfile;
    }

    public static ObservableList<SearchRecentItem> getRecentSearches() {
        return recentSearches;
    }

    public static void addRecentSearch(SearchRecentItem item) {
        if (item == null || currentUsername == null || currentUsername.isBlank()) {
            return;
        }

        recentSearches.removeIf(existing -> existing.sameAs(item));
        recentSearches.add(0, item);

        while (recentSearches.size() > 20) {
            recentSearches.remove(recentSearches.size() - 1);
        }

        try {
            searchHistoryDAO.addRecentSearch(currentUsername, item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearRecentSearches() {
        recentSearches.clear();

        if (currentUsername == null || currentUsername.isBlank()) {
            return;
        }

        try {
            searchHistoryDAO.clearRecentSearches(currentUsername);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveCurrentProfile() {
        // Intentionally left blank for playlist/song changes.
        // Playlist and liked-song updates now write directly to the database
        // through PlaylistService and UserProfileDAO.
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

    public static void setSelectedArtist(String artist) {
        selectedArtist = artist;
    }

    public static String getSelectedArtist() {
        return selectedArtist;
    }
}
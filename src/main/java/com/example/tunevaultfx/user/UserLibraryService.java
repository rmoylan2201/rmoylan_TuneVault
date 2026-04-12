package com.example.tunevaultfx.user;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.playlist.service.PlaylistService;
import com.example.tunevaultfx.session.SessionManager;

/**
 * Handles current-user song actions like liked-song checks.
 */
public class UserLibraryService {

    private final PlaylistService playlistService = new PlaylistService();

    public boolean isLiked(Song song) {
        UserProfile profile = SessionManager.getCurrentUserProfile();
        return profile != null && song != null && profile.isLiked(song);
    }

    public void addSongToPlaylist(String playlistName, Song song) {
        if (playlistName == null || playlistName.isBlank() || song == null) {
            return;
        }

        UserProfile profile = SessionManager.getCurrentUserProfile();
        if (profile == null) {
            return;
        }

        playlistService.addSongToPlaylist(profile, playlistName, song);
    }
}
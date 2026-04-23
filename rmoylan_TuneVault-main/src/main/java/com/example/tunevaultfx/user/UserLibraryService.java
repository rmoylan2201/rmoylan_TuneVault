package com.example.tunevaultfx.user;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.playlist.service.PlaylistService;
import com.example.tunevaultfx.session.SessionManager;

/**
 * Handles current-user song actions like liked-song checks.
 *
 * Key fix: isLiked() now compares by songId instead of full record equality.
 *
 * Root cause of the bug:
 *  Songs in getLikedSongs() were loaded via UserProfileDAO with '' AS album_name.
 *  Songs from SongDAO.getAllSongs() have real album names.
 *  Song is a record — Java record equality compares ALL fields, so
 *  Song(5, "Ocean Eyes", "Billie Eilish", "", "Pop", 200)
 *  != Song(5, "Ocean Eyes", "Billie Eilish", "dont smile at me", "Pop", 200)
 *  even though they are the same song. This caused isLiked() to return the
 *  wrong result depending on which Song object was passed in.
 *
 *  Fixing this here means UserProfile.isLiked() is also fixed since it's
 *  only called from here.
 */
public class UserLibraryService {

    private final PlaylistService playlistService = new PlaylistService();

    /**
     * Returns true if the given song is in the current user's Liked Songs.
     * Comparison is by songId only — immune to field differences between
     * songs loaded from different queries.
     */
    public boolean isLiked(Song song) {
        UserProfile profile = SessionManager.getCurrentUserProfile();
        if (profile == null || song == null) return false;

        return profile.getLikedSongs().stream()
                .anyMatch(s -> s != null && s.songId() == song.songId());
    }

    public void addSongToPlaylist(String playlistName, Song song) {
        if (playlistName == null || playlistName.isBlank() || song == null) return;

        UserProfile profile = SessionManager.getCurrentUserProfile();
        if (profile == null) return;

        playlistService.addSongToPlaylist(profile, playlistName, song);
    }
}

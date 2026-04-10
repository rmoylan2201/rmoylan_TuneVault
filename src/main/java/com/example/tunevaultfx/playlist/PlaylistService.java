package com.example.tunevaultfx.playlist;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.user.UserProfile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
/**
 * Handles playlist operations such as creating, deleting,
 * adding songs, and removing songs.
 */
public class PlaylistService {

    public boolean createPlaylist(UserProfile profile, String name) {
        if (profile == null || name == null) {
            return false;
        }

        String trimmed = name.trim();
        if (trimmed.isEmpty() || profile.getPlaylists().containsKey(trimmed)) {
            return false;
        }

        profile.getPlaylists().put(trimmed, FXCollections.observableArrayList());
        SessionManager.saveCurrentProfile();
        return true;
    }

    public boolean deletePlaylist(UserProfile profile, String playlistName) {
        if (profile == null || playlistName == null) {
            return false;
        }

        if ("Liked Songs".equals(playlistName)) {
            return false;
        }

        if (!profile.getPlaylists().containsKey(playlistName)) {
            return false;
        }

        profile.getPlaylists().remove(playlistName);
        SessionManager.saveCurrentProfile();
        return true;
    }

    public boolean addSongToPlaylist(UserProfile profile, String playlistName, Song song) {
        if (profile == null || playlistName == null || song == null) {
            return false;
        }

        ObservableList<Song> songs = profile.getPlaylists().get(playlistName);
        if (songs == null || songs.contains(song)) {
            return false;
        }

        songs.add(song);
        SessionManager.saveCurrentProfile();
        return true;
    }

    public boolean removeSongFromPlaylist(UserProfile profile, String playlistName, Song song) {
        if (profile == null || playlistName == null || song == null) {
            return false;
        }

        ObservableList<Song> songs = profile.getPlaylists().get(playlistName);
        if (songs == null) {
            return false;
        }

        boolean removed = songs.remove(song);
        if (removed) {
            SessionManager.saveCurrentProfile();
        }
        return removed;
    }
}
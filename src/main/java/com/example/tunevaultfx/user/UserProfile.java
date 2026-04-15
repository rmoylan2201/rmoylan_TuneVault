package com.example.tunevaultfx.user;

import com.example.tunevaultfx.core.PlaylistNames;
import com.example.tunevaultfx.core.Song;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.LinkedHashMap;

/**
 * Represents one user's in-memory music data: playlists and liked songs.
 *
 * isLiked() compares by songId, not full record equality, to handle the
 * case where the same song is represented by different Song objects with
 * differing album fields (e.g. UserProfileDAO loads '' AS album_name while
 * SongDAO loads the real album name).
 */
public class UserProfile {

    private final String username;
    private final ObservableMap<String, ObservableList<Song>> playlists =
            FXCollections.observableMap(new LinkedHashMap<>());

    public UserProfile(String username) {
        this.username = username;
        playlists.put(PlaylistNames.LIKED_SONGS, FXCollections.observableArrayList());
    }

    public String getUsername() { return username; }

    public ObservableMap<String, ObservableList<Song>> getPlaylists() {
        return playlists;
    }

    public ObservableList<Song> getLikedSongs() {
        return playlists.computeIfAbsent(
                PlaylistNames.LIKED_SONGS, k -> FXCollections.observableArrayList());
    }

    /**
     * Returns true if the given song is in Liked Songs.
     * Uses songId comparison — immune to field differences between Song objects
     * loaded by different DAO queries.
     */
    public boolean isLiked(Song song) {
        if (song == null) return false;
        return getLikedSongs().stream()
                .anyMatch(s -> s != null && s.songId() == song.songId());
    }

    /**
     * Toggles the liked state of a song in memory only.
     * The actual DB write is handled by PlaylistService / UserProfileDAO.
     */
    public void toggleLike(Song song) {
        if (song == null) return;
        ObservableList<Song> liked = getLikedSongs();
        boolean alreadyLiked = liked.stream()
                .anyMatch(s -> s != null && s.songId() == song.songId());
        if (alreadyLiked) {
            liked.removeIf(s -> s != null && s.songId() == song.songId());
        } else {
            liked.add(song);
        }
    }
}

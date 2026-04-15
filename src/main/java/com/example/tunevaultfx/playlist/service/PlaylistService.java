package com.example.tunevaultfx.playlist.service;

import com.example.tunevaultfx.core.PlaylistNames;
import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.db.ListeningEventDAO;
import com.example.tunevaultfx.db.UserProfileDAO;
import com.example.tunevaultfx.musicplayer.controller.MusicPlayerController;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.user.UserProfile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;

/**
 * Handles playlist operations such as creating, deleting,
 * adding songs, and removing songs.
 * Uses database-backed updates instead of rewriting the whole profile.
 */
public class PlaylistService {

    private final UserProfileDAO userProfileDAO = new UserProfileDAO();
    private final ListeningEventDAO listeningEventDAO = new ListeningEventDAO();

    public boolean createPlaylist(UserProfile profile, String name) {
        if (profile == null || name == null || name.isBlank()) {
            return false;
        }

        if (profile.getPlaylists().containsKey(name)) {
            return false;
        }

        try {
            boolean created = userProfileDAO.createPlaylist(profile.getUsername(), name);
            if (created) {
                profile.getPlaylists().put(name, FXCollections.observableArrayList());
            }
            return created;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Picks a playlist name for quick-create-from-song: the song title, made unique and safe
     * (never collides with {@code Liked Songs} or an existing playlist name).
     */
    public String suggestPlaylistNameFromSongTitle(UserProfile profile, Song song) {
        String base =
                (song != null && song.title() != null) ? song.title().trim() : "";
        if (base.isBlank()) {
            base = "New playlist";
        }
        if ("Liked Songs".equals(base)) {
            base = "Liked Songs mix";
        }
        if (profile == null || profile.getPlaylists() == null) {
            return base;
        }
        if (!profile.getPlaylists().containsKey(base)) {
            return base;
        }
        int n = 2;
        String candidate;
        do {
            candidate = base + " (" + n + ")";
            n++;
        } while (profile.getPlaylists().containsKey(candidate));
        return candidate;
    }

    /** Creates a playlist named after the song and adds that song in one step. */
    public boolean createPlaylistWithSong(UserProfile profile, Song song) {
        if (profile == null || song == null) {
            return false;
        }
        String name = suggestPlaylistNameFromSongTitle(profile, song);
        if (!createPlaylist(profile, name)) {
            return false;
        }
        return addSongToPlaylist(profile, name, song);
    }

    public boolean deletePlaylist(UserProfile profile, String playlistName) {
        if (profile == null || playlistName == null) {
            return false;
        }
        if (isProtectedPlaylist(playlistName)) {
            return false;
        }

        try {
            boolean deleted = userProfileDAO.deletePlaylist(profile.getUsername(), playlistName);
            if (deleted) {
                profile.getPlaylists().remove(playlistName);
            }
            return deleted;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addSongToPlaylist(UserProfile profile, String playlistName, Song song) {
        if (profile == null || playlistName == null || song == null) {
            return false;
        }

        ObservableList<Song> playlistSongs = profile.getPlaylists().get(playlistName);
        if (playlistSongs == null) {
            return false;
        }

        if (playlistContainsSongId(playlistSongs, song)) {
            return false;
        }

        try {
            boolean added = userProfileDAO.addSongToPlaylist(profile.getUsername(), playlistName, song);
            if (added) {
                playlistSongs.add(song);
                listeningEventDAO.recordPlaylistAdd(profile.getUsername(), song);
                if (isProtectedPlaylist(playlistName)) {
                    notifyCurrentSongLikeUi();
                }
            }
            return added;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeSongFromPlaylist(UserProfile profile, String playlistName, Song song) {
        if (profile == null || playlistName == null || song == null) {
            return false;
        }

        ObservableList<Song> playlistSongs = profile.getPlaylists().get(playlistName);
        if (playlistSongs == null) {
            return false;
        }

        try {
            boolean removed = userProfileDAO.removeSongFromPlaylist(profile.getUsername(), playlistName, song);
            if (removed) {
                removeSongWithId(playlistSongs, song);
                listeningEventDAO.recordPlaylistRemove(profile.getUsername(), song);
                if (isProtectedPlaylist(playlistName)) {
                    notifyCurrentSongLikeUi();
                }
            }
            return removed;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void toggleLikeSong(Song song) {
        UserProfile profile = SessionManager.getCurrentUserProfile();
        if (profile == null || song == null) {
            return;
        }

        boolean wasLiked = profile.isLiked(song);

        try {
            userProfileDAO.toggleLike(profile.getUsername(), song);
            profile.toggleLike(song);

            if (wasLiked) {
                listeningEventDAO.recordUnlike(profile.getUsername(), song);
            } else {
                listeningEventDAO.recordLike(profile.getUsername(), song);
            }
            notifyCurrentSongLikeUi();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isProtectedPlaylist(String playlistName) {
        return PlaylistNames.isLikedSongs(playlistName);
    }

    /**
     * Song is a record: {@code contains}/{@code remove} use full field equality, but liked rows may
     * differ from the current-playback instance (e.g. album string). Match by id instead.
     */
    private static boolean playlistContainsSongId(ObservableList<Song> list, Song song) {
        if (song == null) {
            return false;
        }
        return list.stream().anyMatch(s -> s != null && s.songId() == song.songId());
    }

    private static void removeSongWithId(ObservableList<Song> list, Song song) {
        if (song == null) {
            return;
        }
        list.removeIf(s -> s != null && s.songId() == song.songId());
    }

    /** Mini / expanded player heart reads {@link MusicPlayerController#currentSongLikedProperty()}. */
    private static void notifyCurrentSongLikeUi() {
        MusicPlayerController.getInstance().refreshCurrentSongLiked();
    }
}
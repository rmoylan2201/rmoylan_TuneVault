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
import java.util.ArrayList;

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
                profile.getPinnedPlaylistsOrdered().remove(playlistName);
                persistPins(profile);
                MusicPlayerController.getInstance().onPlaylistDeleted(playlistName);
            }
            return deleted;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean renamePlaylist(UserProfile profile, String oldName, String newName) {
        if (profile == null || oldName == null || newName == null) {
            return false;
        }
        if (isProtectedPlaylist(oldName) || PlaylistNames.isLikedSongs(newName.trim())) {
            return false;
        }
        ObservableList<Song> list = profile.getPlaylists().get(oldName);
        if (list == null) {
            return false;
        }
        try {
            if (!userProfileDAO.renamePlaylist(profile.getUsername(), oldName, newName)) {
                return false;
            }
            SessionManager.setPendingPlaylistRenameSelection(newName.trim());
            profile.getPlaylists().put(newName.trim(), list);
            profile.getPlaylists().remove(oldName);
            var pins = profile.getPinnedPlaylistsOrdered();
            int i = pins.indexOf(oldName);
            if (i >= 0) {
                pins.set(i, newName.trim());
                persistPins(profile);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isPlaylistPinned(UserProfile profile, String playlistName) {
        if (profile == null || playlistName == null) {
            return false;
        }
        return profile.getPinnedPlaylistsOrdered().contains(playlistName);
    }

    /**
     * @param pin true to pin, false to unpin
     * @return false if pin was requested but the cap was reached
     */
    public boolean setPlaylistPinned(UserProfile profile, String playlistName, boolean pin) {
        if (profile == null || playlistName == null) {
            return false;
        }
        var pins = profile.getPinnedPlaylistsOrdered();
        if (pin) {
            if (pins.contains(playlistName)) {
                return true;
            }
            if (pins.size() >= PlaylistNames.MAX_USER_PINNED_PLAYLISTS) {
                return false;
            }
            pins.add(playlistName);
        } else {
            pins.remove(playlistName);
        }
        try {
            persistPins(profile);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void persistPins(UserProfile profile) throws SQLException {
        userProfileDAO.syncPlaylistPins(
                profile.getUsername(), new ArrayList<>(profile.getPinnedPlaylistsOrdered()));
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

    /** Non-system playlists only. Persists {@code playlist.is_public}. */
    public boolean setPlaylistPublic(UserProfile profile, String playlistName, boolean isPublic) {
        if (profile == null || PlaylistNames.isLikedSongs(playlistName)) {
            return false;
        }
        try {
            return userProfileDAO.setPlaylistPublic(profile.getUsername(), playlistName, isPublic);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isPlaylistPublic(UserProfile profile, String playlistName) {
        if (profile == null) {
            return false;
        }
        try {
            return userProfileDAO.isPlaylistPublic(profile.getUsername(), playlistName);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
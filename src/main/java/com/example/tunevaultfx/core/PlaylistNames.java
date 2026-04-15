package com.example.tunevaultfx.core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.List;

/**
 * Canonical playlist name strings and UI ordering shared across UI, services, and persistence.
 */
public final class PlaylistNames {

    public static final String LIKED_SONGS = "Liked Songs";
    /** When a song title would equal {@link #LIKED_SONGS}, use this for a new playlist name. */
    public static final String LIKED_SONGS_DERIVED_ALIAS = "Liked Songs mix";

    private static final Comparator<String> DISPLAY_ORDER =
            (a, b) -> {
                if (LIKED_SONGS.equals(a)) {
                    return -1;
                }
                if (LIKED_SONGS.equals(b)) {
                    return 1;
                }
                return a.compareToIgnoreCase(b);
            };

    private PlaylistNames() {}

    public static boolean isLikedSongs(String name) {
        return LIKED_SONGS.equals(name);
    }

    public static String glyphForPlaylist(String playlistName) {
        return isLikedSongs(playlistName) ? "\u2665" : "\u266B";
    }

    public static void sortForDisplay(List<String> names) {
        names.sort(DISPLAY_ORDER);
    }

    public static void sortForDisplay(ObservableList<String> names) {
        FXCollections.sort(names, DISPLAY_ORDER);
    }
}

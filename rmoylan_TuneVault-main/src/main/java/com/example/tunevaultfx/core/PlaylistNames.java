package com.example.tunevaultfx.core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Canonical playlist name strings and UI ordering shared across UI, services, and persistence.
 */
public final class PlaylistNames {

    public static final String LIKED_SONGS = "Liked Songs";
    /** User-chosen pins in the library sidebar (may include {@link #LIKED_SONGS}; max 3 total). */
    public static final int MAX_USER_PINNED_PLAYLISTS = 3;
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

    /**
     * Sidebar order: pinned playlists in the given order (each must exist in {@code names}), then
     * every other playlist A–Z (including unpinned {@link #LIKED_SONGS}).
     */
    public static void orderSidebarPlaylists(List<String> names, List<String> pinnedUserPlaylistsInOrder) {
        if (names == null || names.isEmpty()) {
            return;
        }
        Set<String> inNames = new HashSet<>(names);
        LinkedHashSet<String> pinOrdered = new LinkedHashSet<>();
        if (pinnedUserPlaylistsInOrder != null) {
            for (String p : pinnedUserPlaylistsInOrder) {
                if (p != null && inNames.contains(p)) {
                    pinOrdered.add(p);
                }
            }
        }
        List<String> pinned = new ArrayList<>(pinOrdered);
        Set<String> pinnedSet = new LinkedHashSet<>(pinned);
        List<String> rest = new ArrayList<>();
        for (String n : names) {
            if (n != null && !pinnedSet.contains(n)) {
                rest.add(n);
            }
        }
        rest.sort(String.CASE_INSENSITIVE_ORDER);
        names.clear();
        names.addAll(pinned);
        names.addAll(rest);
    }
}

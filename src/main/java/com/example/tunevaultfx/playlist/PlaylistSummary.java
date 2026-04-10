package com.example.tunevaultfx.playlist;

import com.example.tunevaultfx.core.Song;
import javafx.collections.ObservableList;
/**
 * Simple data object that stores display information
 * about one playlist.
 */
public class PlaylistSummary {
    private final String playlistName;
    private final ObservableList<Song> songs;
    private final int songCount;
    private final int totalDurationSeconds;
    private final String formattedDuration;

    public PlaylistSummary(String playlistName,
                           ObservableList<Song> songs,
                           int songCount,
                           int totalDurationSeconds,
                           String formattedDuration) {
        this.playlistName = playlistName;
        this.songs = songs;
        this.songCount = songCount;
        this.totalDurationSeconds = totalDurationSeconds;
        this.formattedDuration = formattedDuration;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public ObservableList<Song> getSongs() {
        return songs;
    }

    public int getSongCount() {
        return songCount;
    }

    public int getTotalDurationSeconds() {
        return totalDurationSeconds;
    }

    public String getFormattedDuration() {
        return formattedDuration;
    }
}
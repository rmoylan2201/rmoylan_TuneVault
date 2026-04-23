package com.example.tunevaultfx.core;

/**
 * Represents one song in the application.
 * Stores the song id, title, artist, album, genre, and duration.
 */
public record Song(
        int songId,
        String title,
        String artist,
        String album,
        String genre,
        int durationSeconds
) {

    public Song(String title, String artist, String album, String genre, int durationSeconds) {
        this(0, title, artist, album, genre, durationSeconds);
    }

    @Override
    public String toString() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return title + " - " + artist + " (" + minutes + ":" + String.format("%02d", seconds) + ")";
    }
}
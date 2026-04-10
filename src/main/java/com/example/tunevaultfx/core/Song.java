package com.example.tunevaultfx.core;
/**
 * Represents one song in the application.
 * Stores the song title, artist, album, and duration.
 */
public record Song(String title, String artist, String album, int durationSeconds) {

    @Override
    public String toString() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return title + " - " + artist + " (" + minutes + ":" + String.format("%02d", seconds) + ")";
    }
}
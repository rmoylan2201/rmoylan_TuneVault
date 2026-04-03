package com.example.tunevaultfx;

public record Song(String title, String artist, String album, int durationSeconds) {

    @Override
    public String toString() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return title + " - " + artist + " (" + minutes + ":" + String.format("%02d", seconds) + ")";
    }
}
package com.example.tunevaultfx.core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Provides built-in demo songs for the application.
 * Acts as the app's default music library.
 */
public class DemoLibrary {

    private static final ObservableList<Song> SONGS = FXCollections.observableArrayList(
            new Song("Midnight Echo", "Nova Lane", "Afterglow", "Pop", 205),
            new Song("Blue Static", "The Satellites", "City Radio", "Indie", 188),
            new Song("Falling Lights", "Aria Bloom", "Open Skies", "Pop", 240),
            new Song("Neon Drive", "Pulse City", "Night Run", "Synthwave", 197),
            new Song("Velvet Skies", "Luna Hart", "Moon Hotel", "R&B", 221),
            new Song("Golden Hour", "Sunset Motel", "Westbound", "Country", 214),
            new Song("Silver Dreams", "Aurora Bay", "Horizon", "Dream Pop", 232),
            new Song("Glass Hearts", "The Lanes", "Reflection", "Rock", 201),
            new Song("Night Bloom", "Aster Vale", "Neon Garden", "Alternative", 216)
    );

    public static ObservableList<Song> getSongs() {
        return SONGS;
    }
}
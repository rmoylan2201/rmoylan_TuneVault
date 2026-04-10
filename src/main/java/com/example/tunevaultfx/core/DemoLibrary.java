package com.example.tunevaultfx.core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
/**
 * Provides built-in demo songs for the application.
 * Acts as the app's default music library.
 */
public class DemoLibrary {

    private static final ObservableList<Song> SONGS = FXCollections.observableArrayList(
            new Song("Midnight Echo", "Nova Lane", "Afterglow", 205),
            new Song("Blue Static", "The Satellites", "City Radio", 188),
            new Song("Falling Lights", "Aria Bloom", "Open Skies", 240),
            new Song("Neon Drive", "Pulse City", "Night Run", 197),
            new Song("Velvet Skies", "Luna Hart", "Moon Hotel", 221),
            new Song("Golden Hour", "Sunset Motel", "Westbound", 214),
            new Song("Silver Dreams", "Aurora Bay", "Horizon", 232),
            new Song("Glass Hearts", "The Lanes", "Reflection", 201),
            new Song("Night Bloom", "Aster Vale", "Neon Garden", 216)
    );

    public static ObservableList<Song> getSongs() {
        return SONGS;
    }
}
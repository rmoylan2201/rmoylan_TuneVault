package com.example.tunevaultfx.playlist.service;

import com.example.tunevaultfx.core.Song;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Handles searching and filtering songs based on user input.
 * Used by playlist search and general search flows.
 */
public class SongSearchService {

    public ObservableList<Song> filterSongs(ObservableList<Song> sourceSongs, String searchText) {
        ObservableList<Song> results = FXCollections.observableArrayList();

        if (sourceSongs == null) {
            return results;
        }

        String search = searchText == null ? "" : searchText.trim().toLowerCase();

        if (search.isBlank()) {
            return results;
        }

        for (Song song : sourceSongs) {
            if (song == null) {
                continue;
            }

            if (contains(song.title(), search)
                    || contains(song.artist(), search)
                    || contains(song.album(), search)
                    || contains(song.genre(), search)) {
                results.add(song);
            }
        }

        return results;
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }
}
package com.example.tunevaultfx.controllers;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.util.SceneUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.io.IOException;
/**
 * Controls the song details page.
 * Displays information about the currently selected song,
 * such as title, artist, album, and duration.
 */
public class SongDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label artistLabel;
    @FXML private Label albumLabel;
    @FXML private Label durationLabel;

    @FXML
    public void initialize() {
        Song song = SessionManager.getSelectedSong();

        if (song == null) {
            titleLabel.setText("No song selected");
            artistLabel.setText("-");
            albumLabel.setText("Album: -");
            durationLabel.setText("Duration: -");
            return;
        }

        titleLabel.setText(song.title());
        artistLabel.setText(song.artist());
        albumLabel.setText("Album: " + song.album());
        durationLabel.setText("Duration: " + formatTime(song.durationSeconds()));
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        SceneUtil.switchScene((Node) event.getSource(), "playlists-page.fxml");
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }
}
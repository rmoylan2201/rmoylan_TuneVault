package com.example.tunevaultfx.controllers;

import com.example.tunevaultfx.util.SceneUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;
/**
 * Controls the wrapped screen.
 * Displays listening summary information such as top song,
 * top artist, favorite genre, and total listening time.
 */
public class WrappedPageController {

    @FXML private Label topSongLabel;
    @FXML private Label topArtistLabel;
    @FXML private Label favoriteGenreLabel;
    @FXML private Label totalMinutesLabel;
    @FXML private Label summaryLabel;

    @FXML private ProgressBar songBar;
    @FXML private ProgressBar artistBar;
    @FXML private ProgressBar genreBar;

    @FXML
    public void initialize() {
        topSongLabel.setText("Midnight Echo");
        topArtistLabel.setText("Nova Lane");
        favoriteGenreLabel.setText("Synthwave");
        totalMinutesLabel.setText("1,482 minutes");
        summaryLabel.setText("You loved late-night, atmospheric tracks with a cinematic vibe.");

        songBar.setProgress(0.86);
        artistBar.setProgress(0.72);
        genreBar.setProgress(0.91);
    }

    @FXML
    private void handleBackToMenu(ActionEvent e) throws IOException {
        SceneUtil.switchScene((Node) e.getSource(), "main-menu.fxml");
    }
}
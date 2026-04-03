package com.example.tunevaultfx;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.io.IOException;

public class MiniPlayerController {

    @FXML private Label miniSongLabel;
    @FXML private Label miniArtistLabel;
    @FXML private Label miniTimeLabel;

    @FXML private Hyperlink miniPlaylistLink;

    @FXML private Button miniShuffleButton;
    @FXML private Button miniPreviousButton;
    @FXML private Button miniPlayPauseButton;
    @FXML private Button miniNextButton;
    @FXML private Button miniLoopButton;
    @FXML private Button miniLikeButton;

    @FXML private Slider miniProgressSlider;

    private final MusicPlayerService player = MusicPlayerService.getInstance();

    @FXML
    public void initialize() {
        miniSongLabel.textProperty().bind(player.currentTitleProperty());
        miniArtistLabel.textProperty().bind(player.currentArtistProperty());

        miniPlayPauseButton.textProperty().bind(
                Bindings.when(player.playingProperty()).then("||").otherwise("▶")
        );

        miniProgressSlider.setOnMouseReleased(e -> player.seek((int) miniProgressSlider.getValue()));
        miniProgressSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                player.seek((int) miniProgressSlider.getValue());
            }
        });

        player.currentSongProperty().addListener((obs, oldVal, newVal) -> {
            updateMiniLikeButton();
            updateMiniTime();
        });
        player.currentSecondProperty().addListener((obs, oldVal, newVal) -> updateMiniTime());
        player.currentDurationProperty().addListener((obs, oldVal, newVal) -> updateMiniTime());
        player.currentSourcePlaylistNameProperty().addListener((obs, oldVal, newVal) -> updateMiniPlaylistLink());
        player.shuffleEnabledProperty().addListener((obs, oldVal, newVal) -> updateMiniModeButtons());
        player.loopEnabledProperty().addListener((obs, oldVal, newVal) -> updateMiniModeButtons());

        updateMiniLikeButton();
        updateMiniTime();
        updateMiniPlaylistLink();
        updateMiniModeButtons();

        setupMiniButtonHover(miniShuffleButton);
        setupMiniButtonHover(miniPreviousButton);
        setupMiniButtonHover(miniPlayPauseButton);
        setupMiniButtonHover(miniNextButton);
        setupMiniButtonHover(miniLoopButton);
        setupMiniButtonHover(miniLikeButton);
    }

    @FXML
    private void handleMiniShuffle() {
        player.toggleShuffle();
        updateMiniModeButtons();
    }

    @FXML
    private void handleMiniPrevious() {
        player.previous();
        updateMiniLikeButton();
    }

    @FXML
    private void handleMiniPlayPause() {
        player.togglePlayPause();
    }

    @FXML
    private void handleMiniNext() {
        player.next();
        updateMiniLikeButton();
    }

    @FXML
    private void handleMiniLoop() {
        player.toggleLoop();
        updateMiniModeButtons();
    }

    @FXML
    private void handleMiniLike() {
        player.toggleLikeCurrentSong();
        updateMiniLikeButton();
    }

    @FXML
    private void handleOpenNowPlaying(ActionEvent event) throws IOException {
        SceneUtil.switchScene((Node) event.getSource(), "nowplaying-page.fxml");
    }

    @FXML
    private void handleOpenCurrentPlaylist(ActionEvent event) throws IOException {
        String playlistName = player.getCurrentSourcePlaylistName();
        if (playlistName == null || playlistName.isBlank()) {
            return;
        }

        SessionManager.requestPlaylistToOpen(playlistName);
        SceneUtil.switchScene((Node) event.getSource(), "playlists-page.fxml");
    }

    private void updateMiniPlaylistLink() {
        String playlistName = player.getCurrentSourcePlaylistName();

        if (playlistName == null || playlistName.isBlank()) {
            miniPlaylistLink.setText("");
            miniPlaylistLink.setVisible(false);
            miniPlaylistLink.setManaged(false);
        } else {
            miniPlaylistLink.setText("Playlist: " + playlistName);
            miniPlaylistLink.setVisible(true);
            miniPlaylistLink.setManaged(true);
        }
    }

    private void updateMiniLikeButton() {
        miniLikeButton.setText(player.isCurrentSongLiked() ? "♥" : "♡");
    }

    private void updateMiniModeButtons() {
        miniShuffleButton.setText("🔀");
        miniLoopButton.setText("🔁");
        miniShuffleButton.setOpacity(player.isShuffleEnabled() ? 1.0 : 0.65);
        miniLoopButton.setOpacity(player.isLoopEnabled() ? 1.0 : 0.65);
    }

    private void updateMiniTime() {
        int current = player.currentSecondProperty().get();
        int total = player.currentDurationProperty().get();

        miniProgressSlider.setMax(total);
        miniProgressSlider.setValue(current);
        miniTimeLabel.setText(formatTime(current) + " / " + formatTime(total));
    }

    private void setupMiniButtonHover(Button button) {
        String normal = button.getStyle();
        String hover = normal + "; -fx-background-color: rgba(255,255,255,0.12);";

        button.setOnMouseEntered(e -> button.setStyle(hover));
        button.setOnMouseExited(e -> button.setStyle(normal));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }
}
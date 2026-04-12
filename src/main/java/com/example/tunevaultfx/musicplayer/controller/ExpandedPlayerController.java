package com.example.tunevaultfx.musicplayer.controller;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.playlist.service.PlaylistPickerService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controls the expanded player overlay shown above the current page.
 */
public class ExpandedPlayerController {

    @FXML private StackPane overlayRoot;
    @FXML private VBox playerCard;

    @FXML private Label titleLabel;
    @FXML private Label artistLabel;
    @FXML private Label albumLabel;
    @FXML private Label timeLabel;

    @FXML private Button playPauseButton;
    @FXML private Button likeButton;
    @FXML private Button shuffleButton;
    @FXML private Button loopButton;
    @FXML private Button addToPlaylistButton;

    @FXML private Slider progressSlider;

    private final MusicPlayerController player = MusicPlayerController.getInstance();
    private final PlaylistPickerService addToPlaylistDialog = new PlaylistPickerService();

    private boolean animatingClose = false;

    @FXML
    public void initialize() {
        overlayRoot.setVisible(false);
        overlayRoot.setManaged(false);
        overlayRoot.setOpacity(0);
        playerCard.setTranslateY(80);

        titleLabel.textProperty().bind(player.currentTitleProperty());
        artistLabel.textProperty().bind(player.currentArtistProperty());

        playPauseButton.textProperty().bind(
                Bindings.when(player.playingProperty()).then("⏸").otherwise("▶")
        );

        player.currentSongProperty().addListener((obs, oldVal, newVal) -> {
            refreshSongInfo();
            refreshLikeButton();
            refreshAddButton();
        });

        player.currentSecondProperty().addListener((obs, oldVal, newVal) -> refreshTime());
        player.currentDurationProperty().addListener((obs, oldVal, newVal) -> refreshTime());

        progressSlider.setOnMouseReleased(e -> player.seek((int) progressSlider.getValue()));
        progressSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging) {
                player.seek((int) progressSlider.getValue());
            }
        });

        player.shuffleEnabledProperty().addListener((obs, oldVal, newVal) -> updateModeButtons());
        player.loopEnabledProperty().addListener((obs, oldVal, newVal) -> updateModeButtons());

        player.expandedPlayerVisibleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                openOverlay();
            } else {
                closeOverlay();
            }
        });

        player.currentSongLikedProperty().addListener((obs, oldVal, newVal) -> refreshLikeButton());

        shuffleButton.setStyle(neutralButtonStyle(18, 21));
        loopButton.setStyle(neutralButtonStyle(18, 21));
        likeButton.setStyle(neutralButtonStyle(20, 22));
        addToPlaylistButton.setStyle(neutralButtonStyle(22, 22));

        refreshSongInfo();
        refreshTime();
        refreshLikeButton();
        refreshAddButton();
        updateModeButtons();
    }

    @FXML
    private void handleBackdropClick() {
        player.setExpandedPlayerVisible(false);
    }

    @FXML
    private void handleClose() {
        player.setExpandedPlayerVisible(false);
    }

    @FXML
    private void handleConsumeClick() {
        // Prevent click from closing overlay.
    }

    @FXML
    private void handlePrevious() {
        player.previous();
        refreshLikeButton();
        refreshAddButton();
    }

    @FXML
    private void handleNext() {
        player.next();
        refreshLikeButton();
        refreshAddButton();
    }

    @FXML
    private void handlePlayPause() {
        player.togglePlayPause();
    }

    @FXML
    private void handleLike() {
        player.toggleLikeCurrentSong();
        refreshLikeButton();
    }

    @FXML
    private void handleShuffle() {
        player.toggleShuffle();
        updateModeButtons();
    }

    @FXML
    private void handleLoop() {
        player.toggleLoop();
        updateModeButtons();
    }

    @FXML
    private void handleAddToPlaylist() {
        addToPlaylistDialog.show(player.getCurrentSong());
        refreshAddButton();
    }

    private void refreshSongInfo() {
        Song song = player.getCurrentSong();

        if (song == null) {
            albumLabel.setText("Album: -");
            return;
        }

        String album = song.album();
        if (album == null || album.isBlank()) {
            albumLabel.setText("Album: -");
        } else {
            albumLabel.setText("Album: " + album);
        }
    }

    private void refreshTime() {
        int current = player.currentSecondProperty().get();
        int total = player.currentDurationProperty().get();

        progressSlider.setMax(total);
        progressSlider.setValue(current);
        timeLabel.setText(formatTime(current) + " / " + formatTime(total));
    }

    private void refreshLikeButton() {
        boolean liked = player.isCurrentSongLiked();

        likeButton.setText(liked ? "♥" : "♡");
        likeButton.setStyle(
                liked
                        ? "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 22;"
                        : "-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 22;"
        );
    }

    private void refreshAddButton() {
        boolean hasSong = player.getCurrentSong() != null;

        addToPlaylistButton.setDisable(!hasSong);
        addToPlaylistButton.setStyle(
                "-fx-background-color: #e2e8f0; " +
                        "-fx-text-fill: #475569; " +
                        "-fx-font-size: 22px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 22;"
        );
    }

    private void updateModeButtons() {
        shuffleButton.setText("🔀");
        loopButton.setText("↻");

        shuffleButton.setStyle(
                player.isShuffleEnabled()
                        ? "-fx-background-color: #fef3c7; -fx-text-fill: #1DB954; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 21;"
                        : "-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 21;"
        );

        loopButton.setStyle(
                player.isLoopEnabled()
                        ? "-fx-background-color: #e2e8f0; -fx-text-fill: #1DB954; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 21;"
                        : "-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 21;"
        );
    }

    private void openOverlay() {
        animatingClose = false;
        overlayRoot.setManaged(true);
        overlayRoot.setVisible(true);

        overlayRoot.setOpacity(0);
        playerCard.setTranslateY(120);
        playerCard.setScaleX(0.97);
        playerCard.setScaleY(0.97);

        FadeTransition fade = new FadeTransition(Duration.millis(220), overlayRoot);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(280), playerCard);
        slide.setFromY(120);
        slide.setToY(0);

        javafx.animation.ScaleTransition scale =
                new javafx.animation.ScaleTransition(Duration.millis(280), playerCard);
        scale.setFromX(0.97);
        scale.setFromY(0.97);
        scale.setToX(1.0);
        scale.setToY(1.0);

        new ParallelTransition(fade, slide, scale).play();
    }

    private void closeOverlay() {
        if (!overlayRoot.isVisible() || animatingClose) {
            return;
        }

        animatingClose = true;

        FadeTransition fade = new FadeTransition(Duration.millis(180), overlayRoot);
        fade.setFromValue(overlayRoot.getOpacity());
        fade.setToValue(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(220), playerCard);
        slide.setFromY(playerCard.getTranslateY());
        slide.setToY(100);

        javafx.animation.ScaleTransition scale =
                new javafx.animation.ScaleTransition(Duration.millis(220), playerCard);
        scale.setFromX(playerCard.getScaleX());
        scale.setFromY(playerCard.getScaleY());
        scale.setToX(0.98);
        scale.setToY(0.98);

        ParallelTransition transition = new ParallelTransition(fade, slide, scale);
        transition.setOnFinished(e -> {
            overlayRoot.setVisible(false);
            overlayRoot.setManaged(false);
            animatingClose = false;
        });
        transition.play();
    }

    private String neutralButtonStyle(int fontSize, int radius) {
        return "-fx-background-color: #e2e8f0;" +
                "-fx-text-fill: #334155;" +
                "-fx-font-size: " + fontSize + "px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: " + radius + ";";
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }
}
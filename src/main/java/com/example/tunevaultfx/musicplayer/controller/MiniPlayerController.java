package com.example.tunevaultfx.musicplayer.controller;

import com.example.tunevaultfx.playlist.service.PlaylistPickerService;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.util.SceneUtil;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Controls the shared mini-player shown across pages.
 */
public class MiniPlayerController {

    @FXML private Label miniSongLabel;
    @FXML private Label miniArtistLabel;
    @FXML private Label miniTimeLabel;

    @FXML private Hyperlink miniPlaylistLink;

    @FXML private Button miniPlayPauseButton;
    @FXML private Button miniLikeButton;
    @FXML private Button miniShuffleButton;
    @FXML private Button miniLoopButton;
    @FXML private Button miniAddButton;

    @FXML private Slider miniProgressSlider;

    private final MusicPlayerController player = MusicPlayerController.getInstance();
    private final PlaylistPickerService addToPlaylistDialog = new PlaylistPickerService();

    @FXML
    public void initialize() {
        miniSongLabel.textProperty().bind(player.currentTitleProperty());
        miniArtistLabel.textProperty().bind(player.currentArtistProperty());

        miniPlayPauseButton.textProperty().bind(
                Bindings.when(player.playingProperty()).then("⏸").otherwise("▶")
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
            updateMiniPlaylistLink();
            updateMiniAddButton();
        });

        player.currentSecondProperty().addListener((obs, oldVal, newVal) -> updateMiniTime());
        player.currentDurationProperty().addListener((obs, oldVal, newVal) -> updateMiniTime());
        player.currentSourcePlaylistNameProperty().addListener((obs, oldVal, newVal) -> updateMiniPlaylistLink());

        player.shuffleEnabledProperty().addListener((obs, oldVal, newVal) -> updateMiniModeButtons());
        player.loopEnabledProperty().addListener((obs, oldVal, newVal) -> updateMiniModeButtons());

        player.currentSongLikedProperty().addListener((obs, oldVal, newVal) -> updateMiniLikeButton());

        updateMiniLikeButton();
        updateMiniTime();
        updateMiniPlaylistLink();
        updateMiniModeButtons();
        updateMiniAddButton();
    }

    @FXML
    private void handleMiniPrevious() {
        player.previous();
        updateMiniLikeButton();
        updateMiniAddButton();
    }

    @FXML
    private void handleMiniPlayPause() {
        player.togglePlayPause();
    }

    @FXML
    private void handleMiniNext() {
        player.next();
        updateMiniLikeButton();
        updateMiniAddButton();
    }

    @FXML
    private void handleMiniLike() {
        player.toggleLikeCurrentSong();
        updateMiniLikeButton();
    }

    @FXML
    private void handleMiniShuffle() {
        player.toggleShuffle();
        updateMiniModeButtons();
    }

    @FXML
    private void handleMiniLoop() {
        player.toggleLoop();
        updateMiniModeButtons();
    }

    @FXML
    private void handleMiniAddToPlaylist() {
        addToPlaylistDialog.show(player.getCurrentSong());
        updateMiniAddButton();
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

    @FXML
    private void handleOpenNowPlaying(MouseEvent event) {
        if (player.getCurrentSong() == null) {
            return;
        }

        Node source = (Node) event.getSource();
        ensureExpandedPlayerAttached(source);
        player.setExpandedPlayerVisible(true);
    }

    private void ensureExpandedPlayerAttached(Node sourceNode) {
        Scene scene = sourceNode.getScene();
        if (scene == null) {
            return;
        }

        Parent currentRoot = scene.getRoot();
        if (currentRoot.lookup("#expandedPlayerOverlay") != null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/tunevaultfx/expanded-page.fxml")
            );
            Parent overlay = loader.load();

            StackPane wrapper = new StackPane();
            wrapper.getChildren().add(currentRoot);
            wrapper.getChildren().add(overlay);

            scene.setRoot(wrapper);

            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case ESCAPE -> player.setExpandedPlayerVisible(false);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        boolean liked = player.isCurrentSongLiked();

        miniLikeButton.setText(liked ? "♥" : "♡");
        miniLikeButton.setStyle(
                liked
                        ? "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 21;"
                        : "-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 21;"
        );
    }

    private void updateMiniAddButton() {
        boolean hasSong = player.getCurrentSong() != null;

        miniAddButton.setDisable(!hasSong);
        miniAddButton.setStyle(
                "-fx-background-color: #e2e8f0; " +
                        "-fx-text-fill: #475569; " +
                        "-fx-font-size: 20px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 21;"
        );
    }

    private void updateMiniModeButtons() {
        miniShuffleButton.setText("🔀");
        miniLoopButton.setText("↻");

        miniShuffleButton.setStyle(
                player.isShuffleEnabled()
                        ? "-fx-background-color: #fef3c7; -fx-text-fill: #1DB954; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 21;"
                        : "-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 21;"
        );

        miniLoopButton.setStyle(
                player.isLoopEnabled()
                        ? "-fx-background-color: #e2e8f0; -fx-text-fill: #1DB954; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 21;"
                        : "-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-size: 18px; -fx-font-weight: bold; -fx-background-radius: 21;"
        );
    }

    private void updateMiniTime() {
        int current = player.currentSecondProperty().get();
        int total = player.currentDurationProperty().get();

        miniProgressSlider.setMax(total);
        miniProgressSlider.setValue(current);
        miniTimeLabel.setText(formatTime(current) + " / " + formatTime(total));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }
}
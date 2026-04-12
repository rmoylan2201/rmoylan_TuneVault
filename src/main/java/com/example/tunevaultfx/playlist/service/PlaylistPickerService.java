package com.example.tunevaultfx.playlist.service;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.user.UserProfile;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class PlaylistPickerService {

    private final PlaylistService playlistService = new PlaylistService();

    public void show(Song song) {
        UserProfile profile = SessionManager.getCurrentUserProfile();

        if (song == null || profile == null || profile.getPlaylists().isEmpty()) {
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add to Playlist");
        dialog.setHeaderText("Choose playlists for \"" + song.title() + "\"");

        ButtonType closeButtonType = new ButtonType("Done", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);
        dialog.getDialogPane().setPrefWidth(420);

        List<String> playlistNames = new ArrayList<>(profile.getPlaylists().keySet());
        ListView<String> playlistListView = new ListView<>(FXCollections.observableArrayList(playlistNames));
        playlistListView.setPrefHeight(320);
        playlistListView.setFocusTraversable(false);
        playlistListView.getSelectionModel().clearSelection();
        playlistListView.setCellFactory(listView -> new PlaylistPickerCell(profile, song));

        dialog.getDialogPane().setContent(playlistListView);
        dialog.showAndWait();
    }

    private boolean songIsInPlaylist(UserProfile profile, String playlistName, Song song) {
        List<Song> songs = profile.getPlaylists().get(playlistName);
        return songs != null && songs.contains(song);
    }

    private class PlaylistPickerCell extends ListCell<String> {
        private final UserProfile profile;
        private final Song song;

        private final HBox root = new HBox();
        private final Label nameLabel = new Label();
        private final Region spacer = new Region();
        private final Button actionButton = new Button();

        PlaylistPickerCell(UserProfile profile, Song song) {
            this.profile = profile;
            this.song = song;

            HBox.setHgrow(spacer, Priority.ALWAYS);

            root.setSpacing(12);
            root.setPadding(new Insets(8, 10, 8, 10));
            root.setStyle("-fx-background-color: transparent; -fx-background-radius: 14;");

            nameLabel.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 14px; -fx-font-weight: bold;");

            actionButton.setPrefWidth(42);
            actionButton.setPrefHeight(32);
            actionButton.setFocusTraversable(false);

            root.getChildren().addAll(nameLabel, spacer, actionButton);

            root.setOnMouseClicked(event -> {
                String playlistName = getItem();
                if (playlistName == null || isEmpty()) {
                    return;
                }

                togglePlaylistMembership(playlistName);
                event.consume();
            });

            setOnMousePressed(event -> {
                if (!isEmpty() && getListView() != null) {
                    getListView().getSelectionModel().clearSelection();
                    event.consume();
                }
            });
        }

        @Override
        protected void updateItem(String playlistName, boolean empty) {
            super.updateItem(playlistName, empty);

            if (empty || playlistName == null) {
                setText(null);
                setGraphic(null);
                setBackground(Background.EMPTY);
                setStyle("-fx-background-color: transparent;");
                return;
            }

            nameLabel.setText(playlistName);
            refreshActionButton(playlistName);

            setText(null);
            setGraphic(root);
            setBackground(Background.EMPTY);
            setStyle("-fx-background-color: transparent; -fx-padding: 2 0 2 0;");
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(false);
        }

        private void togglePlaylistMembership(String playlistName) {
            boolean wasInPlaylist = songIsInPlaylist(profile, playlistName, song);
            boolean changed;

            if (wasInPlaylist) {
                changed = playlistService.removeSongFromPlaylist(profile, playlistName, song);
            } else {
                changed = playlistService.addSongToPlaylist(profile, playlistName, song);
            }

            if (changed) {
                playClickFlash(!wasInPlaylist);
            }

            refreshActionButton(playlistName);

            if (getListView() != null) {
                getListView().refresh();
                getListView().getSelectionModel().clearSelection();
            }
        }

        private void refreshActionButton(String playlistName) {
            boolean alreadyInPlaylist = songIsInPlaylist(profile, playlistName, song);

            actionButton.setText(alreadyInPlaylist ? "✓" : "+");
            actionButton.setStyle(alreadyInPlaylist
                    ? "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 16;"
                    : "-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 16;");

            actionButton.setOnAction(event -> {
                togglePlaylistMembership(playlistName);
                event.consume();
            });
        }

        private void playClickFlash(boolean added) {
            Color flashColor = added ? Color.web("#dbeafe") : Color.web("#fee2e2");

            root.setBackground(new Background(
                    new BackgroundFill(flashColor, new CornerRadii(14), Insets.EMPTY)
            ));

            PauseTransition pause = new PauseTransition(Duration.millis(180));
            pause.setOnFinished(e -> root.setBackground(Background.EMPTY));
            pause.play();
        }
    }
}
package com.example.tunevaultfx.playlist.service;

import com.example.tunevaultfx.core.PlaylistNames;
import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.user.UserProfile;
import com.example.tunevaultfx.util.AppTheme;
import com.example.tunevaultfx.util.CellStyleKit;
import com.example.tunevaultfx.util.OverlayTheme;
import com.example.tunevaultfx.util.SceneUtil;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * In-scene overlay for picking playlists to add a song to (theme-aware).
 */
public class PlaylistPickerService {

    private final PlaylistService playlistService = new PlaylistService();

    public void show(Song song) {
        show(song, null);
    }

    public void show(Song song, Scene scene) {
        UserProfile profile = SessionManager.getCurrentUserProfile();
        if (song == null || profile == null || profile.getPlaylists() == null) {
            return;
        }

        if (scene == null) {
            return;
        }

        StackPane backdrop = new StackPane();
        backdrop.setStyle(OverlayTheme.backdrop());
        backdrop.setOnMouseClicked(e -> closeOverlay(backdrop));

        VBox card = new VBox(10);
        card.setMaxWidth(400);
        card.setMaxHeight(480);
        card.setPadding(new Insets(16, 18, 14, 18));
        card.setStyle(OverlayTheme.card());
        card.setOnMouseClicked(e -> e.consume());

        Label title = new Label("Add to Playlist");
        title.setStyle(OverlayTheme.title());

        Label subtitle = new Label("Choose playlists for \u201c" + song.title() + "\u201d");
        subtitle.setStyle(OverlayTheme.subtitle());
        subtitle.setWrapText(true);

        TextField searchField = new TextField();
        searchField.setPromptText("Search playlists");
        searchField.setStyle(OverlayTheme.createPlaylistField());
        searchField.setPrefHeight(36);

        Label newPlaylistError = new Label();
        newPlaylistError.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444;");
        newPlaylistError.setManaged(false);
        newPlaylistError.setVisible(false);
        newPlaylistError.setWrapText(true);

        Button addToNewPlaylistBtn = new Button();
        addToNewPlaylistBtn.setMaxWidth(Double.MAX_VALUE);
        addToNewPlaylistBtn.setWrapText(true);
        addToNewPlaylistBtn.setStyle(OverlayTheme.secondaryButton());
        addToNewPlaylistBtn.setOnMouseEntered(
                e -> addToNewPlaylistBtn.setStyle(OverlayTheme.secondaryButtonHover()));
        addToNewPlaylistBtn.setOnMouseExited(
                e -> addToNewPlaylistBtn.setStyle(OverlayTheme.secondaryButton()));
        String songTitle = song.title() == null ? "" : song.title().trim();
        String resolvedName = playlistService.suggestPlaylistNameFromSongTitle(profile, song);
        if (songTitle.isEmpty()) {
            addToNewPlaylistBtn.setText("Create new playlist with this song");
        } else {
            addToNewPlaylistBtn.setText(
                    "Create playlist \u201c" + resolvedName + "\u201d with this song");
        }

        List<String> names = new ArrayList<>(profile.getPlaylists().keySet());
        PlaylistNames.sortForDisplay(names);
        ObservableList<String> master = FXCollections.observableArrayList(names);
        FilteredList<String> filtered = new FilteredList<>(master, w -> true);
        searchField
                .textProperty()
                .addListener(
                        (obs, o, q) -> {
                            String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
                            filtered.setPredicate(
                                    name ->
                                            needle.isEmpty()
                                                    || name.toLowerCase(Locale.ROOT).contains(needle));
                        });

        ListView<String> listView = new ListView<>(filtered);
        listView.setPrefHeight(200);
        listView.setMaxHeight(220);
        listView.setMinHeight(120);
        listView.setFocusTraversable(false);
        listView.getSelectionModel().clearSelection();
        listView.setCellFactory(lv -> new PickerCell(profile, song, listView));
        VBox.setVgrow(listView, Priority.NEVER);

        Runnable refreshAfterCreate =
                () -> {
                    master.setAll(new ArrayList<>(profile.getPlaylists().keySet()));
                    PlaylistNames.sortForDisplay(master);
                    listView.refresh();
                };

        addToNewPlaylistBtn.setOnAction(
                e -> {
                    if (!playlistService.createPlaylistWithSong(profile, song)) {
                        newPlaylistError.setText(
                                "Couldn\u2019t create the playlist. Try again.");
                        newPlaylistError.setVisible(true);
                        newPlaylistError.setManaged(true);
                        return;
                    }
                    newPlaylistError.setVisible(false);
                    newPlaylistError.setManaged(false);
                    refreshAfterCreate.run();
                    closeOverlay(backdrop);
                    e.consume();
                });

        Button doneBtn = new Button("Done");
        doneBtn.setMaxWidth(Double.MAX_VALUE);
        doneBtn.setStyle(OverlayTheme.primaryButton());
        doneBtn.setOnMouseEntered(e -> doneBtn.setStyle(OverlayTheme.primaryButtonHover()));
        doneBtn.setOnMouseExited(e -> doneBtn.setStyle(OverlayTheme.primaryButton()));
        doneBtn.setOnAction(e -> closeOverlay(backdrop));

        VBox header = new VBox(2, title, subtitle);
        Label newSectionLabel = new Label("New playlist (uses this song\u2019s title)");
        newSectionLabel.setStyle(OverlayTheme.subtitle());
        card.getChildren()
                .addAll(
                        header,
                        searchField,
                        newSectionLabel,
                        addToNewPlaylistBtn,
                        newPlaylistError,
                        listView,
                        doneBtn);

        StackPane.setAlignment(card, Pos.CENTER);
        backdrop.getChildren().add(card);

        backdrop.addEventFilter(
                KeyEvent.KEY_PRESSED,
                e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        closeOverlay(backdrop);
                        e.consume();
                    }
                });

        if (scene.getRoot() instanceof StackPane sp) {
            sp.getChildren().add(backdrop);
        } else {
            StackPane wrapper = new StackPane();
            wrapper.getChildren().addAll(scene.getRoot(), backdrop);
            scene.setRoot(wrapper);
            SceneUtil.applySavedTheme(scene);
        }

        backdrop.setOpacity(0);
        card.setTranslateY(40);
        FadeTransition fade = new FadeTransition(Duration.millis(180), backdrop);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(220), card);
        slide.setToY(0);
        new ParallelTransition(fade, slide).play();

        backdrop.requestFocus();
    }

    private void closeOverlay(StackPane backdrop) {
        FadeTransition fade = new FadeTransition(Duration.millis(140), backdrop);
        fade.setToValue(0);
        fade.setOnFinished(
                e -> {
                    javafx.scene.Parent p = backdrop.getParent();
                    if (p instanceof StackPane sp) {
                        sp.getChildren().remove(backdrop);
                    }
                });
        fade.play();
    }

    private boolean songIsInPlaylist(UserProfile profile, String playlistName, Song song) {
        List<Song> songs = profile.getPlaylists().get(playlistName);
        if (songs == null || song == null) {
            return false;
        }
        return songs.stream().anyMatch(s -> s != null && s.songId() == song.songId());
    }

    private class PickerCell extends ListCell<String> {
        private final UserProfile profile;
        private final Song song;
        private final ListView<String> parentList;

        private final HBox row = new HBox(12);
        private final StackPane icon = new StackPane();
        private final Label iconLbl = new Label();
        private final Label nameLabel = new Label();
        private final Region spacer = new Region();
        private final Button actionBtn = new Button();

        private static final String BTN_ADD =
                "-fx-background-color: rgba(139,92,246,0.16);"
                        + "-fx-text-fill: #c4b5fd;"
                        + "-fx-font-size: 16px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-color: rgba(139,92,246,0.28);"
                        + "-fx-border-radius: 16; -fx-border-width: 1;"
                        + "-fx-cursor: hand;";

        private static final String BTN_ADD_LIGHT =
                "-fx-background-color: rgba(124,58,237,0.14);"
                        + "-fx-text-fill: #5b21b6;"
                        + "-fx-font-size: 16px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-color: rgba(124,58,237,0.28);"
                        + "-fx-border-radius: 16; -fx-border-width: 1;"
                        + "-fx-cursor: hand;";

        private static final String BTN_ADDED =
                "-fx-background-color: rgba(34,197,94,0.18);"
                        + "-fx-text-fill: #86efac;"
                        + "-fx-font-size: 16px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-color: rgba(34,197,94,0.28);"
                        + "-fx-border-radius: 16; -fx-border-width: 1;"
                        + "-fx-cursor: hand;";

        private static final String BTN_ADDED_LIGHT =
                "-fx-background-color: rgba(22,163,74,0.16);"
                        + "-fx-text-fill: #15803d;"
                        + "-fx-font-size: 16px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 16;"
                        + "-fx-border-color: rgba(22,163,74,0.30);"
                        + "-fx-border-radius: 16; -fx-border-width: 1;"
                        + "-fx-cursor: hand;";

        private static final String BTN_REMOVE =
                "-fx-background-color: rgba(239,68,68,0.14);"
                        + "-fx-text-fill: #fca5a5;"
                        + "-fx-font-size: 13px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 16;"
                        + "-fx-cursor: hand;";

        private static final String BTN_REMOVE_LIGHT =
                "-fx-background-color: rgba(220,38,38,0.12);"
                        + "-fx-text-fill: #b91c1c;"
                        + "-fx-font-size: 13px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 16;"
                        + "-fx-cursor: hand;";

        PickerCell(UserProfile profile, Song song, ListView<String> parentList) {
            this.profile = profile;
            this.song = song;
            this.parentList = parentList;

            icon.setPrefSize(32, 32);
            icon.setMinSize(32, 32);
            icon.setMaxSize(32, 32);
            iconLbl.setStyle(OverlayTheme.pickerIconGlyph());
            icon.setStyle(OverlayTheme.pickerIconBox());
            icon.getChildren().add(iconLbl);
            StackPane.setAlignment(iconLbl, Pos.CENTER);

            nameLabel.setStyle(
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: "
                            + CellStyleKit.getTextPrimary()
                            + ";");

            actionBtn.setPrefSize(38, 32);
            actionBtn.setMinSize(38, 32);
            actionBtn.setMaxSize(38, 32);
            actionBtn.setFocusTraversable(false);

            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 10, 6, 10));
            row.setStyle("-fx-background-color: transparent; -fx-background-radius: 14;");
            row.getChildren().addAll(icon, nameLabel, spacer, actionBtn);

            row.setOnMouseEntered(e -> row.setStyle(OverlayTheme.pickerRowHover()));
            row.setOnMouseExited(e ->
                    row.setStyle("-fx-background-color: transparent; -fx-background-radius: 14;"));

            actionBtn.setOnMouseEntered(e -> {
                String name = getItem();
                if (name != null && songIsInPlaylist(profile, name, song)) {
                    actionBtn.setText("\u2212");
                    actionBtn.setStyle(removeHoverStyle());
                }
            });
            actionBtn.setOnMouseExited(e -> {
                String name = getItem();
                if (name != null) {
                    refreshBtn(name);
                }
            });

            actionBtn.setOnAction(
                    e -> {
                        String name = getItem();
                        if (name != null) {
                            toggle(name);
                        }
                        e.consume();
                    });
            row.setOnMouseClicked(
                    e -> {
                        String name = getItem();
                        if (name != null && !isEmpty()) {
                            toggle(name);
                        }
                        e.consume();
                    });

            setOnMousePressed(
                    e -> {
                        if (!isEmpty() && getListView() != null) {
                            getListView().getSelectionModel().clearSelection();
                        }
                    });
        }

        private String removeHoverStyle() {
            return AppTheme.isLightMode() ? BTN_REMOVE_LIGHT : BTN_REMOVE;
        }

        @Override
        protected void updateItem(String name, boolean empty) {
            super.updateItem(name, empty);
            if (empty || name == null) {
                setText(null);
                setGraphic(null);
                setBackground(Background.EMPTY);
                setStyle("-fx-background-color: transparent;");
                return;
            }
            icon.setStyle(OverlayTheme.pickerIconBox());
            iconLbl.setStyle(OverlayTheme.pickerIconGlyph());
            iconLbl.setText(PlaylistNames.glyphForPlaylist(name));
            nameLabel.setText(name);
            nameLabel.setStyle(
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: "
                            + CellStyleKit.getTextPrimary()
                            + ";");
            refreshBtn(name);
            setText(null);
            setGraphic(row);
            setBackground(Background.EMPTY);
            setStyle("-fx-background-color: transparent; -fx-padding: 2 0 2 0;");
        }

        @Override
        public void updateSelected(boolean s) {
            super.updateSelected(false);
        }

        private void toggle(String name) {
            boolean wasIn = songIsInPlaylist(profile, name, song);
            if (wasIn) {
                playlistService.removeSongFromPlaylist(profile, name, song);
            } else {
                playlistService.addSongToPlaylist(profile, name, song);
            }
            flashRow(!wasIn);
            parentList.refresh();
        }

        private void refreshBtn(String name) {
            boolean inPlaylist = songIsInPlaylist(profile, name, song);
            actionBtn.setText(inPlaylist ? "\u2713" : "+");
            if (AppTheme.isLightMode()) {
                actionBtn.setStyle(inPlaylist ? BTN_ADDED_LIGHT : BTN_ADD_LIGHT);
            } else {
                actionBtn.setStyle(inPlaylist ? BTN_ADDED : BTN_ADD);
            }
        }

        private void flashRow(boolean added) {
            String flash =
                    added
                            ? "-fx-background-color: rgba(34,197,94,0.12); -fx-background-radius: 14;"
                            : "-fx-background-color: rgba(239,68,68,0.10); -fx-background-radius: 14;";
            if (AppTheme.isLightMode()) {
                flash =
                        added
                                ? "-fx-background-color: rgba(22,163,74,0.10); -fx-background-radius: 14;"
                                : "-fx-background-color: rgba(220,38,38,0.08); -fx-background-radius: 14;";
            }
            row.setStyle(flash);
            PauseTransition p = new PauseTransition(Duration.millis(200));
            p.setOnFinished(e ->
                    row.setStyle("-fx-background-color: transparent; -fx-background-radius: 14;"));
            p.play();
        }
    }
}

package com.example.tunevaultfx.search;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.db.SongDAO;
import com.example.tunevaultfx.musicplayer.controller.MusicPlayerController;
import com.example.tunevaultfx.playlist.service.PlaylistPickerService;
import com.example.tunevaultfx.playlist.service.PlaylistService;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.util.AppTheme;
import com.example.tunevaultfx.util.CellStyleKit;
import com.example.tunevaultfx.util.SceneUtil;
import com.example.tunevaultfx.util.SongContextMenuBuilder;
import com.example.tunevaultfx.util.TimeUtil;
import com.example.tunevaultfx.view.FxmlResources;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Song detail page: metadata, actions, and genre-based picks from the library.
 */
public class SongProfileController {

    @FXML private Label songTitleLabel;
    @FXML private Hyperlink songArtistLink;
    @FXML private Label songMetaTailLabel;
    @FXML private Button playSongButton;
    @FXML private Button likeSongButton;
    @FXML private Button addSongButton;

    @FXML private VBox relatedSection;
    @FXML private Label relatedHeadingLabel;
    @FXML private Label relatedEmptyLabel;
    @FXML private ListView<Song> relatedSongsListView;

    private final ObservableList<Song> relatedSongs = FXCollections.observableArrayList();
    private final SongDAO songDAO = new SongDAO();
    private final MusicPlayerController player = MusicPlayerController.getInstance();
    private final PlaylistService playlistService = new PlaylistService();
    private final PlaylistPickerService pickerService = new PlaylistPickerService();

    private Song song;
    /** Virtual playlist name shown while playing the related list (mini player “From:”). */
    private String relatedQueueSourceLabel = "";

    // ─────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        Song selected = SessionManager.getSelectedSong();
        if (selected == null || selected.songId() <= 0) {
            song = selected;
            showEmptyState("No song selected. Right-click any track and choose View song details.");
            return;
        }

        try {
            Song loaded = songDAO.findById(selected.songId());
            song = loaded != null ? loaded : selected;
        } catch (SQLException e) {
            e.printStackTrace();
            song = selected;
        }

        populateHero();
        relatedSongsListView.setItems(relatedSongs);
        setupRelatedCells();
        installPlayerRefreshListeners();
        player.currentSongLikedProperty().addListener((obs, o, n) -> refreshLikeButton());

        refreshLikeButton();
        loadRelatedSongs();
    }

    private void showEmptyState(String message) {
        songTitleLabel.setText("Song");
        if (songArtistLink != null) {
            songArtistLink.setVisible(false);
            songArtistLink.setManaged(false);
        }
        if (songMetaTailLabel != null) {
            songMetaTailLabel.setText(message);
        }
        if (playSongButton != null) {
            playSongButton.setDisable(true);
            playSongButton.setVisible(false);
            playSongButton.setManaged(false);
        }
        if (likeSongButton != null) {
            likeSongButton.setDisable(true);
            likeSongButton.setVisible(false);
            likeSongButton.setManaged(false);
        }
        if (addSongButton != null) {
            addSongButton.setDisable(true);
            addSongButton.setVisible(false);
            addSongButton.setManaged(false);
        }
        if (relatedSection != null) {
            relatedSection.setVisible(false);
            relatedSection.setManaged(false);
        }
    }

    private void populateHero() {
        if (song == null) {
            return;
        }
        songTitleLabel.setText(song.title() == null || song.title().isBlank() ? "Untitled" : song.title());

        String album = song.album();
        boolean hasAlbum = album != null && !album.isBlank();
        String genre = song.genre();
        boolean hasGenre = genre != null && !genre.isBlank();
        String dur = TimeUtil.formatTime(Math.max(0, song.durationSeconds()));

        boolean hasArtist = song.artist() != null && !song.artist().isBlank();
        if (songArtistLink != null) {
            if (hasArtist) {
                songArtistLink.setText(song.artist().trim());
                songArtistLink.setVisible(true);
                songArtistLink.setManaged(true);
            } else {
                songArtistLink.setVisible(false);
                songArtistLink.setManaged(false);
            }
        }

        StringBuilder tail = new StringBuilder();
        if (hasAlbum) {
            tail.append(" · ").append(album);
        }
        if (hasGenre) {
            tail.append(" · ").append(genre);
        }
        tail.append(" · ").append(dur);

        if (songMetaTailLabel != null) {
            if (hasArtist) {
                songMetaTailLabel.setText(tail.toString());
            } else {
                StringBuilder meta = new StringBuilder();
                if (hasAlbum) {
                    meta.append(album);
                }
                if (hasGenre) {
                    if (meta.length() > 0) {
                        meta.append(" · ");
                    }
                    meta.append(genre);
                }
                if (meta.length() > 0) {
                    meta.append(" · ");
                }
                meta.append(dur);
                songMetaTailLabel.setText(meta.toString());
            }
        }

        if (relatedHeadingLabel != null) {
            relatedHeadingLabel.setText(
                    hasGenre ? "More in " + genre : "More from your library");
        }
    }

    private void loadRelatedSongs() {
        if (song == null) {
            return;
        }
        final String genre = song.genre() == null ? "" : song.genre().trim();
        relatedQueueSourceLabel =
                genre.isEmpty() ? "Library picks" : genre + " · picks";

        Task<List<Song>> task =
                new Task<>() {
                    @Override
                    protected List<Song> call() throws Exception {
                        ObservableList<Song> source =
                                SessionManager.isSongLibraryReady()
                                        ? SessionManager.getSongLibrary()
                                        : songDAO.getAllSongs();
                        if (genre.isEmpty()) {
                            return source.stream()
                                    .filter(s -> s != null && s.songId() != song.songId())
                                    .limit(24)
                                    .toList();
                        }
                        String g = genre.toLowerCase(Locale.ROOT);
                        return source.stream()
                                .filter(s -> s != null && s.songId() != song.songId())
                                .filter(
                                        s -> {
                                            String sg = s.genre();
                                            return sg != null
                                                    && sg.toLowerCase(Locale.ROOT).equals(g);
                                        })
                                .limit(24)
                                .toList();
                    }
                };

        task.setOnSucceeded(
                e -> {
                    relatedSongs.setAll(task.getValue());
                    boolean empty = relatedSongs.isEmpty();
                    if (relatedEmptyLabel != null) {
                        relatedEmptyLabel.setVisible(empty);
                        relatedEmptyLabel.setManaged(empty);
                        if (genre.isEmpty()) {
                            relatedEmptyLabel.setText(
                                    "No other tracks to suggest yet. Try again after your library loads.");
                        } else {
                            relatedEmptyLabel.setText(
                                    "No other songs share this genre in your library right now.");
                        }
                    }
                    if (relatedSongsListView != null) {
                        relatedSongsListView.setPrefHeight(empty ? 120 : 420);
                    }
                });
        task.setOnFailed(
                e -> {
                    task.getException().printStackTrace();
                    relatedSongs.clear();
                    if (relatedEmptyLabel != null) {
                        relatedEmptyLabel.setText("Could not load related songs.");
                        relatedEmptyLabel.setVisible(true);
                        relatedEmptyLabel.setManaged(true);
                    }
                });

        Thread t = new Thread(task, "song-related-loader");
        t.setDaemon(true);
        t.start();
    }

    private void installPlayerRefreshListeners() {
        if (relatedSongsListView == null) {
            return;
        }
        Runnable refresh = () -> Platform.runLater(() -> relatedSongsListView.refresh());
        player.currentSongProperty().addListener((obs, o, n) -> refresh.run());
        player.playingProperty().addListener((obs, o, n) -> refresh.run());
        player.currentSourcePlaylistNameProperty().addListener((obs, o, n) -> refresh.run());
    }

    private void setupRelatedCells() {
        relatedSongsListView.setCellFactory(
                lv ->
                        new ListCell<>() {
                            private ContextMenu menu;

                            @Override
                            protected void updateItem(Song item, boolean empty) {
                                super.updateItem(item, empty);
                                if (menu != null) {
                                    menu.hide();
                                    menu = null;
                                }
                                if (empty || item == null) {
                                    setText(null);
                                    setGraphic(null);
                                    setBackground(Background.EMPTY);
                                    setStyle("-fx-background-color: transparent;");
                                    return;
                                }

                                boolean isCurrent = isCurrentRelatedTrack(item);
                                boolean audioPlaying = isCurrent && player.isPlaying();

                                int index = relatedSongs.indexOf(item) + 1;
                                Label num = CellStyleKit.trackNumber(index);
                                StackPane icon =
                                        CellStyleKit.iconBox("♫", CellStyleKit.Palette.PURPLE, false);
                                VBox text =
                                        CellStyleKit.textBox(
                                                item.title(),
                                                CellStyleKit.albumMeta(item.album(), item.genre()));
                                Label dur = CellStyleKit.duration(item.durationSeconds());

                                if (isCurrent && !text.getChildren().isEmpty()) {
                                    text.getChildren()
                                            .get(0)
                                            .setStyle(
                                                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: "
                                                            + CellStyleKit.getAccentTitle()
                                                            + ";");
                                }

                                Button playBtn = new Button(audioPlaying ? "⏸" : "▶");
                                playBtn.setFocusTraversable(false);
                                playBtn.setPrefSize(34, 34);
                                playBtn.setMinSize(34, 34);
                                playBtn.setMaxSize(34, 34);
                                playBtn.setStyle(relatedPlayStyle(isCurrent, audioPlaying));
                                playBtn.setOnMouseEntered(
                                        ev ->
                                                playBtn.setStyle(
                                                        relatedPlayHoverStyle(isCurrent, audioPlaying)));
                                playBtn.setOnMouseExited(
                                        ev ->
                                                playBtn.setStyle(relatedPlayStyle(isCurrent, audioPlaying)));
                                playBtn.setOnAction(
                                        ev -> {
                                            playFromRelated(item);
                                            ev.consume();
                                        });

                                HBox row =
                                        CellStyleKit.row(
                                                num,
                                                icon,
                                                text,
                                                new Region() {
                                                    {
                                                        HBox.setHgrow(this, Priority.ALWAYS);
                                                    }
                                                },
                                                playBtn,
                                                dur);
                                Region edgeBar = CellStyleKit.nowPlayingEdgeBar();
                                edgeBar.setVisible(isCurrent);
                                edgeBar.setManaged(isCurrent);
                                row.getChildren().add(0, edgeBar);
                                CellStyleKit.markPlaying(row, isCurrent);

                                row.setOnMouseClicked(
                                        ev -> {
                                            if (ev.getButton() == MouseButton.SECONDARY) {
                                                showMenu(item, row, ev.getScreenX(), ev.getScreenY());
                                                ev.consume();
                                                return;
                                            }
                                            if (ev.getButton() == MouseButton.PRIMARY
                                                    && ev.getClickCount() == 2) {
                                                playFromRelated(item);
                                                ev.consume();
                                            }
                                        });
                                row.addEventFilter(
                                        ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                                        ev -> {
                                            Song s = getItem();
                                            if (s == null || isEmpty()) {
                                                return;
                                            }
                                            showMenu(s, row, ev.getScreenX(), ev.getScreenY());
                                            ev.consume();
                                        });

                                setText(null);
                                setGraphic(row);
                                setBackground(Background.EMPTY);
                                setStyle("-fx-background-color: transparent; -fx-padding: 2 0 2 0;");
                            }

                            private void showMenu(Song s, Node anchor, double sx, double sy) {
                                if (menu != null) {
                                    menu.hide();
                                }
                                menu =
                                        SongContextMenuBuilder.build(
                                                s, anchor, SongContextMenuBuilder.Spec.general());
                                menu.show(anchor, sx, sy);
                            }

                            @Override
                            public void updateSelected(boolean s) {
                                super.updateSelected(false);
                            }
                        });
    }

    private boolean isCurrentRelatedTrack(Song item) {
        if (item == null || player.getCurrentSong() == null) {
            return false;
        }
        String src = player.getCurrentSourcePlaylistName();
        if (src == null) {
            src = "";
        }
        return src.equals(relatedQueueSourceLabel)
                && player.getCurrentSong().songId() == item.songId();
    }

    private void playFromRelated(Song item) {
        if (item == null) {
            return;
        }
        int idx = relatedSongs.indexOf(item);
        if (idx < 0) {
            return;
        }
        Song cur = player.getCurrentSong();
        String curSrc = player.getCurrentSourcePlaylistName();
        if (curSrc == null) {
            curSrc = "";
        }
        if (cur != null
                && cur.songId() == item.songId()
                && curSrc.equals(relatedQueueSourceLabel)) {
            player.togglePlayPause();
        } else {
            player.playQueue(relatedSongs, idx, relatedQueueSourceLabel);
        }
    }

    private static String relatedPlayStyle(boolean currentRow, boolean playingAudio) {
        if (!currentRow) {
            if (AppTheme.isLightMode()) {
                return "-fx-background-color: transparent;"
                        + "-fx-text-fill: #64748b;"
                        + "-fx-font-size: 13px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 17;";
            }
            return "-fx-background-color: transparent;"
                    + "-fx-text-fill: #58586e;"
                    + "-fx-font-size: 13px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 17;";
        }
        if (playingAudio) {
            if (AppTheme.isLightMode()) {
                return "-fx-background-color: #7c3aed;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-size: 12px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 17;";
            }
            return "-fx-background-color: rgba(139,92,246,0.25);"
                    + "-fx-text-fill: #c4b5fd;"
                    + "-fx-font-size: 12px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 17;";
        }
        if (AppTheme.isLightMode()) {
            return "-fx-background-color: rgba(124,58,237,0.22);"
                    + "-fx-text-fill: #5b21b6;"
                    + "-fx-font-size: 13px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 17;";
        }
        return "-fx-background-color: rgba(139,92,246,0.14);"
                + "-fx-text-fill: #ddd6fe;"
                + "-fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-radius: 17;";
    }

    private static String relatedPlayHoverStyle(boolean currentRow, boolean playingAudio) {
        if (!currentRow) {
            if (AppTheme.isLightMode()) {
                return "-fx-background-color: rgba(124,58,237,0.14);"
                        + "-fx-text-fill: #5b21b6;"
                        + "-fx-font-size: 13px; -fx-font-weight: bold;"
                        + "-fx-background-radius: 17;";
            }
            return "-fx-background-color: rgba(255,255,255,0.1);"
                    + "-fx-text-fill: #f2f2fa;"
                    + "-fx-font-size: 13px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 17;";
        }
        return relatedPlayStyle(currentRow, playingAudio);
    }

    private void refreshLikeButton() {
        if (likeSongButton == null || song == null) {
            return;
        }
        boolean liked =
                SessionManager.getCurrentUserProfile() != null
                        && SessionManager.getCurrentUserProfile().isLiked(song);
        likeSongButton.setText(liked ? "♥ Saved" : "♡ Save");
    }

    @FXML
    private void handlePlay() {
        if (song == null) {
            return;
        }
        player.playSingleSong(song);
    }

    @FXML
    private void handleLike() {
        if (song == null) {
            return;
        }
        playlistService.toggleLikeSong(song);
        refreshLikeButton();
    }

    @FXML
    private void handleAddToPlaylist() {
        if (song == null || addSongButton == null) {
            return;
        }
        pickerService.show(song, addSongButton.getScene());
    }

    @FXML
    private void handleArtistNameClick(ActionEvent event) throws IOException {
        if (song == null || song.artist() == null || song.artist().isBlank()) {
            return;
        }
        SessionManager.setSelectedArtist(song.artist().trim());
        Node src = songArtistLink != null ? songArtistLink : (Node) event.getSource();
        SceneUtil.switchScene(src, FxmlResources.ARTIST_PROFILE);
    }
}

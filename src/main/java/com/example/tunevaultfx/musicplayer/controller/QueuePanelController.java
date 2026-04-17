package com.example.tunevaultfx.musicplayer.controller;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.util.AppTheme;
import com.example.tunevaultfx.util.CellStyleKit;
import com.example.tunevaultfx.util.SceneUtil;
import com.example.tunevaultfx.view.FxmlResources;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QueuePanelController {

    @FXML private StackPane queueOverlayRoot;
    @FXML private StackPane backdropPane;
    @FXML private VBox queuePanel;

    @FXML private Label queueCountLabel;
    @FXML private Button clearQueueButton;

    @FXML private VBox nowPlayingSection;
    @FXML private HBox nowPlayingRow;
    @FXML private Label nowPlayingTitle;
    @FXML private Label nowPlayingArtist;

    @FXML private Label queueBadge;
    @FXML private ListView<QueueEntry> queueListView;

    private final MusicPlayerController player = MusicPlayerController.getInstance();
    private final BooleanProperty visible = new SimpleBooleanProperty(false);
    private boolean animatingClose = false;

    private final ObservableList<QueueEntry> displayQueue = FXCollections.observableArrayList();

    private static final DataFormat DRAG_INDEX = new DataFormat("application/x-queue-drag-index");

    /** Invisible drag image so the OS does not show a file/document cursor ghost. */
    private static final Image QUEUE_DRAG_GHOST = createQueueDragGhost();

    private static Image createQueueDragGhost() {
        WritableImage img = new WritableImage(1, 1);
        img.getPixelWriter().setArgb(0, 0, 0x00000000);
        return img;
    }

    @FXML
    public void initialize() {
        queueOverlayRoot.setVisible(false);
        queueOverlayRoot.setManaged(false);
        queueOverlayRoot.setOpacity(0);
        queuePanel.setTranslateX(420);

        queueListView.setItems(displayQueue);
        queueListView.setCellFactory(lv -> new UnifiedQueueCell());
        queueListView.setPlaceholder(new Label("Nothing queued yet") {{
            setStyle("-fx-text-fill: #5c5c78; -fx-font-size: 13px;");
        }});
        queueListView.setFixedCellSize(-1);

        player.currentSongProperty().addListener((obs, o, n) -> refreshAll());
        player.playingProperty().addListener((obs, o, n) -> refreshNowPlaying());
        player.getUserQueue().addListener((ListChangeListener<Song>) c -> refreshAll());
        player.getAutoplaySuggestionsList().addListener((ListChangeListener<Song>) c -> refreshAll());

        visible.addListener((obs, o, n) -> {
            if (n) { refreshAll(); openPanel(); }
            else   { closePanel(); }
        });

        queueOverlayRoot.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null || newScene.getProperties().containsKey("queuePanelEscInstalled")) return;
            newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ESCAPE && queueOverlayRoot.isVisible()) {
                    visible.set(false);
                    event.consume();
                }
            });
            newScene.getProperties().put("queuePanelEscInstalled", true);
        });

        refreshAll();
    }

    public BooleanProperty visibleProperty() { return visible; }

    @FXML private void handleBackdropClick() { visible.set(false); }
    @FXML private void handleClose()         { visible.set(false); }
    @FXML private void handleConsumeClick()  { /* absorb */ }

    @FXML
    private void handleClearQueue() {
        player.clearUserQueue();
        refreshAll();
    }

    // ── Refresh ────────────────────────────────────────────────

    private void refreshAll() {
        refreshNowPlaying();
        rebuildDisplayQueue();
        refreshHeader();
    }

    private void refreshNowPlaying() {
        Song current = player.getCurrentSong();
        if (current == null) {
            nowPlayingTitle.setText("\u2014");
            nowPlayingArtist.setText("");
        } else {
            nowPlayingTitle.setText(current.title());
            nowPlayingArtist.setText(current.artist());
        }
    }

    private void rebuildDisplayQueue() {
        ObservableList<Song> upcoming = player.getUpcomingQueueSnapshot();
        int userQueueSize = player.getUserQueueSize();
        int playlistUpcoming = player.getPlaylistUpcomingCount();

        displayQueue.clear();
        for (int i = 0; i < upcoming.size(); i++) {
            Song s = upcoming.get(i);
            QueueSeg seg =
                    i < userQueueSize
                            ? QueueSeg.USER
                            : (i < userQueueSize + playlistUpcoming
                                    ? QueueSeg.PLAYLIST
                                    : QueueSeg.AUTOPLAY);
            displayQueue.add(new QueueEntry(s, i, seg));
        }

        int total = displayQueue.size();
        queueBadge.setText(total > 0 ? String.valueOf(total) : "");
        queueBadge.setVisible(total > 0);
        queueBadge.setManaged(total > 0);

        boolean hasUserItems = userQueueSize > 0;
        clearQueueButton.setVisible(hasUserItems);
        clearQueueButton.setManaged(hasUserItems);
    }

    private void refreshHeader() {
        int total = displayQueue.size();
        queueCountLabel.setText(total == 0
                ? "Queue"
                : "Queue \u00B7 " + total + " song" + (total == 1 ? "" : "s"));
    }

    private static String queueSectionDividerColor() {
        return AppTheme.isLightMode() ? "rgba(15,23,42,0.14)" : "rgba(238,238,246,0.12)";
    }

    // ── Animation ──────────────────────────────────────────────

    private void openPanel() {
        animatingClose = false;
        queueOverlayRoot.setManaged(true);
        queueOverlayRoot.setVisible(true);
        queueOverlayRoot.setOpacity(0);
        queuePanel.setTranslateX(420);

        FadeTransition fade = new FadeTransition(Duration.millis(180), queueOverlayRoot);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(250), queuePanel);
        slide.setToX(0);
        new ParallelTransition(fade, slide).play();
    }

    private void closePanel() {
        if (!queueOverlayRoot.isVisible() || animatingClose) return;
        animatingClose = true;

        FadeTransition fade = new FadeTransition(Duration.millis(140), queueOverlayRoot);
        fade.setToValue(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(180), queuePanel);
        slide.setToX(420);

        ParallelTransition anim = new ParallelTransition(fade, slide);
        anim.setOnFinished(e -> {
            queueOverlayRoot.setVisible(false);
            queueOverlayRoot.setManaged(false);
            animatingClose = false;
        });
        anim.play();
    }

    private void openArtistProfile(String artist) {
        if (artist == null || artist.isBlank()) return;
        SessionManager.setSelectedArtist(artist.trim());
        try {
            SceneUtil.switchScene(queueListView, FxmlResources.ARTIST_PROFILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Data model ─────────────────────────────────────────────

    private enum QueueSeg {
        USER,
        PLAYLIST,
        AUTOPLAY
    }

    record QueueEntry(Song song, int upcomingIndex, QueueSeg seg) {
        boolean userQueued() {
            return seg == QueueSeg.USER;
        }
    }

    // ── Cell ───────────────────────────────────────────────────

    private class UnifiedQueueCell extends ListCell<QueueEntry> {

        private final Region queueDropTopLine = new Region();
        private final Region queueDropBottomLine = new Region();
        private final VBox queueRowBox = new VBox(0);
        private boolean dropInsertAbove;

        UnifiedQueueCell() {
            String lineStyle =
                    "-fx-background-color: #a78bfa; -fx-min-height: 3; -fx-max-height: 3; -fx-pref-height: 3;";
            queueDropTopLine.setStyle(lineStyle);
            queueDropBottomLine.setStyle(lineStyle);
            hideDropInsert();
            setupDragAndDrop();
        }

        private void showDropInsert(boolean above) {
            dropInsertAbove = above;
            queueDropTopLine.setManaged(above);
            queueDropTopLine.setVisible(above);
            queueDropBottomLine.setManaged(!above);
            queueDropBottomLine.setVisible(!above);
        }

        private void hideDropInsert() {
            queueDropTopLine.setManaged(false);
            queueDropTopLine.setVisible(false);
            queueDropBottomLine.setManaged(false);
            queueDropBottomLine.setVisible(false);
        }

        @Override
        protected void updateItem(QueueEntry entry, boolean empty) {
            super.updateItem(entry, empty);
            if (empty || entry == null) {
                hideDropInsert();
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent;");
                return;
            }

            Song song = entry.song();

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(7, 10, 7, 10));
            row.setStyle(CellStyleKit.getRowDefault() + " -fx-cursor: hand;");

            Label indexLabel = new Label(String.valueOf(getIndex() + 1));
            indexLabel.setMinWidth(22);
            indexLabel.setAlignment(Pos.CENTER);
            indexLabel.setStyle("-fx-text-fill: " + CellStyleKit.getTextMuted()
                    + "; -fx-font-size: 12px; -fx-font-weight: bold;");

            VBox info = new VBox(1);
            HBox.setHgrow(info, Priority.ALWAYS);
            info.setMaxWidth(220);

            Label title = new Label(song.title());
            title.setStyle("-fx-text-fill: " + CellStyleKit.getTextPrimary()
                    + "; -fx-font-size: 13px; -fx-font-weight: bold;");
            title.setMaxWidth(220);

            HBox meta = CellStyleKit.songMetaLine(
                    song.artist(), null, QueuePanelController.this::openArtistProfile);
            meta.setMaxWidth(220);

            info.getChildren().add(title);
            if (!meta.getChildren().isEmpty()) {
                info.getChildren().add(meta);
            }

            Label duration = new Label(formatDuration(song.durationSeconds()));
            duration.setStyle("-fx-text-fill: " + CellStyleKit.getTextMuted()
                    + "; -fx-font-size: 11px;");

            row.getChildren().addAll(indexLabel, info, duration);

            if (entry.userQueued()) {
                Button removeBtn = new Button("\u2715");
                removeBtn.setMinSize(26, 26);
                removeBtn.setMaxSize(26, 26);
                removeBtn.setPrefSize(26, 26);
                removeBtn.setFocusTraversable(false);
                removeBtn.setStyle(REMOVE_DEFAULT);
                removeBtn.setOnAction(e -> {
                    int idx = entry.upcomingIndex();
                    if (idx >= 0 && idx < player.getUserQueueSize()) {
                        player.removeFromQueue(idx);
                    }
                    e.consume();
                });
                removeBtn.setOnMouseEntered(e -> removeBtn.setStyle(REMOVE_HOVER));
                removeBtn.setOnMouseExited(e -> removeBtn.setStyle(REMOVE_DEFAULT));
                row.getChildren().add(removeBtn);
            }

            if (rowReorderable(entry.seg())) {
                Label dragHandle = new Label("\u2261");
                dragHandle.setMinWidth(20);
                dragHandle.setAlignment(Pos.CENTER);
                dragHandle.setStyle(
                        "-fx-text-fill: " + CellStyleKit.getTextMuted() + "; -fx-font-size: 18px; -fx-cursor: move;");
                dragHandle.setOnMouseEntered(e ->
                        dragHandle.setStyle(
                                "-fx-text-fill: "
                                        + CellStyleKit.getTextSecondary()
                                        + "; -fx-font-size: 18px; -fx-cursor: move;"));
                dragHandle.setOnMouseExited(e ->
                        dragHandle.setStyle(
                                "-fx-text-fill: "
                                        + CellStyleKit.getTextMuted()
                                        + "; -fx-font-size: 18px; -fx-cursor: move;"));
                row.getChildren().add(dragHandle);
            }

            row.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    player.playFromUpcomingQueue(entry.upcomingIndex());
                    refreshAll();
                    e.consume();
                }
            });

            row.setOnMouseEntered(e -> row.setStyle(CellStyleKit.getRowHover() + " -fx-cursor: hand;"));
            row.setOnMouseExited(e -> row.setStyle(CellStyleKit.getRowDefault() + " -fx-cursor: hand;"));

            boolean isNextUpRow = entry.upcomingIndex() == 0;
            boolean restDivider =
                    entry.upcomingIndex() == 1 && displayQueue.size() > 1;

            List<Node> stack = new ArrayList<>();
            if (isNextUpRow) {
                Label nextHeader = new Label("Next up");
                nextHeader.setPadding(new Insets(2, 10, 2, 10));
                nextHeader.setMaxWidth(Double.MAX_VALUE);
                nextHeader.setStyle(
                        "-fx-text-fill: "
                                + CellStyleKit.getAccentTitle()
                                + "; -fx-font-size: 11px; -fx-font-weight: bold;");
                stack.add(nextHeader);
            }
            if (restDivider) {
                Region gap = new Region();
                gap.setMinHeight(8);
                gap.setPrefHeight(8);
                gap.setMaxHeight(8);
                Region line = new Region();
                line.setMinHeight(1);
                line.setPrefHeight(1);
                line.setMaxHeight(1);
                line.setStyle("-fx-background-color: " + queueSectionDividerColor() + ";");
                VBox sep = new VBox(0, gap, line);
                sep.setFillWidth(true);
                stack.add(sep);
            }
            stack.add(queueDropTopLine);
            stack.add(row);
            stack.add(queueDropBottomLine);
            queueRowBox.getChildren().setAll(stack);
            setGraphic(queueRowBox);
            setText(null);
            setStyle("-fx-background-color: transparent; -fx-padding: 1 0 1 0;");
        }

        private boolean rowReorderable(QueueSeg seg) {
            int uq = player.getUserQueueSize();
            int pl = player.getPlaylistUpcomingCount();
            int ap = player.getAutoplayUpcomingCount();
            return switch (seg) {
                case USER -> uq > 1;
                case PLAYLIST -> pl > 1;
                case AUTOPLAY -> ap > 1;
            };
        }

        private void setupDragAndDrop() {
            setOnDragDetected(event -> {
                QueueEntry entry = getItem();
                if (entry == null || !rowReorderable(entry.seg())) {
                    return;
                }
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                db.setDragView(QUEUE_DRAG_GHOST, 0, 0);
                ClipboardContent cc = new ClipboardContent();
                cc.put(DRAG_INDEX, entry.upcomingIndex());
                db.setContent(cc);
                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getDragboard().hasContent(DRAG_INDEX)) {
                    QueueEntry target = getItem();
                    int fromIdx = (int) event.getDragboard().getContent(DRAG_INDEX);
                    if (target != null
                            && rowReorderable(target.seg())
                            && player.isSameUpcomingSegment(fromIdx, target.upcomingIndex())) {
                        event.acceptTransferModes(TransferMode.MOVE);
                        double fallback = queueListView.getFixedCellSize();
                        if (fallback <= 0) {
                            fallback = Math.max(56, prefHeight(-1));
                        }
                        double h = getHeight() > 0 ? getHeight() : fallback;
                        showDropInsert(event.getY() < h / 2);
                    } else {
                        hideDropInsert();
                    }
                }
                event.consume();
            });

            setOnDragExited(event -> hideDropInsert());

            setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(DRAG_INDEX)) {
                    int fromIdx = (int) db.getContent(DRAG_INDEX);
                    QueueEntry target = getItem();
                    if (target != null
                            && player.isSameUpcomingSegment(fromIdx, target.upcomingIndex())) {
                        int insertBefore =
                                dropInsertAbove ? target.upcomingIndex() : target.upcomingIndex() + 1;
                        insertBefore = clampUpcomingInsertBefore(insertBefore, target.seg());
                        player.reorderUpcomingSnapshot(fromIdx, insertBefore);
                        QueuePanelController.this.refreshAll();
                    }
                    event.setDropCompleted(true);
                } else {
                    event.setDropCompleted(false);
                }
                hideDropInsert();
                event.consume();
            });

            setOnDragDone(event -> {
                hideDropInsert();
                queueListView.refresh();
                event.consume();
            });
        }

        private int clampUpcomingInsertBefore(int insertBefore, QueueSeg seg) {
            int uq = player.getUserQueueSize();
            int pl = player.getPlaylistUpcomingCount();
            int ap = player.getAutoplayUpcomingCount();
            int lo =
                    switch (seg) {
                        case USER -> 0;
                        case PLAYLIST -> uq;
                        case AUTOPLAY -> uq + pl;
                    };
            int hi =
                    switch (seg) {
                        case USER -> uq;
                        case PLAYLIST -> uq + pl;
                        case AUTOPLAY -> uq + pl + ap;
                    };
            return Math.max(lo, Math.min(insertBefore, hi));
        }

        private String formatDuration(int seconds) {
            return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
        }

        private static final String REMOVE_DEFAULT =
                "-fx-background-color: transparent; -fx-text-fill: #5c5c78;"
                        + " -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2 5 2 5;";
        private static final String REMOVE_HOVER =
                "-fx-background-color: rgba(239,68,68,0.14); -fx-text-fill: #f87171;"
                        + " -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2 5 2 5;"
                        + " -fx-background-radius: 8;";
    }
}

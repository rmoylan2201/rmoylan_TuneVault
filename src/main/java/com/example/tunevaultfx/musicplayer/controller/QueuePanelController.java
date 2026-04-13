package com.example.tunevaultfx.musicplayer.controller;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.util.CellStyleKit;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

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

    @FXML private VBox userQueueSection;
    @FXML private Label userQueueCountBadge;
    @FXML private ListView<Song> userQueueListView;

    @FXML private VBox upNextSection;
    @FXML private Label upNextLabel;
    @FXML private ListView<Song> upNextListView;

    private final MusicPlayerController player = MusicPlayerController.getInstance();
    private final BooleanProperty visible = new SimpleBooleanProperty(false);
    private boolean animatingClose = false;

    private static final DataFormat SONG_INDEX = new DataFormat("application/x-queue-index");

    @FXML
    public void initialize() {
        queueOverlayRoot.setVisible(false);
        queueOverlayRoot.setManaged(false);
        queueOverlayRoot.setOpacity(0);
        queuePanel.setTranslateX(420);

        userQueueListView.setItems(player.getUserQueue());
        userQueueListView.setCellFactory(lv -> new QueueSongCell(true));
        userQueueListView.setPlaceholder(new Label("Queue is empty — add songs with \"Play Next\"") {{
            setStyle("-fx-text-fill: #58586e; -fx-font-size: 12px;");
        }});
        userQueueListView.setFixedCellSize(56);

        upNextListView.setCellFactory(lv -> new QueueSongCell(false));
        upNextListView.setPlaceholder(new Label("Nothing coming up") {{
            setStyle("-fx-text-fill: #58586e; -fx-font-size: 12px;");
        }});
        upNextListView.setFixedCellSize(56);

        player.currentSongProperty().addListener((obs, o, n) -> refreshAll());
        player.playingProperty().addListener((obs, o, n) -> refreshNowPlaying());
        player.getUserQueue().addListener((ListChangeListener<Song>) c -> refreshAll());

        visible.addListener((obs, o, n) -> {
            if (n) {
                refreshAll();
                openPanel();
            } else {
                closePanel();
            }
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

    public BooleanProperty visibleProperty() {
        return visible;
    }

    // ── FXML handlers ──────────────────────────────────────────

    @FXML private void handleBackdropClick() { visible.set(false); }
    @FXML private void handleClose() { visible.set(false); }
    @FXML private void handleConsumeClick() { /* absorb */ }

    @FXML
    private void handleClearQueue() {
        player.clearUserQueue();
        refreshAll();
    }

    // ── Refresh ────────────────────────────────────────────────

    private void refreshAll() {
        refreshNowPlaying();
        refreshUserQueue();
        refreshUpNext();
        refreshHeader();
    }

    private void refreshNowPlaying() {
        Song current = player.getCurrentSong();
        if (current == null) {
            nowPlayingTitle.setText("—");
            nowPlayingArtist.setText("");
            nowPlayingRow.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 14; -fx-padding: 10 14 10 14;");
        } else {
            nowPlayingTitle.setText(current.title());
            nowPlayingArtist.setText(current.artist());
            nowPlayingRow.setStyle("-fx-background-color: rgba(139,92,246,0.08); -fx-background-radius: 14; -fx-padding: 10 14 10 14;");
        }
    }

    private void refreshUserQueue() {
        int size = player.getUserQueueSize();
        userQueueCountBadge.setText(size > 0 ? String.valueOf(size) : "");
        userQueueCountBadge.setVisible(size > 0);
        userQueueCountBadge.setManaged(size > 0);
        clearQueueButton.setVisible(size > 0);
        clearQueueButton.setManaged(size > 0);
    }

    private void refreshUpNext() {
        ObservableList<Song> upcoming = player.getUpcomingQueueSnapshot();
        ObservableList<Song> filtered = FXCollections.observableArrayList();
        for (Song s : upcoming) {
            if (!player.getUserQueue().contains(s)) {
                filtered.add(s);
            }
        }

        upNextListView.setItems(filtered);

        boolean hasUpNext = !filtered.isEmpty();
        upNextSection.setVisible(hasUpNext);
        upNextSection.setManaged(hasUpNext);
    }

    private void refreshHeader() {
        int total = player.getUserQueueSize();
        queueCountLabel.setText(total == 0 ? "Queue" : "Queue · " + total + " song" + (total == 1 ? "" : "s"));
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

    // ── Custom cell with drag-reorder and remove ───────────────

    private class QueueSongCell extends ListCell<Song> {

        private final boolean editable;

        QueueSongCell(boolean editable) {
            this.editable = editable;

            if (editable) {
                setupDragAndDrop();
            }
        }

        @Override
        protected void updateItem(Song song, boolean empty) {
            super.updateItem(song, empty);
            if (empty || song == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent;");
                return;
            }

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 10, 6, 10));
            row.setStyle(CellStyleKit.ROW_DEFAULT);

            Label indexLabel = new Label(String.valueOf(getIndex() + 1));
            indexLabel.setMinWidth(22);
            indexLabel.setAlignment(Pos.CENTER);
            indexLabel.setStyle("-fx-text-fill: " + CellStyleKit.TEXT_MUTED + "; -fx-font-size: 12px; -fx-font-weight: bold;");

            VBox info = new VBox(1);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label title = new Label(song.title());
            title.setStyle("-fx-text-fill: " + CellStyleKit.TEXT_PRIMARY + "; -fx-font-size: 13px; -fx-font-weight: bold;");
            title.setMaxWidth(240);

            Label meta = new Label(CellStyleKit.songMeta(song.artist(), song.genre()));
            meta.setStyle("-fx-text-fill: " + CellStyleKit.TEXT_SECONDARY + "; -fx-font-size: 11px;");
            meta.setMaxWidth(240);

            Label duration = new Label(formatDuration(song.durationSeconds()));
            duration.setStyle("-fx-text-fill: " + CellStyleKit.TEXT_MUTED + "; -fx-font-size: 11px;");

            info.getChildren().addAll(title, meta);
            row.getChildren().addAll(indexLabel, info, duration);

            if (editable) {
                Label dragHandle = new Label("≡");
                dragHandle.setStyle("-fx-text-fill: #58586e; -fx-font-size: 16px; -fx-cursor: move;");
                dragHandle.setMinWidth(20);
                dragHandle.setAlignment(Pos.CENTER);

                Button removeBtn = new Button("✕");
                removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #58586e; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 2 6 2 6;");
                removeBtn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0) {
                        player.removeFromQueue(idx);
                    }
                });

                removeBtn.setOnMouseEntered(e -> removeBtn.setStyle("-fx-background-color: rgba(244,63,94,0.15); -fx-text-fill: #f43f5e; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 2 6 2 6; -fx-background-radius: 10;"));
                removeBtn.setOnMouseExited(e -> removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #58586e; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 2 6 2 6;"));

                row.getChildren().add(0, dragHandle);
                row.getChildren().add(removeBtn);
            }

            row.setOnMouseEntered(e -> row.setStyle(CellStyleKit.ROW_HOVER));
            row.setOnMouseExited(e -> row.setStyle(CellStyleKit.ROW_DEFAULT));

            setGraphic(row);
            setText(null);
            setStyle("-fx-background-color: transparent; -fx-padding: 1 0 1 0;");
        }

        private void setupDragAndDrop() {
            setOnDragDetected(event -> {
                if (getItem() == null) return;
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.put(SONG_INDEX, getIndex());
                db.setContent(cc);
                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasContent(SONG_INDEX)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            setOnDragEntered(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasContent(SONG_INDEX)) {
                    setStyle("-fx-background-color: rgba(139,92,246,0.12); -fx-padding: 1 0 1 0;");
                }
            });

            setOnDragExited(event -> {
                setStyle("-fx-background-color: transparent; -fx-padding: 1 0 1 0;");
            });

            setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SONG_INDEX)) {
                    int fromIdx = (int) db.getContent(SONG_INDEX);
                    int toIdx = getIndex();
                    if (fromIdx >= 0 && toIdx >= 0 && fromIdx != toIdx) {
                        player.moveInQueue(fromIdx, toIdx);
                    }
                    event.setDropCompleted(true);
                } else {
                    event.setDropCompleted(false);
                }
                event.consume();
            });

            setOnDragDone(event -> event.consume());
        }

        private String formatDuration(int seconds) {
            return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
        }
    }
}

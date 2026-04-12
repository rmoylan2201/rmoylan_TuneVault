package com.example.tunevaultfx.playlist.cell;

import com.example.tunevaultfx.core.Song;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Search the results cell with an add / remove toggle button for the selected playlist.
 */
public class SearchSongToggleCell extends ListCell<Song> {

    private final Predicate<Song> isSongInPlaylist;
    private final Consumer<Song> onToggleSong;

    private final HBox root = new HBox();
    private final VBox textBox = new VBox();
    private final Label titleLabel = new Label();
    private final Label artistLabel = new Label();
    private final Region spacer = new Region();
    private final Button actionButton = new Button();

    public SearchSongToggleCell(Predicate<Song> isSongInPlaylist, Consumer<Song> onToggleSong) {
        this.isSongInPlaylist = isSongInPlaylist;
        this.onToggleSong = onToggleSong;

        root.setSpacing(12);
        root.setPadding(new Insets(8, 10, 8, 10));
        root.setStyle("-fx-background-color: transparent; -fx-background-radius: 14;");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        titleLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 15px; -fx-font-weight: bold;");
        artistLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        textBox.setSpacing(4);
        textBox.getChildren().addAll(titleLabel, artistLabel);

        actionButton.setPrefWidth(42);
        actionButton.setPrefHeight(32);
        actionButton.setFocusTraversable(false);

        root.getChildren().addAll(textBox, spacer, actionButton);

        root.setOnMouseClicked(event -> {
            Song song = getItem();
            if (song == null || isEmpty()) {
                return;
            }

            toggleSong(song);
            event.consume();
        });

        setOnMousePressed(event -> {
            if (!isEmpty()) {
                getListView().getSelectionModel().clearSelection();
                event.consume();
            }
        });
    }

    @Override
    protected void updateItem(Song song, boolean empty) {
        super.updateItem(song, empty);

        if (empty || song == null) {
            setText(null);
            setGraphic(null);
            setStyle("-fx-background-color: transparent;");
            return;
        }

        titleLabel.setText(song.title());
        artistLabel.setText(song.artist());
        refreshActionButton(song);

        setText(null);
        setGraphic(root);
        setBackground(Background.EMPTY);
        setStyle("-fx-background-color: transparent; -fx-padding: 2 0 2 0;");
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(false);
    }

    private void toggleSong(Song song) {
        boolean wasInPlaylist = isSongInPlaylist.test(song);

        onToggleSong.accept(song);

        playClickFlash(!wasInPlaylist);

        if (getListView() != null) {
            getListView().refresh();
            getListView().getSelectionModel().clearSelection();
        }
    }

    private void refreshActionButton(Song song) {
        boolean alreadyInPlaylist = isSongInPlaylist.test(song);

        actionButton.setText(alreadyInPlaylist ? "✓" : "+");
        actionButton.setStyle(alreadyInPlaylist
                ? "-fx-background-color: #dbeafe; -fx-text-fill: #2563eb; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 16;"
                : "-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 16;");

        actionButton.setOnAction(event -> {
            toggleSong(song);
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
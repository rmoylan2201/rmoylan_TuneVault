package com.example.tunevaultfx.playlist.cell;

import com.example.tunevaultfx.core.Song;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;
/**
 * Custom ListView cell used to display a song row with a play button,
 * title, and artist.
 */
public class PlayableSongCell extends ListCell<Song> {

    private final Button playButton = new Button("▶");
    private final Label titleLabel = new Label();
    private final Label artistLabel = new Label();
    private final VBox textBox = new VBox(2, titleLabel, artistLabel);
    private final HBox row = new HBox(10, playButton, textBox);

    private final Consumer<Song> onPlay;

    public PlayableSongCell(Consumer<Song> onPlay) {
        this.onPlay = onPlay;

        playButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 18;");
        playButton.setPrefSize(34, 34);
        playButton.setMinSize(34, 34);
        playButton.setMaxSize(34, 34);

        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        artistLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        textBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        row.setAlignment(Pos.CENTER_LEFT);

        playButton.setOnAction(event -> {
            Song song = getItem();
            if (song != null && onPlay != null) {
                onPlay.accept(song);
            }
        });
    }

    @Override
    protected void updateItem(Song song, boolean empty) {
        super.updateItem(song, empty);

        if (empty || song == null) {
            setText(null);
            setGraphic(null);
        } else {
            titleLabel.setText(song.title());
            artistLabel.setText(song.artist());
            setText(null);
            setGraphic(row);
        }
    }
}
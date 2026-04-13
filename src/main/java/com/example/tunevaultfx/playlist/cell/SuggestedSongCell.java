package com.example.tunevaultfx.playlist.cell;

import com.example.tunevaultfx.core.Song;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Suggested song row — dark theme.
 *
 * The [+] button is hidden by default and fades in when the row is hovered,
 * so the layout looks clean at rest and reveals the action naturally.
 */
public class SuggestedSongCell extends ListCell<Song> {

    private final Consumer<Song> onAdd;
    private final Consumer<Song> onPlay;

    private final HBox      root       = new HBox(12);
    private final StackPane addWrapper = new StackPane();   // fixed-width slot so layout never shifts
    private final Button    addButton  = new Button("+");
    private final StackPane iconBox    = new StackPane();
    private final Label     iconLabel  = new Label("♫");
    private final VBox      textBox    = new VBox(3);
    private final Label     titleLabel = new Label();
    private final Label     metaLabel  = new Label();
    private final Region    spacer     = new Region();
    private final Button    playButton = new Button("▶  Play");

    // ── Styles ────────────────────────────────────────────────────

    private static final String ADD_HIDDEN =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: transparent;" +
                    "-fx-font-size: 18px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 18;";

    private static final String ADD_VISIBLE =
            "-fx-background-color: rgba(139,92,246,0.15);" +
                    "-fx-text-fill: #a78bfa;" +
                    "-fx-font-size: 18px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 18;" +
                    "-fx-border-color: rgba(139,92,246,0.22);" +
                    "-fx-border-radius: 18; -fx-border-width: 1;";

    private static final String ADD_HOVER =
            "-fx-background-color: #8b5cf6;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 18px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 18;";

    private static final String ADD_CONFIRM =
            "-fx-background-color: rgba(34,197,94,0.2);" +
                    "-fx-text-fill: #22c55e;" +
                    "-fx-font-size: 18px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 18;" +
                    "-fx-border-color: rgba(34,197,94,0.28);" +
                    "-fx-border-radius: 18; -fx-border-width: 1;";

    private static final String PLAY_DEFAULT =
            "-fx-background-color: rgba(34,197,94,0.12);" +
                    "-fx-text-fill: #22c55e;" +
                    "-fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 17;" +
                    "-fx-border-color: rgba(34,197,94,0.2);" +
                    "-fx-border-radius: 17; -fx-border-width: 1;" +
                    "-fx-padding: 0 14 0 14;";

    private static final String PLAY_HOVER =
            "-fx-background-color: #22c55e;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-background-radius: 17;" +
                    "-fx-padding: 0 14 0 14;";

    private static final String ROW_DEFAULT =
            "-fx-background-color: transparent; -fx-background-radius: 14;";

    private static final String ROW_HOVER =
            "-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 14;";

    // ─────────────────────────────────────────────────────────────

    public SuggestedSongCell(Consumer<Song> onAdd, Consumer<Song> onPlay) {
        this.onAdd  = onAdd;
        this.onPlay = onPlay;

        // ── Add button (starts invisible, fades in on row hover) ──
        addButton.setPrefSize(36, 36);
        addButton.setMinSize(36, 36);
        addButton.setMaxSize(36, 36);
        addButton.setFocusTraversable(false);
        addButton.setStyle(ADD_HIDDEN);
        addButton.setOpacity(0);

        // Wrapper has fixed size so removing the button doesn't shift layout
        addWrapper.setPrefSize(36, 36);
        addWrapper.setMinSize(36, 36);
        addWrapper.setMaxSize(36, 36);
        addWrapper.getChildren().add(addButton);

        // ── Icon box ──────────────────────────────────────────────
        iconBox.setPrefSize(40, 40);
        iconBox.setMinSize(40, 40);
        iconBox.setMaxSize(40, 40);
        iconBox.setStyle(
                "-fx-background-color: rgba(139,92,246,0.12);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(139,92,246,0.18);" +
                        "-fx-border-radius: 12; -fx-border-width: 1;");
        iconLabel.setStyle("-fx-font-size: 17px; -fx-text-fill: #6b5fa6;");
        iconBox.getChildren().add(iconLabel);
        StackPane.setAlignment(iconLabel, Pos.CENTER);

        // ── Text ──────────────────────────────────────────────────
        titleLabel.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");
        metaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #52525b;");
        textBox.getChildren().addAll(titleLabel, metaLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        HBox.setHgrow(spacer,  Priority.ALWAYS);

        // ── Play button ───────────────────────────────────────────
        playButton.setPrefHeight(34);
        playButton.setFocusTraversable(false);
        playButton.setStyle(PLAY_DEFAULT);
        playButton.setOnMouseEntered(e -> playButton.setStyle(PLAY_HOVER));
        playButton.setOnMouseExited(e  -> playButton.setStyle(PLAY_DEFAULT));

        // ── Row ───────────────────────────────────────────────────
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(10, 14, 10, 14));
        root.setStyle(ROW_DEFAULT);
        root.getChildren().addAll(addWrapper, iconBox, textBox, spacer, playButton);

        // ── Row hover — reveal + button ───────────────────────────
        root.setOnMouseEntered(e -> {
            root.setStyle(ROW_HOVER);
            fadeButton(addButton, true);
            addButton.setStyle(ADD_VISIBLE);
        });
        root.setOnMouseExited(e -> {
            root.setStyle(ROW_DEFAULT);
            // Only hide if not mid-confirm flash
            if (addButton.getText().equals("+")) {
                fadeButton(addButton, false);
                // Delay style reset until fade completes
                PauseTransition delay = new PauseTransition(Duration.millis(180));
                delay.setOnFinished(ev -> addButton.setStyle(ADD_HIDDEN));
                delay.play();
            }
        });

        // ── Add button hover (within row hover) ───────────────────
        addButton.setOnMouseEntered(e -> {
            if (addButton.getOpacity() > 0) addButton.setStyle(ADD_HOVER);
            e.consume();
        });
        addButton.setOnMouseExited(e -> {
            if (addButton.getOpacity() > 0) addButton.setStyle(ADD_VISIBLE);
            e.consume();
        });

        // ── Actions ───────────────────────────────────────────────
        addButton.setOnAction(e -> {
            Song song = getItem();
            if (song != null && onAdd != null) {
                flashAddConfirm();
                onAdd.accept(song);
            }
            e.consume();
        });

        playButton.setOnAction(e -> {
            Song song = getItem();
            if (song != null && onPlay != null) onPlay.accept(song);
            e.consume();
        });

        setOnMousePressed(e -> {
            if (!isEmpty() && getListView() != null) {
                getListView().getSelectionModel().clearSelection();
                e.consume();
            }
        });
    }

    // ─────────────────────────────────────────────────────────────

    @Override
    protected void updateItem(Song song, boolean empty) {
        super.updateItem(song, empty);

        if (empty || song == null) {
            setText(null);
            setGraphic(null);
            setBackground(Background.EMPTY);
            setStyle("-fx-background-color: transparent;");
            return;
        }

        titleLabel.setText(song.title());

        StringBuilder meta = new StringBuilder();
        if (song.artist() != null && !song.artist().isBlank()) meta.append(song.artist());
        if (song.genre()  != null && !song.genre().isBlank()) {
            if (!meta.isEmpty()) meta.append(" \u00B7 ");
            meta.append(song.genre());
        }
        metaLabel.setText(meta.toString());

        // Reset add button to hidden state
        addButton.setText("+");
        addButton.setStyle(ADD_HIDDEN);
        addButton.setOpacity(0);

        setText(null);
        setGraphic(root);
        setBackground(Background.EMPTY);
        setStyle("-fx-background-color: transparent; -fx-padding: 2 0 2 0;");
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(false);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void fadeButton(Button btn, boolean in) {
        FadeTransition fade = new FadeTransition(Duration.millis(160), btn);
        fade.setToValue(in ? 1.0 : 0.0);
        fade.play();
    }

    private void flashAddConfirm() {
        addButton.setText("✓");
        addButton.setStyle(ADD_CONFIRM);
        PauseTransition pause = new PauseTransition(Duration.millis(700));
        pause.setOnFinished(e -> {
            addButton.setText("+");
            addButton.setStyle(ADD_VISIBLE);
        });
        pause.play();
    }
}

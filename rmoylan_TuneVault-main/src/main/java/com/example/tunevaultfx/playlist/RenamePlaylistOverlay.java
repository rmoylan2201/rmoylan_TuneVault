package com.example.tunevaultfx.playlist;

import com.example.tunevaultfx.playlist.service.PlaylistService;
import com.example.tunevaultfx.user.UserProfile;
import com.example.tunevaultfx.util.OverlayTheme;
import com.example.tunevaultfx.util.SceneUtil;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Modal overlay to rename a user playlist. */
public final class RenamePlaylistOverlay {

    private RenamePlaylistOverlay() {}

    public static void show(
            Scene scene,
            UserProfile profile,
            PlaylistService playlistService,
            String currentName,
            Runnable onRenamed) {
        if (scene == null || profile == null || playlistService == null || currentName == null) {
            return;
        }

        StackPane backdrop = new StackPane();
        backdrop.setStyle(OverlayTheme.backdrop());

        VBox card = new VBox(8);
        card.setMaxWidth(320);
        card.setMaxHeight(220);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle(OverlayTheme.card());
        card.setOnMouseClicked(e -> e.consume());

        Label title = new Label("Edit playlist info");
        title.setStyle(OverlayTheme.title());

        TextField nameField = new TextField(currentName);
        nameField.setPromptText("Playlist name");
        nameField.setStyle(OverlayTheme.createPlaylistField());
        nameField.setPrefHeight(38);
        nameField.setMaxHeight(38);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setMaxHeight(44);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(OverlayTheme.secondaryButton());
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(OverlayTheme.secondaryButtonHover()));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(OverlayTheme.secondaryButton()));

        Button saveBtn = new Button("Save");
        saveBtn.setStyle(OverlayTheme.primaryButton());
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(OverlayTheme.primaryButtonHover()));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(OverlayTheme.primaryButton()));

        HBox actions = new HBox(10, cancelBtn, saveBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(saveBtn, Priority.ALWAYS);
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        Runnable closeOverlay =
                () -> {
                    FadeTransition ft = new FadeTransition(Duration.millis(140), backdrop);
                    ft.setToValue(0);
                    ft.setOnFinished(
                            e -> {
                                javafx.scene.Parent p = backdrop.getParent();
                                if (p instanceof StackPane sp) {
                                    sp.getChildren().remove(backdrop);
                                }
                            });
                    ft.play();
                };

        Runnable doSave =
                () -> {
                    String name = nameField.getText() == null ? "" : nameField.getText().trim();
                    if (name.isEmpty()) {
                        errorLabel.setText("Playlist name cannot be empty.");
                        errorLabel.setVisible(true);
                        errorLabel.setManaged(true);
                        return;
                    }
                    if (name.equals(currentName)) {
                        closeOverlay.run();
                        return;
                    }
                    if (!playlistService.renamePlaylist(profile, currentName, name)) {
                        errorLabel.setText("Could not rename — that name may already exist.");
                        errorLabel.setVisible(true);
                        errorLabel.setManaged(true);
                        return;
                    }
                    if (onRenamed != null) {
                        onRenamed.run();
                    }
                    closeOverlay.run();
                };

        saveBtn.setOnAction(e -> doSave.run());
        cancelBtn.setOnAction(e -> closeOverlay.run());
        backdrop.setOnMouseClicked(e -> closeOverlay.run());

        backdrop.addEventFilter(
                KeyEvent.KEY_PRESSED,
                e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        closeOverlay.run();
                        e.consume();
                    }
                });

        nameField.setOnKeyPressed(
                e -> {
                    if (e.getCode() == KeyCode.ENTER) {
                        doSave.run();
                    }
                    if (e.getCode() == KeyCode.ESCAPE) {
                        closeOverlay.run();
                    }
                });

        card.getChildren().addAll(title, nameField, errorLabel, actions);
        StackPane.setAlignment(card, Pos.CENTER);
        backdrop.getChildren().add(card);

        if (scene.getRoot() instanceof StackPane sp) {
            sp.getChildren().add(backdrop);
        } else {
            StackPane wrapper = new StackPane();
            wrapper.getChildren().addAll(scene.getRoot(), backdrop);
            scene.setRoot(wrapper);
            SceneUtil.applySavedTheme(scene);
        }

        backdrop.setOpacity(0);
        card.setTranslateY(20);
        FadeTransition fade = new FadeTransition(Duration.millis(180), backdrop);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(200), card);
        slide.setToY(0);
        new ParallelTransition(fade, slide).play();

        Platform.runLater(
                () -> {
                    nameField.requestFocus();
                    nameField.selectAll();
                });
    }
}

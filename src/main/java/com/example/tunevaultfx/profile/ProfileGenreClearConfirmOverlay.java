package com.example.tunevaultfx.profile;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Themed confirmation before clearing saved Find Your Genre data (replaces stock {@code Alert}).
 */
public final class ProfileGenreClearConfirmOverlay {

    private ProfileGenreClearConfirmOverlay() {}

    public static void show(Scene scene, Runnable onConfirm) {
        if (scene == null || onConfirm == null) {
            return;
        }

        StackPane backdrop = new StackPane();
        backdrop.setStyle(OverlayTheme.backdrop());

        VBox card = new VBox(12);
        card.setMaxWidth(400);
        card.setPadding(new Insets(22, 24, 20, 24));
        card.setStyle(OverlayTheme.card());
        card.setOnMouseClicked(e -> e.consume());

        Label title = new Label("Clear Find Your Genre?");
        title.setStyle(OverlayTheme.title());
        title.setWrapText(true);

        Label body = new Label(
                "This removes only your saved quiz blend from the database. Listening history, playlists, "
                        + "and play counts stay the same. You can retake the quiz anytime.");
        body.setStyle(OverlayTheme.subtitle());
        body.setWrapText(true);
        body.setMaxWidth(360);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(OverlayTheme.secondaryButton());
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(OverlayTheme.secondaryButtonHover()));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(OverlayTheme.secondaryButton()));

        Button clearBtn = new Button("Clear saved blend");
        clearBtn.setStyle(OverlayTheme.dangerButton());
        clearBtn.setOnMouseEntered(e -> clearBtn.setStyle(OverlayTheme.dangerButtonHover()));
        clearBtn.setOnMouseExited(e -> clearBtn.setStyle(OverlayTheme.dangerButton()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(10, spacer, cancelBtn, clearBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(4, 0, 0, 0));

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

        clearBtn.setOnAction(
                e -> {
                    onConfirm.run();
                    closeOverlay.run();
                });

        card.getChildren().addAll(title, body, actions);
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
        card.setTranslateY(16);
        FadeTransition fade = new FadeTransition(Duration.millis(180), backdrop);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(200), card);
        slide.setToY(0);
        new ParallelTransition(fade, slide).play();

        Platform.runLater(cancelBtn::requestFocus);
    }
}

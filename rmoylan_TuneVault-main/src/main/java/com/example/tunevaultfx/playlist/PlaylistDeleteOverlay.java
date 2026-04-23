package com.example.tunevaultfx.playlist;

import com.example.tunevaultfx.playlist.service.PlaylistService;
import com.example.tunevaultfx.user.UserProfile;
import com.example.tunevaultfx.util.CellStyleKit;
import com.example.tunevaultfx.util.OverlayTheme;
import com.example.tunevaultfx.util.ToastUtil;
import com.example.tunevaultfx.util.SceneUtil;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Confirmation overlay for deleting a playlist. */
public final class PlaylistDeleteOverlay {

    private PlaylistDeleteOverlay() {}

    public static void show(
            Scene scene,
            UserProfile profile,
            PlaylistService playlistService,
            String playlistName,
            Runnable onDeleted) {
        if (scene == null || profile == null || playlistService == null || playlistName == null) {
            return;
        }

        StackPane backdrop = new StackPane();
        backdrop.setStyle(OverlayTheme.backdrop());

        VBox card = new VBox(8);
        card.setMaxWidth(300);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setPadding(new Insets(12, 16, 12, 16));
        card.setStyle(OverlayTheme.card());
        card.setOnMouseClicked(e -> e.consume());

        Label title = new Label("Delete playlist?");
        title.setStyle(compactTitleStyle());

        Label msg =
                new Label(
                        "Are you sure you want to delete \u201c"
                                + playlistName
                                + "\u201d? This cannot be undone.");
        msg.setWrapText(true);
        msg.setMaxWidth(268);
        msg.setStyle(compactMessageStyle());

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(compactSecondaryButton());
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(compactSecondaryButtonHover()));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(compactSecondaryButton()));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(compactDangerButton());
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(compactDangerButtonHover()));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(compactDangerButton()));

        HBox actions = new HBox(8, cancelBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

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
        deleteBtn.setOnAction(
                e -> {
                    if (!playlistService.deletePlaylist(profile, playlistName)) {
                        ToastUtil.error(scene, "This playlist cannot be deleted.");
                    } else if (onDeleted != null) {
                        onDeleted.run();
                    }
                    closeOverlay.run();
                    e.consume();
                });
        backdrop.setOnMouseClicked(e -> closeOverlay.run());

        backdrop.addEventFilter(
                KeyEvent.KEY_PRESSED,
                e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        closeOverlay.run();
                        e.consume();
                    }
                });

        card.getChildren().addAll(title, msg, actions);
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
    }

    private static String compactTitleStyle() {
        return "-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: "
                + CellStyleKit.getTextPrimary()
                + ";";
    }

    private static String compactMessageStyle() {
        return "-fx-font-size: 11px; -fx-text-fill: "
                + CellStyleKit.getTextSecondary()
                + "; -fx-line-spacing: 2px;";
    }

    private static String compactSecondaryButton() {
        return OverlayTheme.secondaryButton()
                .replace("-fx-font-size: 13px", "-fx-font-size: 12px")
                .replace("-fx-padding: 10 18 10 18", "-fx-padding: 6 12 6 12");
    }

    private static String compactSecondaryButtonHover() {
        return OverlayTheme.secondaryButtonHover()
                .replace("-fx-font-size: 13px", "-fx-font-size: 12px")
                .replace("-fx-padding: 10 18 10 18", "-fx-padding: 6 12 6 12");
    }

    private static String compactDangerButton() {
        return OverlayTheme.dangerButton()
                .replace("-fx-font-size: 13px", "-fx-font-size: 12px")
                .replace("-fx-padding: 10 18 10 18", "-fx-padding: 6 12 6 12");
    }

    private static String compactDangerButtonHover() {
        return OverlayTheme.dangerButtonHover()
                .replace("-fx-font-size: 13px", "-fx-font-size: 12px")
                .replace("-fx-padding: 10 18 10 18", "-fx-padding: 6 12 6 12");
    }
}

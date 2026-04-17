package com.example.tunevaultfx.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Slide-in toast notifications anchored to the top of the current scene.
 * <p>
 * Usage:
 * <pre>
 *   ToastUtil.success(someNode.getScene(), "Song added to queue");
 *   ToastUtil.error(someNode.getScene(), "Could not save playlist");
 *   ToastUtil.info(someNode.getScene(), "3 songs imported");
 *   ToastUtil.warning(someNode.getScene(), "You can only pin 3 playlists.");
 * </pre>
 */
public final class ToastUtil {

    private ToastUtil() {}

    private static final Duration SLIDE_IN  = Duration.millis(250);
    private static final Duration DISPLAY   = Duration.millis(2400);
    /** Longer hold for messages that need a deliberate read (limits, warnings). */
    private static final Duration DISPLAY_PROMINENT = Duration.millis(5200);
    private static final Duration SLIDE_OUT = Duration.millis(200);

    public static void success(Scene scene, String message) {
        show(scene, message, DISPLAY, 460, "toast", "toast-success");
    }

    public static void error(Scene scene, String message) {
        show(scene, message, DISPLAY, 460, "toast", "toast-error");
    }

    public static void info(Scene scene, String message) {
        show(scene, message, DISPLAY, 460, "toast", "toast-info");
    }

    /**
     * High-contrast toast that stays longer — use for important limits or policy messages
     * that are easy to miss with a standard info toast.
     */
    public static void warning(Scene scene, String message) {
        show(scene, message, DISPLAY_PROMINENT, 540, "toast", "toast-warning");
    }

    private static void show(
            Scene scene, String message, Duration holdDuration, double maxWidth, String... cssClasses) {
        if (scene == null || scene.getRoot() == null) return;

        Label toast = new Label(message);
        toast.getStyleClass().addAll(cssClasses);
        toast.setMouseTransparent(true);
        toast.setMaxWidth(maxWidth);
        toast.setWrapText(true);

        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        toast.setTranslateY(-50);
        toast.setOpacity(0);

        if (scene.getRoot() instanceof StackPane sp) {
            sp.getChildren().add(toast);
            animate(toast, sp, holdDuration);
        } else {
            StackPane wrapper = new StackPane();
            wrapper.getChildren().addAll(scene.getRoot(), toast);
            scene.setRoot(wrapper);
            SceneUtil.applySavedTheme(scene);
            animate(toast, wrapper, holdDuration);
        }
    }

    private static void animate(Label toast, StackPane parent, Duration holdDuration) {
        FadeTransition fadeIn = new FadeTransition(SLIDE_IN, toast);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(SLIDE_IN, toast);
        slideIn.setToY(20);

        ParallelTransition enter = new ParallelTransition(fadeIn, slideIn);

        PauseTransition hold = new PauseTransition(holdDuration);

        FadeTransition fadeOut = new FadeTransition(SLIDE_OUT, toast);
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(SLIDE_OUT, toast);
        slideOut.setToY(-30);

        ParallelTransition exit = new ParallelTransition(fadeOut, slideOut);
        exit.setOnFinished(e -> parent.getChildren().remove(toast));

        enter.setOnFinished(e -> hold.play());
        hold.setOnFinished(e -> exit.play());
        enter.play();
    }
}

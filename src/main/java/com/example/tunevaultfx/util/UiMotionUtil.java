package com.example.tunevaultfx.util;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.List;

public final class UiMotionUtil {

    private UiMotionUtil() {}

    public static void applyHoverLift(Node node) {
        if (node == null) {
            return;
        }

        node.setOnMouseEntered(e -> {
            node.setTranslateY(-2);
            node.setScaleX(1.01);
            node.setScaleY(1.01);
        });
        node.setOnMouseExited(e -> {
            node.setTranslateY(0);
            node.setScaleX(1.0);
            node.setScaleY(1.0);
        });
    }

    public static void playStaggeredEntrance(List<? extends Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        int idx = 0;
        for (Node node : nodes) {
            if (node == null) {
                idx++;
                continue;
            }

            node.setOpacity(0);
            node.setTranslateY(10);

            FadeTransition fade = new FadeTransition(Duration.millis(280), node);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(Duration.millis(idx * 55L));

            TranslateTransition slide = new TranslateTransition(Duration.millis(280), node);
            slide.setFromY(10);
            slide.setToY(0);
            slide.setDelay(Duration.millis(idx * 55L));

            new ParallelTransition(fade, slide).play();
            idx++;
        }
    }
}

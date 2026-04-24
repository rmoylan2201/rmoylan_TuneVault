package com.example.tunevaultfx.profile.media;

import javafx.scene.Scene;
import javafx.stage.Stage;

/** Shared stylesheet + light-theme sync for profile crop modals. */
public final class ProfileCropSceneStyler {

    private ProfileCropSceneStyler() {}

    public static void apply(Scene scene, Stage owner) {
        try {
            var url = ProfileCropSceneStyler.class.getResource("/com/example/tunevaultfx/app.css");
            if (url != null) {
                scene.getStylesheets().add(url.toExternalForm());
            }
        } catch (Exception ignored) {
        }
        if (owner != null
                && owner.getScene() != null
                && owner.getScene().getRoot() != null
                && owner.getScene().getRoot().getStyleClass().contains("theme-light")) {
            scene.getRoot().getStyleClass().add("theme-light");
        }
    }
}

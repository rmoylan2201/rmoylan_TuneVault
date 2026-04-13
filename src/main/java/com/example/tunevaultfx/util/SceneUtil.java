package com.example.tunevaultfx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Handles scene switching throughout the application.
 *
 * Changes:
 *  - Stage stays maximized across every scene switch (no more manual resizing).
 *  - sizeToScene() is removed — it was shrinking the window to FXML preferred
 *    sizes instead of keeping the maximized state.
 *  - Global CSS stylesheet is re-applied to every new scene so all pages
 *    inherit the dark theme consistently.
 *  - Minimum dimensions are enforced if the user un-maximizes.
 */
public final class SceneUtil {

    private static final String CSS_PATH = "/com/example/tunevaultfx/app.css";
    private static final String FXML_BASE = "/com/example/tunevaultfx/";

    private SceneUtil() {}

    public static void switchScene(Node sourceNode, String fxmlFile) throws IOException {
        Stage stage = (Stage) sourceNode.getScene().getWindow();

        // Preserve current maximized state before switching
        boolean wasMaximized = stage.isMaximized();

        FXMLLoader loader = new FXMLLoader(
                SceneUtil.class.getResource(FXML_BASE + fxmlFile));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        // Apply global stylesheet to every scene — without this, pages loaded
        // after the first one lose the dark theme
        String cssUrl = SceneUtil.class.getResource(CSS_PATH).toExternalForm();
        scene.getStylesheets().add(cssUrl);

        stage.setScene(scene);

        // Enforce minimum window size in case user un-maximizes
        stage.setMinWidth(1180);
        stage.setMinHeight(780);

        // Restore maximized state — this is what was being lost before,
        // causing the window to shrink to FXML preferred size on every switch
        if (wasMaximized) {
            stage.setMaximized(true);
        } else {
            // If user had un-maximized, keep their window size (don't force resize)
            if (stage.getWidth() < 1180)  stage.setWidth(1280);
            if (stage.getHeight() < 780)  stage.setHeight(820);
            stage.centerOnScreen();
        }

        stage.show();
    }
}

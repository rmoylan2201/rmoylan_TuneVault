package com.example.tunevaultfx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneUtil {

    private static final String CSS_PATH = "/com/example/tunevaultfx/app.css";
    private static final String FXML_BASE = "/com/example/tunevaultfx/";

    private SceneUtil() {}

    public static void switchScene(Node sourceNode, String fxmlFile) throws IOException {
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        Scene scene = stage.getScene();

        FXMLLoader loader = new FXMLLoader(
                SceneUtil.class.getResource(FXML_BASE + fxmlFile));
        Parent root = loader.load();

        String cssUrl = SceneUtil.class.getResource(CSS_PATH).toExternalForm();
        if (!scene.getStylesheets().contains(cssUrl)) {
            scene.getStylesheets().add(cssUrl);
        }

        scene.setRoot(root);
    }
}

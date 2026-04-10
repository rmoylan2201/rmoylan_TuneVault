package com.example.tunevaultfx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility class for switching between JavaFX scenes.
 * Helps controllers load and display different screens.
 */
public class SceneUtil {

    private SceneUtil() {
    }

    public static void switchScene(Node node, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                SceneUtil.class.getResource("/com/example/tunevaultfx/" + fxmlFile)
        );

        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
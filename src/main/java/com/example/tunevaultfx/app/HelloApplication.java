package com.example.tunevaultfx.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Starts the JavaFX application.
 * This class loads the first screen and creates the main application window.
 * It is the main JavaFX entry point of the app.
 */
public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxmlUrl = HelloApplication.class.getResource("/com/example/tunevaultfx/login-page.fxml");

        if (fxmlUrl == null) {
            throw new IllegalStateException("Could not find login-page.fxml at /com/example/tunevaultfx/login-page.fxml");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("TuneVault");
        stage.setScene(scene);
        stage.show();
    }
}
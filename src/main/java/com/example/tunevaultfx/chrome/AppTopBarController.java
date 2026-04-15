package com.example.tunevaultfx.chrome;

import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.util.AppTheme;
import com.example.tunevaultfx.util.SceneUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.prefs.Preferences;

/** Thin header: theme toggle and log out (top-right). */
public class AppTopBarController {

    private static final Preferences PREFS = Preferences.userRoot().node("com/example/tunevaultfx/ui");
    private static final String KEY_THEME_LIGHT = "themeLight";

    @FXML private HBox topBarRoot;
    @FXML private Button themeToggleBtn;

    @FXML
    public void initialize() {
        syncThemeToggleLabel();
        topBarRoot.sceneProperty().addListener((obs, o, n) -> {
            if (n != null) {
                Platform.runLater(this::syncThemeToggleLabel);
            }
        });
    }

    @FXML
    private void toggleTheme(ActionEvent e) {
        boolean next = !PREFS.getBoolean(KEY_THEME_LIGHT, false);
        PREFS.putBoolean(KEY_THEME_LIGHT, next);
        Scene scene = themeToggleBtn.getScene();
        SceneUtil.applySavedTheme(scene);
        syncThemeToggleLabel();
        if (scene != null && scene.getRoot() != null) {
            AppTheme.refreshAllListViews(scene.getRoot());
        }
    }

    @FXML
    private void handleLogout(ActionEvent e) throws IOException {
        SessionManager.logout();
        SceneUtil.clearHistory();
        SceneUtil.switchSceneNoHistory((Node) e.getSource(), "login-page.fxml");
    }

    private void syncThemeToggleLabel() {
        boolean light = PREFS.getBoolean(KEY_THEME_LIGHT, false);
        themeToggleBtn.setText(light ? "Dark mode" : "Light mode");
    }
}

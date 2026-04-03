package com.example.tunevaultfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.io.IOException;

public class MainMenuController {

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        welcomeLabel.setText(user != null ? "Welcome, " + user.getUsername() : "Welcome to TuneVault");
    }

    @FXML
    private void openPlaylistsPage(ActionEvent e) throws IOException {
        SceneUtil.switchScene((Node) e.getSource(), "playlists-page.fxml");
    }

    @FXML
    private void openNowPlayingPage(ActionEvent e) throws IOException {
        SceneUtil.switchScene((Node) e.getSource(), "nowplaying-page.fxml");
    }

    @FXML
    private void openWrappedPage(ActionEvent e) throws IOException {
        SceneUtil.switchScene((Node) e.getSource(), "wrapped-page.fxml");
    }

    @FXML
    private void openFindYourGenrePage(ActionEvent e) throws IOException {
        SceneUtil.switchScene((Node) e.getSource(), "findyourgenre-page.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        SessionManager.logout();
        SceneUtil.switchScene((Node) event.getSource(), "login-page.fxml");
    }
}
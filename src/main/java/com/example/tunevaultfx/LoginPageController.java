
package com.example.tunevaultfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginPageController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void handleLogin(ActionEvent event) throws IOException {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Enter username and password.");
            return;
        }

        User user = UserStore.validateLogin(username, password);

        if (user == null) {
            statusLabel.setText("Invalid username or password.");
            return;
        }

        SessionManager.startSession(user);
        SceneUtil.switchScene((Node) event.getSource(), "main-menu.fxml");
    }

    @FXML
    private void handleCreateAccountPage(ActionEvent event) throws IOException {
        SceneUtil.switchScene((Node) event.getSource(), "create-account-page.fxml");
    }

    @FXML
    private void openForgotPasswordPage(ActionEvent event) throws IOException {
        SceneUtil.switchScene((Node) event.getSource(), "forgot-password-page.fxml");
    }
}
package com.example.tunevaultfx.controllers.auth;

import com.example.tunevaultfx.util.SceneUtil;
import com.example.tunevaultfx.user.User;
import com.example.tunevaultfx.user.UserStore;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
/**
 * Controls the create account page.
 * Reads new account information and creates a new user account.
 */
public class CreateAccountPageController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void handleCreateAccount(ActionEvent event) throws IOException {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        if (UserStore.usernameExists(username)) {
            statusLabel.setText("Username already exists.");
            return;
        }

        UserStore.saveUser(new User(username, email, password));
        statusLabel.setText("Account created.");

        SceneUtil.switchScene((Node) event.getSource(), "login-page.fxml");
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) throws IOException {
        SceneUtil.switchScene((Node) event.getSource(), "login-page.fxml");
    }
}
package com.example.tunevaultfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;

public class ForgotPasswordPageController {

    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    @FXML
    private void handleSubmit() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            statusLabel.setText("Please enter your email.");
            return;
        }

        statusLabel.setText("Password reset feature is not connected yet.");
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) throws IOException {
        SceneUtil.switchScene((Node) event.getSource(), "login-page.fxml");
    }
}
package com.example.tunevaultfx.auth;

import com.example.tunevaultfx.db.UserDAO;
import com.example.tunevaultfx.musicplayer.controller.MusicPlayerController;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.user.User;
import com.example.tunevaultfx.util.SceneUtil;
import com.example.tunevaultfx.view.FxmlResources;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controls the login page.
 * Supports login with either username or email.
 */
public class LoginPageController {

    @FXML private StackPane loginRoot;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        Platform.runLater(() -> usernameField.requestFocus());

        // Enter in either field submits (avoids a Scene-level filter, which would outlive this
        // controller after logout — same Scene instance gets a new root but kept stale closures).
        usernameField.setOnAction(e -> attemptLogin(usernameField));
        passwordField.setOnAction(e -> attemptLogin(passwordField));

        if (loginRoot != null) {
            loginRoot.addEventFilter(
                    KeyEvent.KEY_PRESSED,
                    event -> {
                        if (event.getCode() != KeyCode.ESCAPE) {
                            return;
                        }
                        usernameField.clear();
                        passwordField.clear();
                        clearStatus();
                        usernameField.requestFocus();
                        event.consume();
                    });
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        Node source = event != null && event.getSource() instanceof Node n ? n : loginButton;
        attemptLogin(source != null ? source : usernameField);
    }

    private void attemptLogin(Node navigationSource) {
        clearStatus();

        String loginInput = safeTrim(usernameField.getText());
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (loginInput.isBlank() || password.isBlank()) {
            showError("Please enter your username/email and password.");
            return;
        }

        Node nav = navigationSource != null ? navigationSource : loginButton;
        if (nav == null) {
            nav = usernameField;
        }

        try {
            User user = userDAO.authenticateUser(loginInput, password);

            if (user == null) {
                showError("Invalid credentials.");
                return;
            }

            MusicPlayerController.getInstance().resetForNewSession();
            SessionManager.startSession(user.getUsername());
            SceneUtil.clearHistory();
            SceneUtil.switchSceneNoHistory(nav, FxmlResources.MAIN_MENU);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("A database error occurred. Please try again.");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not open the home screen. If this persists, restart the app.");
        }
    }

    @FXML
    private void openCreateAccountPage(ActionEvent event) throws IOException {
        SceneUtil.switchSceneNoHistory((Node) event.getSource(), FxmlResources.AUTH_CREATE_ACCOUNT);
    }

    @FXML
    private void openForgotPasswordPage(ActionEvent event) throws IOException {
        SceneUtil.switchSceneNoHistory((Node) event.getSource(), FxmlResources.AUTH_FORGOT_PASSWORD);
    }

    @FXML
    private void handleCreateAccountPage(ActionEvent event) throws IOException {
        openCreateAccountPage(event);
    }

    @FXML
    private void handleForgotPasswordPage(ActionEvent event) throws IOException {
        openForgotPasswordPage(event);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.getStyleClass().clear();
    }

    private void showError(String message) {
        statusLabel.getStyleClass().setAll("status-error");
        statusLabel.setText(message);
    }
}
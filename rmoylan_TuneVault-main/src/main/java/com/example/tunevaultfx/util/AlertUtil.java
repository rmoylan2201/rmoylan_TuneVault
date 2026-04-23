package com.example.tunevaultfx.util;

import javafx.scene.control.Alert;

/**
 * Utility class for showing alert dialogs.
 * Used to display informational messages to the user.
 */
public class AlertUtil {

    private AlertUtil() {
    }

    public static void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package com.example.tunevaultfx;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class HelloController {
    public Button button2;
    public Button button1;
    @FXML
    private Label welcomeText;
    private int deltaY;

    private void debug(Event event, String message) {
        //Stub. You must replace all stub calls with your own code.
        if (event.getSource() instanceof Node node) {
            deltaY += 42;
            Point2D pos = node.localToScreen(0, deltaY);
            message = message + "\nid:" + node.getId();
            Tooltip tooltip = new Tooltip(message);
            tooltip.show(node.getScene().getWindow(), pos.getX(), pos.getY());
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> {
                tooltip.hide();
                deltaY -= 42;
            });
            pause.play();
        } else {
            message = message + "\n" + event.getSource();
            new Alert(Alert.AlertType.NONE, message, ButtonType.OK).show();
        }
        event.consume();
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}

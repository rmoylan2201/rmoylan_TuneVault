module com.example.tunevaultfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    exports com.example.tunevaultfx;
    opens com.example.tunevaultfx to javafx.fxml, com.fasterxml.jackson.databind;
}
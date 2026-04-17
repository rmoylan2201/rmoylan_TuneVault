package com.example.tunevaultfx.chrome;

import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Shared search query bound to the top-bar {@link TextField} on logged-in screens.
 * Survives navigation so the same text stays when switching pages until the user clears it.
 * <p>
 * The active {@link SearchPageController} registers a debounced subscriber so only one listener
 * is attached to the query property across scene reloads.
 */
public final class SearchBarState {

    private static final StringProperty QUERY = new SimpleStringProperty("");
    private static TextField boundField;
    private static Consumer<String> searchSubscriber;

    private static final PauseTransition DEBOUNCE = new PauseTransition(Duration.millis(280));

    static {
        DEBOUNCE.setOnFinished(
                e -> {
                    Consumer<String> sub = searchSubscriber;
                    if (sub != null) {
                        String v = QUERY.get();
                        sub.accept(v != null ? v : "");
                    }
                });
        QUERY.addListener(
                (obs, o, n) -> {
                    Consumer<String> sub = searchSubscriber;
                    String raw = n != null ? n : "";
                    // Clearing the bar should snap the search UI back to recents immediately — waiting
                    // on debounce felt like "nothing happens" until the user clicked elsewhere.
                    if (sub != null && raw.isBlank()) {
                        DEBOUNCE.stop();
                        sub.accept(raw);
                        return;
                    }
                    DEBOUNCE.playFromStart();
                });
    }

    private SearchBarState() {}

    public static StringProperty queryProperty() {
        return QUERY;
    }

    public static TextField getBoundField() {
        return boundField;
    }

    /**
     * Binds the top-bar field to {@link #queryProperty()}, replacing any previous binding
     * from an older header instance after scene switches.
     */
    public static void bindTopBarSearchField(TextField field) {
        if (boundField != null && boundField != field) {
            try {
                boundField.textProperty().unbindBidirectional(QUERY);
            } catch (@SuppressWarnings("unused") RuntimeException ignored) {
                // field may already be partially torn down
            }
        }
        boundField = field;
        if (field != null) {
            field.textProperty().bindBidirectional(QUERY);
        }
    }

    public static void clearQuery() {
        QUERY.set("");
    }

    /**
     * Only the visible search page should register; replaces any previous subscriber.
     * Immediately applies the current query so results show even if a debounced update
     * fired while {@link #clearSearchSubscriber} had cleared the listener during navigation.
     */
    public static void setSearchSubscriber(Consumer<String> subscriber) {
        searchSubscriber = subscriber;
        DEBOUNCE.stop();
        if (subscriber != null) {
            String v = QUERY.get();
            subscriber.accept(v != null ? v : "");
        }
    }

    public static void clearSearchSubscriber() {
        searchSubscriber = null;
        DEBOUNCE.stop();
    }
}

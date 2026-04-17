package com.example.tunevaultfx.chrome;

import com.example.tunevaultfx.search.RecentSearchActions;
import com.example.tunevaultfx.search.SearchRecentItem;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.util.CellStyleKit;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.io.IOException;

/**
 * Popup under the top-bar search field: recent searches, with “Clear search” as the last row
 * (scroll down when there are many recents).
 */
public final class SearchRecentDropdown {

    private static final Object CLEAR_SENTINEL = new Object();

    private static final String APP_CSS;

    static {
        var res = SearchRecentDropdown.class.getResource("/com/example/tunevaultfx/app.css");
        APP_CSS = res != null ? res.toExternalForm() : "";
    }

    private final TextField anchor;
    private final Popup popup;
    private final ListView<Object> listView;
    private final ObservableList<Object> rows = FXCollections.observableArrayList();
    private final ListChangeListener<SearchRecentItem> recentsListener;

    public SearchRecentDropdown(TextField anchor) {
        this.anchor = anchor;
        this.popup = new Popup();
        popup.setAutoHide(true);
        popup.setOnShowing(
                e -> {
                    Scene ps = popup.getScene();
                    if (ps == null) {
                        return;
                    }
                    if (!APP_CSS.isEmpty() && !ps.getStylesheets().contains(APP_CSS)) {
                        ps.getStylesheets().add(APP_CSS);
                    }
                    Scene owner = anchor.getScene();
                    if (owner != null
                            && owner.getRoot() != null
                            && ps.getRoot() != null) {
                        boolean light = owner.getRoot().getStyleClass().contains("theme-light");
                        ps.getRoot().getStyleClass().removeAll("theme-light");
                        if (light) {
                            ps.getRoot().getStyleClass().add("theme-light");
                        }
                    }
                });
        this.recentsListener =
                c -> Platform.runLater(
                        () -> {
                            if (popup.isShowing()) {
                                rebuildRows();
                            }
                        });

        listView = new ListView<>(rows);
        listView.getStyleClass().add("search-dropdown-list");
        listView.setPrefWidth(400);
        listView.setMaxWidth(560);
        listView.setMaxHeight(280);
        listView.setPlaceholder(new Label("No recent searches yet"));

        listView.prefWidthProperty().bind(anchor.widthProperty());
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                if (item == CLEAR_SENTINEL) {
                    setText(null);
                    Label clear = new Label("Clear search");
                    clear.getStyleClass().add("search-dropdown-clear-label");
                    HBox row = new HBox(clear);
                    row.getStyleClass().add("search-dropdown-clear-row");
                    setGraphic(row);
                    setStyle("-fx-background-color: transparent;");
                    return;
                }
                if (item instanceof SearchRecentItem recent) {
                    boolean isSong = recent.getType() == SearchRecentItem.Type.SONG;
                    StackPane icon =
                            CellStyleKit.iconBox(
                                    isSong ? "♫" : "◎",
                                    isSong ? CellStyleKit.Palette.PURPLE : CellStyleKit.Palette.ROSE,
                                    !isSong);
                    VBox text =
                            isSong
                                    ? CellStyleKit.songTextBoxWithKind(
                                            recent.getPrimaryText(),
                                            recent.getSecondaryText(),
                                            null,
                                            null)
                                    : CellStyleKit.textBox(
                                            recent.getPrimaryText(), recent.getSecondaryText());
                    HBox.setHgrow(text, Priority.ALWAYS);
                    HBox row = CellStyleKit.row(icon, text);
                    CellStyleKit.addHover(row);
                    setText(null);
                    setGraphic(row);
                    setStyle("-fx-background-color: transparent; -fx-padding: 2 0 2 0;");
                }
            }
        });

        listView.setOnMouseClicked(
                e -> {
                    if (e.getButton() != MouseButton.PRIMARY) {
                        return;
                    }
                    Platform.runLater(
                            () -> {
                                Object raw = listView.getSelectionModel().getSelectedItem();
                                if (raw == CLEAR_SENTINEL) {
                                    SearchBarState.clearQuery();
                                    hide();
                                    return;
                                }
                                if (raw instanceof SearchRecentItem item) {
                                    hide();
                                    try {
                                        RecentSearchActions.open(item, anchor);
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                });

        VBox panel = new VBox(listView);
        panel.getStyleClass().add("search-dropdown-panel");
        popup.getContent().setAll(panel);

        SessionManager.getRecentSearches().addListener(recentsListener);

        anchor.sceneProperty().addListener((obs, o, n) -> {
            if (o != null) {
                o.removeEventFilter(KeyEvent.KEY_TYPED, this::hideOnTypingWhileOpen);
            }
            if (n != null) {
                n.addEventFilter(KeyEvent.KEY_TYPED, this::hideOnTypingWhileOpen);
            }
        });
        if (anchor.getScene() != null) {
            anchor.getScene().addEventFilter(KeyEvent.KEY_TYPED, this::hideOnTypingWhileOpen);
        }
    }

    private void hideOnTypingWhileOpen(KeyEvent e) {
        if (!popup.isShowing()) {
            return;
        }
        String ch = e.getCharacter();
        if (ch != null && !ch.isEmpty() && !Character.isISOControl(ch.charAt(0))) {
            hide();
        }
    }

    private void rebuildRows() {
        rows.clear();
        rows.addAll(SessionManager.getRecentSearches());
        rows.add(CLEAR_SENTINEL);
    }

    public void show() {
        if (anchor.getScene() == null || anchor.getScene().getWindow() == null) {
            return;
        }
        rebuildRows();
        Window window = anchor.getScene().getWindow();
        Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
        if (b == null) {
            return;
        }
        popup.show(window, b.getMinX(), b.getMaxY() + 4);
    }

    public void hide() {
        popup.hide();
    }

    public boolean isShowing() {
        return popup.isShowing();
    }
}

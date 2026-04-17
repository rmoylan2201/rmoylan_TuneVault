package com.example.tunevaultfx.chrome;

import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.util.AppTheme;
import com.example.tunevaultfx.util.ContextMenuPopupSupport;
import com.example.tunevaultfx.util.SceneUtil;
import com.example.tunevaultfx.util.UiPrefs;
import com.example.tunevaultfx.view.FxmlResources;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.io.UncheckedIOException;

/** Header: home, centered search, theme, account menu, log out. */
public class AppTopBarController {

    @FXML private HBox topBarRoot;
    @FXML private HBox topBarBrand;
    @FXML private Button topBarHomeButton;
    @FXML private Button topBarBackButton;
    @FXML private Button topBarForwardButton;
    @FXML private TextField globalSearchField;
    @FXML private Button wrappedQuickBtn;
    @FXML private Button genreQuizQuickBtn;
    @FXML private Button themeToggleBtn;
    @FXML private Button accountMenuBtn;

    private ContextMenu accountMenu;
    private SearchRecentDropdown searchRecentDropdown;
    private ChangeListener<String> autoOpenSearchPageListener;

    @FXML
    public void initialize() {
        syncThemeToggleLabel();
        setupHistoryNavButtons();
        SearchBarState.bindTopBarSearchField(globalSearchField);

        if (topBarHomeButton != null) {
            topBarHomeButton.setTooltip(new Tooltip("Home"));
        }

        searchRecentDropdown = new SearchRecentDropdown(globalSearchField);

        // Avoid stealing focus on scene load (focused ring + recents felt like the bar "popped open").
        globalSearchField.setFocusTraversable(false);

        globalSearchField
                .focusedProperty()
                .addListener(
                        (obs, wasFocused, focused) -> {
                            if (!focused || searchRecentDropdown == null || globalSearchField == null) {
                                return;
                            }
                            String t = globalSearchField.getText();
                            if (t == null || t.isBlank()) {
                                Platform.runLater(() -> searchRecentDropdown.show());
                            }
                        });

        // Recents only when the field is empty — clicking to edit an existing query should not pop the dropdown.
        globalSearchField.addEventHandler(
                MouseEvent.MOUSE_CLICKED,
                e -> {
                    if (e.getButton() != MouseButton.PRIMARY) {
                        return;
                    }
                    String t = globalSearchField.getText();
                    if (t == null || t.isBlank()) {
                        searchRecentDropdown.show();
                    }
                });

        globalSearchField.setOnAction(
                e -> {
                    searchRecentDropdown.hide();
                    try {
                        SceneUtil.switchScene(globalSearchField, FxmlResources.SEARCH);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

        autoOpenSearchPageListener =
                (obs, oldQ, newQ) -> {
                    String raw = newQ != null ? newQ : "";
                    if (raw.isBlank()) {
                        if (searchRecentDropdown != null
                                && globalSearchField != null
                                && globalSearchField.isFocused()) {
                            Platform.runLater(
                                    () -> {
                                        String q = SearchBarState.queryProperty().get();
                                        if (q == null || q.isBlank()) {
                                            if (globalSearchField != null
                                                    && globalSearchField.isFocused()) {
                                                searchRecentDropdown.show();
                                            }
                                        }
                                    });
                        }
                        return;
                    }
                    if (searchRecentDropdown != null) {
                        searchRecentDropdown.hide();
                    }
                    if (FxmlResources.SEARCH.equals(SceneUtil.getCurrentPage())) {
                        return;
                    }
                    Platform.runLater(this::navigateToSearchIfStillNeeded);
                };
        topBarRoot.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                SearchBarState.queryProperty().removeListener(autoOpenSearchPageListener);
            }
            if (newScene != null) {
                SearchBarState.queryProperty().addListener(autoOpenSearchPageListener);
                Platform.runLater(this::syncThemeToggleLabel);
            }
        });
        if (topBarRoot.getScene() != null) {
            SearchBarState.queryProperty().addListener(autoOpenSearchPageListener);
            Platform.runLater(this::syncThemeToggleLabel);
        }

        if (wrappedQuickBtn != null) {
            wrappedQuickBtn.setTooltip(new Tooltip("Listening highlights"));
        }
        if (genreQuizQuickBtn != null) {
            genreQuizQuickBtn.setTooltip(new Tooltip("Genre profile quiz"));
        }

        if (accountMenuBtn != null) {
            accountMenu = new ContextMenu();
            accountMenu.getStyleClass().add("tv-account-dropdown");
            MenuItem profile = new MenuItem("Profile");
            profile.getStyleClass().add("top-bar-account-menu-item");
            profile.setOnAction(
                    e -> {
                        accountMenu.hide();
                        try {
                            SessionManager.clearProfileViewUsername();
                            SceneUtil.switchScene(accountMenuBtn, FxmlResources.PROFILE);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    });
            MenuItem settings = new MenuItem("Settings");
            settings.getStyleClass().add("top-bar-account-menu-item");
            settings.setOnAction(
                    e -> {
                        accountMenu.hide();
                        try {
                            SceneUtil.switchScene(accountMenuBtn, FxmlResources.SETTINGS);
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    });
            accountMenu.getItems().addAll(profile, settings);
            ContextMenuPopupSupport.installThemedPopupHandlers(accountMenu, accountMenuBtn);
        }
    }

    private void setupHistoryNavButtons() {
        if (topBarBackButton != null) {
            topBarBackButton.setTooltip(new Tooltip("Back"));
        }
        if (topBarForwardButton != null) {
            topBarForwardButton.setTooltip(new Tooltip("Forward"));
        }
        SceneUtil.setHistoryRefreshHandler(this::refreshHistoryNavButtons);
        refreshHistoryNavButtons();
    }

    private void refreshHistoryNavButtons() {
        if (topBarBackButton != null) {
            topBarBackButton.setDisable(!SceneUtil.canGoBack());
        }
        if (topBarForwardButton != null) {
            topBarForwardButton.setDisable(!SceneUtil.canGoForward());
        }
    }

    @FXML
    private void handleTopBarBack() {
        if (topBarRoot == null || topBarRoot.getScene() == null) {
            return;
        }
        try {
            SceneUtil.goBack(topBarRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTopBarForward() {
        if (topBarRoot == null || topBarRoot.getScene() == null) {
            return;
        }
        try {
            SceneUtil.goForward(topBarRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBrandClick(MouseEvent e) throws IOException {
        if (searchRecentDropdown != null) {
            searchRecentDropdown.hide();
        }
        SceneUtil.switchScene(topBarBrand, FxmlResources.MAIN_MENU);
        e.consume();
    }

    @FXML
    private void handleHomeClick(ActionEvent e) throws IOException {
        if (searchRecentDropdown != null) {
            searchRecentDropdown.hide();
        }
        SceneUtil.switchScene((Node) e.getSource(), FxmlResources.MAIN_MENU);
    }

    @FXML
    private void handleGoWrapped(ActionEvent e) throws IOException {
        if (searchRecentDropdown != null) {
            searchRecentDropdown.hide();
        }
        SceneUtil.switchScene(wrappedQuickBtn, FxmlResources.WRAPPED);
    }

    @FXML
    private void handleGoGenreQuiz(ActionEvent e) throws IOException {
        if (searchRecentDropdown != null) {
            searchRecentDropdown.hide();
        }
        SceneUtil.switchScene(genreQuizQuickBtn, FxmlResources.FIND_YOUR_GENRE);
    }

    @FXML
    private void handleClearSearchBar(ActionEvent e) {
        SearchBarState.clearQuery();
        if (globalSearchField != null) {
            globalSearchField.requestFocus();
        }
        if (searchRecentDropdown != null && globalSearchField != null) {
            Platform.runLater(
                    () -> {
                        String q = SearchBarState.queryProperty().get();
                        if (q == null || q.isBlank()) {
                            searchRecentDropdown.show();
                        }
                    });
        }
    }

    private void navigateToSearchIfStillNeeded() {
        if (globalSearchField == null || globalSearchField.getScene() == null) {
            return;
        }
        String q = SearchBarState.queryProperty().get();
        if (q == null || q.isBlank()) {
            return;
        }
        if (FxmlResources.SEARCH.equals(SceneUtil.getCurrentPage())) {
            return;
        }
        try {
            SceneUtil.switchScene(globalSearchField, FxmlResources.SEARCH);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleAccountMenu(ActionEvent e) {
        if (accountMenu == null || accountMenuBtn == null) {
            return;
        }
        if (accountMenu.isShowing()) {
            accountMenu.hide();
        } else {
            accountMenu.show(accountMenuBtn, Side.BOTTOM, 0, 0);
        }
    }

    @FXML
    private void toggleTheme(ActionEvent e) {
        boolean next = !UiPrefs.prefs().getBoolean(UiPrefs.KEY_THEME_LIGHT, false);
        UiPrefs.prefs().putBoolean(UiPrefs.KEY_THEME_LIGHT, next);
        Scene scene = themeToggleBtn.getScene();
        SceneUtil.applySavedTheme(scene);
        syncThemeToggleLabel();
        if (scene != null && scene.getRoot() != null) {
            AppTheme.refreshAllListViews(scene.getRoot());
        }
    }

    @FXML
    private void handleLogout(ActionEvent e) throws IOException {
        if (searchRecentDropdown != null) {
            searchRecentDropdown.hide();
        }
        SearchBarState.clearSearchSubscriber();
        SearchBarState.clearQuery();
        SessionManager.logout();
        SceneUtil.clearHistory();
        SceneUtil.switchSceneNoHistory((Node) e.getSource(), FxmlResources.AUTH_LOGIN);
    }

    private void syncThemeToggleLabel() {
        boolean light = UiPrefs.prefs().getBoolean(UiPrefs.KEY_THEME_LIGHT, false);
        themeToggleBtn.setText(light ? "Dark mode" : "Light mode");
    }
}

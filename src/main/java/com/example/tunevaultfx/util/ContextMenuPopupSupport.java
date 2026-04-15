package com.example.tunevaultfx.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Context menus render in a separate
 * {@link Scene}. That scene does not inherit the main window's {@code .root.theme-light}, and the
 * default fill is black — rounded menu skins then show dark corners in light mode unless the fill
 * and stylesheet are synced like the main stage.
 */
public final class ContextMenuPopupSupport {

    /** Popup scenes do not inherit the Stage's stylesheets; they must be attached explicitly. */
    private static final String APP_CSS_URL;

    static {
        var res = ContextMenuPopupSupport.class.getResource("/com/example/tunevaultfx/app.css");
        APP_CSS_URL = res != null ? res.toExternalForm() : "";
    }

    private ContextMenuPopupSupport() {}

    public static void preparePopupScene(ContextMenu menu, Node anchor) {
        Scene popupScene = menu.getScene();
        if (popupScene == null) {
            return;
        }
        if (!APP_CSS_URL.isEmpty() && !popupScene.getStylesheets().contains(APP_CSS_URL)) {
            popupScene.getStylesheets().add(APP_CSS_URL);
        }
        Scene ownerScene = anchor != null ? anchor.getScene() : null;
        if (ownerScene != null) {
            for (String sheet : ownerScene.getStylesheets()) {
                if (sheet != null
                        && !sheet.isBlank()
                        && !popupScene.getStylesheets().contains(sheet)) {
                    popupScene.getStylesheets().add(sheet);
                }
            }
            Parent popRoot = popupScene.getRoot();
            Parent ownRoot = ownerScene.getRoot();
            if (popRoot != null && ownRoot != null) {
                boolean light = ownRoot.getStyleClass().contains("theme-light");
                popRoot.getStyleClass().removeAll("theme-light");
                if (light) {
                    popRoot.getStyleClass().add("theme-light");
                }
            }
        }
    }

    public static void syncPopupSceneFill(ContextMenu menu) {
        Scene popupScene = menu.getScene();
        if (popupScene != null) {
            popupScene.setFill(
                    AppTheme.isLightMode() ? Color.web("#ffffff") : Color.web("#161628"));
        }
    }

    /**
     * Rounded backgrounds still paint a rectangular layout bounds; anti-aliased fringe can show
     * whatever is behind (row tint, scene fill). Clipping the skin root matches the rounded shape.
     */
    public static void clipMenuRootToRoundedCorners(ContextMenu menu) {
        Scene sc = menu.getScene();
        if (sc == null) {
            return;
        }
        Node root = sc.getRoot();
        if (!(root instanceof Region reg)) {
            return;
        }
        double radius = AppTheme.isLightMode() ? 16 : 14;
        double arc = 2 * radius;
        Rectangle clip = new Rectangle();
        clip.setSmooth(true);
        clip.setArcWidth(arc);
        clip.setArcHeight(arc);
        clip.widthProperty().bind(reg.widthProperty());
        clip.heightProperty().bind(reg.heightProperty());
        reg.setClip(clip);
    }

    public static void clearMenuRootClip(ContextMenu menu) {
        Scene sc = menu.getScene();
        if (sc != null && sc.getRoot() instanceof Region reg) {
            var old = reg.getClip();
            reg.setClip(null);
            if (old instanceof Rectangle rect) {
                rect.widthProperty().unbind();
                rect.heightProperty().unbind();
            }
        }
    }

    /** Match {@link com.example.tunevaultfx.app.css} {@code .context-menu.tv-context-light}. */
    public static void syncTvContextLightStyleClass(ContextMenu menu) {
        menu.getStyleClass().remove("tv-context-light");
        if (AppTheme.isLightMode()) {
            menu.getStyleClass().add("tv-context-light");
        }
    }

    public static void polishPopupSurface(ContextMenu menu, Node anchor) {
        preparePopupScene(menu, anchor);
        syncPopupSceneFill(menu);
        clipMenuRootToRoundedCorners(menu);
    }

    /**
     * Attach show/hide handlers so the popup matches app theme (fill, stylesheet, light menu
     * styles, rounded clip).
     */
    public static void installThemedPopupHandlers(ContextMenu menu, Node anchor) {
        menu.setOnShowing(
                e -> {
                    syncTvContextLightStyleClass(menu);
                    preparePopupScene(menu, anchor);
                });
        menu.setOnHidden(e -> clearMenuRootClip(menu));
        menu.setOnShown(
                e -> {
                    Runnable syncPopupSurface =
                            () -> {
                                preparePopupScene(menu, anchor);
                                syncPopupSceneFill(menu);
                                clipMenuRootToRoundedCorners(menu);
                            };
                    syncPopupSurface.run();
                    Platform.runLater(syncPopupSurface);
                    Platform.runLater(() -> Platform.runLater(syncPopupSurface));
                });
    }

}

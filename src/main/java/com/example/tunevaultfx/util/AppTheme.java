package com.example.tunevaultfx.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;

/**
 * Global light/dark flag so list cells and inline-styled controls can resolve
 * readable colours. Kept in sync from {@link com.example.tunevaultfx.chrome.AppTopBarController}
 * (and {@link com.example.tunevaultfx.util.SceneUtil#applySavedTheme}) when appearance changes.
 */
public final class AppTheme {

    private static volatile boolean lightMode;

    private AppTheme() {}

    public static void setLightMode(boolean light) {
        lightMode = light;
    }

    public static boolean isLightMode() {
        return lightMode;
    }

    /** Re-run {@link javafx.scene.control.ListView#refresh()} on every list in the scene. */
    public static void refreshAllListViews(Node root) {
        if (root == null) {
            return;
        }
        if (root instanceof javafx.scene.control.ListView<?> lv) {
            lv.refresh();
        }
        if (root instanceof ScrollPane sp) {
            refreshAllListViews(sp.getContent());
        }
        if (root instanceof Parent p) {
            for (Node c : p.getChildrenUnmodifiable()) {
                refreshAllListViews(c);
            }
        }
    }
}

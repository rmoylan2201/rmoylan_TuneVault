package com.example.tunevaultfx.util;

import com.example.tunevaultfx.core.PlaylistNames;
import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.musicplayer.controller.MusicPlayerController;
import com.example.tunevaultfx.playlist.service.PlaylistPickerService;
import com.example.tunevaultfx.playlist.service.PlaylistService;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.user.UserProfile;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Right-click / secondary-click and overflow (⋯) menus for song rows.
 */
public final class SongContextMenuBuilder {

    private static final MusicPlayerController PLAYER = MusicPlayerController.getInstance();
    private static final PlaylistService PLAYLIST_SERVICE = new PlaylistService();
    private static final PlaylistPickerService PICKER = new PlaylistPickerService();

    /** Popup scenes do not inherit the Stage's stylesheets; they must be attached explicitly. */
    private static final String APP_CSS_URL;

    static {
        var res = SongContextMenuBuilder.class.getResource("/com/example/tunevaultfx/app.css");
        APP_CSS_URL = res != null ? res.toExternalForm() : "";
    }

    private SongContextMenuBuilder() {}

    /**
     * Context menus live in a separate {@link Scene}. Without our stylesheet, Modena alone draws
     * the popup — rounded clips then expose the default black window backing (song right-click /
     * trackpad only).
     */
    private static void preparePopupScene(ContextMenu menu, Node anchor) {
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

    private static void syncPopupSceneFill(ContextMenu menu) {
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
    private static void clipMenuRootToRoundedCorners(ContextMenu menu) {
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

    private static void clearMenuRootClip(ContextMenu menu) {
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

    private static void polishSongActionPopup(ContextMenu menu, Node anchor) {
        preparePopupScene(menu, anchor);
        syncPopupSceneFill(menu);
        clipMenuRootToRoundedCorners(menu);
    }

    public static ContextMenu build(Song song, Node anchor, Spec spec) {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("tv-song-actions-menu");
        if (AppTheme.isLightMode()) {
            menu.getStyleClass().add("tv-context-light");
        }
        menu.setAutoHide(true);
        menu.setOnShowing(e -> preparePopupScene(menu, anchor));
        menu.setOnHidden(e -> clearMenuRootClip(menu));
        menu.setOnShown(
                e -> {
                    preparePopupScene(menu, anchor);
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

        if (spec.includePlayNext) {
            MenuItem playNext = new MenuItem("Play Next");
            playNext.setOnAction(
                    e -> {
                        PLAYER.addToQueueNext(song);
                        menu.hide();
                    });
            menu.getItems().add(playNext);
        }

        if (spec.includeAddToPlaylist) {
            String label =
                    spec.addToPlaylistMenuLabel != null && !spec.addToPlaylistMenuLabel.isBlank()
                            ? spec.addToPlaylistMenuLabel
                            : "Add to playlist";
            MenuItem add = new MenuItem(label);
            add.setOnAction(
                    e -> {
                        Scene sc = anchor != null ? anchor.getScene() : null;
                        if (sc != null) {
                            PICKER.show(song, sc);
                        }
                        menu.hide();
                    });
            menu.getItems().add(add);
        }

        if (spec.includeLikeToggle) {
            UserProfile profile = SessionManager.getCurrentUserProfile();
            boolean liked = profile != null && profile.isLiked(song);
            MenuItem like =
                    new MenuItem(
                            liked
                                    ? "Remove from " + PlaylistNames.LIKED_SONGS
                                    : "Save to " + PlaylistNames.LIKED_SONGS);
            if (liked) {
                like.getStyleClass().add("tv-menu-destructive");
            }
            like.setOnAction(
                    e -> {
                        PLAYLIST_SERVICE.toggleLikeSong(song);
                        menu.hide();
                    });
            menu.getItems().add(like);
        }

        if (spec.removeFromPlaylistLabel != null
                && spec.onRemoveFromPlaylist != null
                && !spec.removeFromPlaylistLabel.isBlank()) {
            MenuItem remove = new MenuItem("Remove from " + spec.removeFromPlaylistLabel);
            remove.getStyleClass().add("tv-menu-destructive");
            remove.setOnAction(
                    e -> {
                        spec.onRemoveFromPlaylist.run();
                        menu.hide();
                    });
            menu.getItems().add(remove);
        }

        return menu;
    }

    /** Configuration for {@link #build(Song, Node, Spec)}. */
    public static final class Spec {
        boolean includePlayNext = true;
        boolean includeAddToPlaylist = true;
        String addToPlaylistMenuLabel;
        boolean includeLikeToggle = true;
        String removeFromPlaylistLabel;
        Runnable onRemoveFromPlaylist;

        public Spec playNext(boolean v) {
            this.includePlayNext = v;
            return this;
        }

        public Spec addToPlaylist(boolean v, String menuLabelOrNull) {
            this.includeAddToPlaylist = v;
            this.addToPlaylistMenuLabel = menuLabelOrNull;
            return this;
        }

        public Spec likeToggle(boolean v) {
            this.includeLikeToggle = v;
            return this;
        }

        public Spec removeFromPlaylist(String displayName, Runnable onRemove) {
            this.removeFromPlaylistLabel = displayName;
            this.onRemoveFromPlaylist = onRemove;
            return this;
        }

        public static Spec forPlaylistRow(String playlistDisplayName, Runnable onRemove) {
            return new Spec()
                    .playNext(true)
                    .addToPlaylist(true, "Add to Another Playlist")
                    .likeToggle(true)
                    .removeFromPlaylist(playlistDisplayName, onRemove);
        }

        public static Spec general() {
            return new Spec().playNext(true).addToPlaylist(true, null).likeToggle(true);
        }
    }
}

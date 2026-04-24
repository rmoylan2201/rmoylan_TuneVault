package com.example.tunevaultfx.playlist;

import com.example.tunevaultfx.core.PlaylistNames;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Rounded playlist “profile” tile: gradient + glyph, stable per playlist name (Spotify-style when
 * no custom image exists). Used in the sidebar and playlist page header.
 */
public final class PlaylistCoverGraphic {

    private PlaylistCoverGraphic() {}

    public static StackPane create(double side, String playlistName) {
        if (playlistName == null) {
            playlistName = "";
        }
        Label glyph = new Label(PlaylistNames.glyphForPlaylist(playlistName));
        glyph.getStyleClass().add("playlist-cover-glyph");
        glyph.setStyle(String.format("-fx-font-size: %.0fpx;", Math.max(11, side * 0.36)));

        StackPane stack = new StackPane(glyph);
        stack.setAlignment(Pos.CENTER);
        stack.setMinSize(side, side);
        stack.setPrefSize(side, side);
        stack.setMaxSize(side, side);
        stack.getStyleClass().add("playlist-cover-art");
        if (PlaylistNames.isLikedSongs(playlistName)) {
            stack.getStyleClass().add("playlist-cover-art-liked");
        } else {
            int idx = Math.floorMod(playlistName.hashCode(), 8);
            stack.getStyleClass().add("playlist-cover-palette-" + idx);
        }
        return stack;
    }

    /** Empty state on the playlist page when nothing is selected. */
    public static StackPane createPlaceholder(double side) {
        Label glyph = new Label("\u266A");
        glyph.getStyleClass().add("playlist-cover-glyph");
        glyph.setStyle(String.format("-fx-font-size: %.0fpx;", Math.max(11, side * 0.36)));
        StackPane stack = new StackPane(glyph);
        stack.setAlignment(Pos.CENTER);
        stack.setMinSize(side, side);
        stack.setPrefSize(side, side);
        stack.setMaxSize(side, side);
        stack.getStyleClass().addAll("playlist-cover-art", "playlist-cover-placeholder");
        return stack;
    }
}

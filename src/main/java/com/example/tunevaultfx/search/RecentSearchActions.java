package com.example.tunevaultfx.search;

import com.example.tunevaultfx.musicplayer.controller.MusicPlayerController;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.util.SceneUtil;
import com.example.tunevaultfx.view.FxmlResources;
import javafx.scene.Node;

import java.io.IOException;

/** Opens a recent song (play) or artist (profile) — shared by search page and top-bar dropdown. */
public final class RecentSearchActions {

    private RecentSearchActions() {}

    public static void open(SearchRecentItem item, Node navigationSource) throws IOException {
        if (item == null) {
            return;
        }
        if (item.getType() == SearchRecentItem.Type.SONG && item.getSong() != null) {
            var song = item.getSong();
            MusicPlayerController.getInstance().playSingleSong(song);
            SessionManager.addRecentSearch(SearchRecentItem.song(song));
            SessionManager.setSelectedSong(song);
            SceneUtil.switchScene(navigationSource, FxmlResources.SONG_PROFILE);
            return;
        }
        if (item.getType() == SearchRecentItem.Type.ARTIST && item.getArtistName() != null) {
            SessionManager.setSelectedArtist(item.getArtistName());
            SessionManager.addRecentSearch(SearchRecentItem.artist(item.getArtistName()));
            SceneUtil.switchScene(navigationSource, FxmlResources.ARTIST_PROFILE);
        }
    }
}

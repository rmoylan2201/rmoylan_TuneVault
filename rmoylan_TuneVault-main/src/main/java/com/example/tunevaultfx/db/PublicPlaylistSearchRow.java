package com.example.tunevaultfx.db;

/**
 * One row in search results: a user playlist marked {@code is_public}.
 */
public record PublicPlaylistSearchRow(String ownerUsername, String playlistName, int trackCount) {

    public String displayLine() {
        return playlistName + "  \u00B7  @" + ownerUsername;
    }
}

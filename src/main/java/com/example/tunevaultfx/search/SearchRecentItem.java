package com.example.tunevaultfx.search;

import com.example.tunevaultfx.core.Song;

public class SearchRecentItem {

    public enum Type {
        SONG,
        ARTIST
    }

    private final Type type;
    private final Song song;
    private final String artistName;

    private SearchRecentItem(Type type, Song song, String artistName) {
        this.type = type;
        this.song = song;
        this.artistName = artistName;
    }

    public static SearchRecentItem song(Song song) {
        return new SearchRecentItem(Type.SONG, song, null);
    }

    public static SearchRecentItem artist(String artistName) {
        return new SearchRecentItem(Type.ARTIST, null, artistName);
    }

    public Type getType() {
        return type;
    }

    public Song getSong() {
        return song;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getPrimaryText() {
        if (type == Type.SONG && song != null) {
            return song.title();
        }
        return artistName == null ? "" : artistName;
    }

    public String getSecondaryText() {
        if (type == Type.SONG && song != null) {
            return "Song • " + song.artist();
        }
        return "Artist";
    }

    public boolean sameAs(SearchRecentItem other) {
        if (other == null || type != other.type) {
            return false;
        }

        if (type == Type.SONG && song != null && other.song != null) {
            return song.songId() == other.song.songId();
        }

        if (type == Type.ARTIST) {
            return artistName != null && artistName.equalsIgnoreCase(other.artistName);
        }

        return false;
    }
}
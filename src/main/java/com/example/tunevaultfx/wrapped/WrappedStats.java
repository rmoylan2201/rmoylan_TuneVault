package com.example.tunevaultfx.wrapped;

public class WrappedStats {

    private final String topSong;
    private final int topSongSeconds;
    private final String topSongArtist;

    private final String topArtist;
    private final int topArtistSeconds;
    private final String topArtistTopSong;

    private final String favoriteGenre;
    private final int favoriteGenreSeconds;

    private final int totalListeningSeconds;

    private final String summary;

    public WrappedStats(String topSong,
                        int topSongSeconds,
                        String topSongArtist,
                        String topArtist,
                        int topArtistSeconds,
                        String topArtistTopSong,
                        String favoriteGenre,
                        int favoriteGenreSeconds,
                        int totalListeningSeconds,
                        String summary) {
        this.topSong = topSong;
        this.topSongSeconds = topSongSeconds;
        this.topSongArtist = topSongArtist;
        this.topArtist = topArtist;
        this.topArtistSeconds = topArtistSeconds;
        this.topArtistTopSong = topArtistTopSong;
        this.favoriteGenre = favoriteGenre;
        this.favoriteGenreSeconds = favoriteGenreSeconds;
        this.totalListeningSeconds = totalListeningSeconds;
        this.summary = summary;
    }

    public String getTopSong() {
        return topSong;
    }

    public int getTopSongSeconds() {
        return topSongSeconds;
    }

    public String getTopSongArtist() {
        return topSongArtist;
    }

    public String getTopArtist() {
        return topArtist;
    }

    public int getTopArtistSeconds() {
        return topArtistSeconds;
    }

    public String getTopArtistTopSong() {
        return topArtistTopSong;
    }

    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    public int getFavoriteGenreSeconds() {
        return favoriteGenreSeconds;
    }

    public int getTotalListeningSeconds() {
        return totalListeningSeconds;
    }

    public String getSummary() {
        return summary;
    }

    public static WrappedStats empty() {
        return new WrappedStats(
                "No listening data yet",
                0,
                "No listening data yet",
                "No listening data yet",
                0,
                "No listening data yet",
                "No listening data yet",
                0,
                0,
                "No listening data yet. Play some songs to build your Wrapped."
        );
    }
}
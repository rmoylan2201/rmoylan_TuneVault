package com.example.tunevaultfx.recommendation;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.db.ListeningEventDAO;
import com.example.tunevaultfx.db.SongDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {

    private final SongDAO songDAO = new SongDAO();
    private final ListeningEventDAO listeningEventDAO = new ListeningEventDAO();

    public ObservableList<Song> getSuggestedSongsForUser(String username, int limit) {
        try {
            ObservableList<Song> allSongs = songDAO.getAllSongs();
            List<ListeningEventDAO.UserBehaviorEvent> events = listeningEventDAO.getUserBehaviorEvents(username);

            if (events.isEmpty()) {
                return fallbackSongs(allSongs, limit);
            }

            RecommendationProfile profile = buildRecommendationProfile(events);

            List<ScoredSong> scoredSongs = new ArrayList<>();
            for (Song song : allSongs) {
                if (song == null) {
                    continue;
                }

                if (profile.strongNegativeSongIds.contains(song.songId())) {
                    continue;
                }

                double songScore = profile.songAffinity.getOrDefault(song.songId(), 0.0);
                double artistScore = profile.artistAffinity.getOrDefault(normalize(song.artist()), 0.0);
                double genreScore = profile.genreAffinity.getOrDefault(normalize(song.genre()), 0.0);

                double noveltyPenalty = songScore > 3.0 ? 2.5 : 0.0;

                double finalScore = (artistScore * 2.2) + (genreScore * 1.8) + (songScore * 0.6) - noveltyPenalty;

                if (finalScore > 0.0) {
                    scoredSongs.add(new ScoredSong(song, finalScore));
                }
            }

            scoredSongs.sort(Comparator.comparingDouble(ScoredSong::score).reversed());

            return scoredSongs.stream()
                    .limit(limit)
                    .map(ScoredSong::song)
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<Song> getSuggestedSongsForPlaylist(String username,
                                                             ObservableList<Song> playlistSongs,
                                                             int limit) {
        try {
            ObservableList<Song> allSongs = songDAO.getAllSongs();
            List<ListeningEventDAO.UserBehaviorEvent> events = listeningEventDAO.getUserBehaviorEvents(username);

            RecommendationProfile userProfile = buildRecommendationProfile(events);

            Map<String, Double> playlistArtistWeights = new HashMap<>();
            Map<String, Double> playlistGenreWeights = new HashMap<>();
            Set<Integer> existingSongIds = new HashSet<>();

            if (playlistSongs != null) {
                for (Song song : playlistSongs) {
                    if (song == null) {
                        continue;
                    }

                    existingSongIds.add(song.songId());
                    playlistArtistWeights.merge(normalize(song.artist()), 1.0, Double::sum);
                    playlistGenreWeights.merge(normalize(song.genre()), 1.0, Double::sum);
                }
            }

            normalizeMap(playlistArtistWeights);
            normalizeMap(playlistGenreWeights);

            List<ScoredSong> scoredSongs = new ArrayList<>();
            for (Song song : allSongs) {
                if (song == null || existingSongIds.contains(song.songId())) {
                    continue;
                }

                if (userProfile.strongNegativeSongIds.contains(song.songId())) {
                    continue;
                }

                double userSongScore = userProfile.songAffinity.getOrDefault(song.songId(), 0.0);
                double userArtistScore = userProfile.artistAffinity.getOrDefault(normalize(song.artist()), 0.0);
                double userGenreScore = userProfile.genreAffinity.getOrDefault(normalize(song.genre()), 0.0);

                double playlistArtistScore = playlistArtistWeights.getOrDefault(normalize(song.artist()), 0.0);
                double playlistGenreScore = playlistGenreWeights.getOrDefault(normalize(song.genre()), 0.0);

                double finalScore =
                        (userSongScore * 0.5) +
                                (userArtistScore * 1.6) +
                                (userGenreScore * 1.3) +
                                (playlistArtistScore * 2.4) +
                                (playlistGenreScore * 2.0);

                if (finalScore > 0.0) {
                    scoredSongs.add(new ScoredSong(song, finalScore));
                }
            }

            scoredSongs.sort(Comparator.comparingDouble(ScoredSong::score).reversed());

            return scoredSongs.stream()
                    .limit(limit)
                    .map(ScoredSong::song)
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

        } catch (SQLException e) {
            e.printStackTrace();
            return FXCollections.observableArrayList();
        }
    }

    private RecommendationProfile buildRecommendationProfile(List<ListeningEventDAO.UserBehaviorEvent> events) {
        Map<Integer, Double> songAffinity = new HashMap<>();
        Map<String, Double> artistAffinity = new HashMap<>();
        Map<String, Double> genreAffinity = new HashMap<>();
        Set<Integer> strongNegativeSongIds = new HashSet<>();

        for (ListeningEventDAO.UserBehaviorEvent event : events) {
            double weight = weightFor(event.actionType(), event.completionRatio());

            songAffinity.merge(event.songId(), weight, Double::sum);
            artistAffinity.merge(normalize(event.artistName()), weight, Double::sum);
            genreAffinity.merge(normalize(event.genreName()), weight, Double::sum);

            if (weight <= -4.0) {
                strongNegativeSongIds.add(event.songId());
            }
        }

        normalizeMap(artistAffinity);
        normalizeMap(genreAffinity);

        return new RecommendationProfile(songAffinity, artistAffinity, genreAffinity, strongNegativeSongIds);
    }

    private double weightFor(String actionType, double completionRatio) {
        if (actionType == null) {
            return 0.0;
        }

        return switch (actionType) {
            case "LIKE" -> 5.0;
            case "PLAYLIST_ADD" -> 4.0;
            case "PLAY" -> 3.0 + completionRatio;
            case "PLAY_PARTIAL_POSITIVE" -> 2.0 + (completionRatio * 0.5);
            case "PLAY_PARTIAL" -> 1.0;
            case "PLAY_SHORT" -> -1.0;
            case "STOPPED_MID" -> -1.5;
            case "SKIP" -> -2.0;
            case "SKIP_EARLY" -> -4.5;
            case "PLAYLIST_REMOVE" -> -3.0;
            case "UNLIKE" -> -5.0;
            default -> 0.0;
        };
    }

    private ObservableList<Song> fallbackSongs(ObservableList<Song> allSongs, int limit) {
        return allSongs.stream()
                .limit(limit)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private void normalizeMap(Map<String, Double> map) {
        double max = map.values().stream().mapToDouble(Math::abs).max().orElse(0.0);
        if (max <= 0.0) {
            return;
        }

        for (Map.Entry<String, Double> entry : map.entrySet()) {
            entry.setValue(entry.getValue() / max);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private record ScoredSong(Song song, double score) {
    }

    private record RecommendationProfile(
            Map<Integer, Double> songAffinity,
            Map<String, Double> artistAffinity,
            Map<String, Double> genreAffinity,
            Set<Integer> strongNegativeSongIds
    ) {
    }
}
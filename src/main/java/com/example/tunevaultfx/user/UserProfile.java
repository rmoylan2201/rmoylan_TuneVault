package com.example.tunevaultfx.user;

import com.example.tunevaultfx.core.DemoLibrary;
import com.example.tunevaultfx.core.Song;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * Represents one user's music profile.
 * Stores playlists, liked songs, and other user-specific music data.
 */
public class UserProfile {

    private final String username;
    private final Map<String, ObservableList<Song>> playlists = new LinkedHashMap<>();

    public UserProfile(String username) {
        this(username, true);
    }

    private UserProfile(String username, boolean seedDefaults) {
        this.username = username;

        if (seedDefaults) {
            playlists.put("Liked Songs", FXCollections.observableArrayList());

            ObservableList<Song> chill = FXCollections.observableArrayList();
            chill.addAll(DemoLibrary.getSongs().get(0), DemoLibrary.getSongs().get(4));
            playlists.put("Chill Mix", chill);

            ObservableList<Song> workout = FXCollections.observableArrayList();
            workout.addAll(DemoLibrary.getSongs().get(1), DemoLibrary.getSongs().get(3), DemoLibrary.getSongs().get(5));
            playlists.put("Workout", workout);
        }
    }

    public String getUsername() {
        return username;
    }

    public Map<String, ObservableList<Song>> getPlaylists() {
        return playlists;
    }

    public ObservableList<Song> getLikedSongs() {
        return playlists.computeIfAbsent("Liked Songs", k -> FXCollections.observableArrayList());
    }

    public boolean isLiked(Song song) {
        return getLikedSongs().contains(song);
    }

    public void toggleLike(Song song) {
        ObservableList<Song> liked = getLikedSongs();
        if (liked.contains(song)) liked.remove(song);
        else liked.add(song);
    }

    public UserProfileData toData() {
        UserProfileData data = new UserProfileData();
        data.username = username;
        for (Map.Entry<String, ObservableList<Song>> entry : playlists.entrySet()) {
            data.playlists.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return data;
    }

    public static UserProfile fromData(UserProfileData data) {
        UserProfile profile = new UserProfile(data.username, false);

        if (data.playlists != null) {
            for (Map.Entry<String, List<Song>> entry : data.playlists.entrySet()) {
                profile.playlists.put(entry.getKey(), FXCollections.observableArrayList(entry.getValue()));
            }
        }

        if (!profile.playlists.containsKey("Liked Songs")) {
            profile.playlists.put("Liked Songs", FXCollections.observableArrayList());
        }

        return profile;
    }
}
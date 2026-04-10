package com.example.tunevaultfx.user;

import com.example.tunevaultfx.core.Song;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**
 * Simple data version of a user profile.
 * Used when saving and loading user profile information.
 */
public class UserProfileData {
    public String username;
    public Map<String, List<Song>> playlists = new LinkedHashMap<>();
}
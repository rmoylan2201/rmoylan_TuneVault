package com.example.tunevaultfx;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserProfileData {
    public String username;
    public Map<String, List<Song>> playlists = new LinkedHashMap<>();
}
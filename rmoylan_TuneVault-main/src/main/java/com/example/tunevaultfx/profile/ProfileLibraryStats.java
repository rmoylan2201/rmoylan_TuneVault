package com.example.tunevaultfx.profile;

import com.example.tunevaultfx.user.UserProfile;

import java.util.HashSet;
import java.util.Set;

final class ProfileLibraryStats {

    private ProfileLibraryStats() {}

    static int countUniqueSavedSongs(UserProfile profile) {
        if (profile == null || profile.getPlaylists() == null) {
            return 0;
        }
        Set<Integer> seen = new HashSet<>();
        for (var list : profile.getPlaylists().values()) {
            if (list == null) {
                continue;
            }
            for (var song : list) {
                if (song != null) {
                    seen.add(song.songId());
                }
            }
        }
        return seen.size();
    }
}

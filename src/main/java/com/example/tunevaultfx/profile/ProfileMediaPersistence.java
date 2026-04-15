package com.example.tunevaultfx.profile;

import com.example.tunevaultfx.db.UserDAO;
import com.example.tunevaultfx.profile.media.ProfileMediaStorage;
import com.example.tunevaultfx.user.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

/** Persists avatar file imports and coordinates DB keys with on-disk storage. */
final class ProfileMediaPersistence {

    private final UserDAO userDAO;

    ProfileMediaPersistence(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    void saveAvatarFromCroppedTemp(User user, Path tempPng) throws IOException, SQLException {
        try {
            String key = ProfileMediaStorage.importAvatar(user.getUserId(), tempPng);
            replaceAvatarKey(user.getUserId(), user.getProfileAvatarKey(), key);
        } finally {
            Files.deleteIfExists(tempPng);
        }
    }

    void clearAvatar(User user) throws IOException, SQLException {
        String old = user.getProfileAvatarKey();
        userDAO.updateProfileAvatarKey(user.getUserId(), null);
        if (old != null) {
            ProfileMediaStorage.deleteStoredFile(old);
        }
    }

    private void replaceAvatarKey(int userId, String oldKey, String newKey) throws IOException, SQLException {
        userDAO.updateProfileAvatarKey(userId, newKey);
        if (oldKey != null && !oldKey.equals(newKey)) {
            ProfileMediaStorage.deleteStoredFile(oldKey);
        }
    }
}

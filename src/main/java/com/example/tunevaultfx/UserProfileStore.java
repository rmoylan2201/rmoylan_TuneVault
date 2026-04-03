package com.example.tunevaultfx;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserProfileStore {

    private static final Path PROFILE_DIR = Paths.get("profiles");
    private static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public static UserProfile loadProfile(String username) {
        try {
            Files.createDirectories(PROFILE_DIR);
            Path file = PROFILE_DIR.resolve(username + ".json");

            if (!Files.exists(file)) {
                return new UserProfile(username);
            }

            UserProfileData data = mapper.readValue(file.toFile(), UserProfileData.class);
            return UserProfile.fromData(data);
        } catch (IOException e) {
            e.printStackTrace();
            return new UserProfile(username);
        }
    }

    public static void saveProfile(UserProfile profile) {
        try {
            Files.createDirectories(PROFILE_DIR);
            Path file = PROFILE_DIR.resolve(profile.getUsername() + ".json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), profile.toData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
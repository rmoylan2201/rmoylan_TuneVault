package com.example.tunevaultfx;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserStore {
    private static final String FILE_NAME = "/Users/berk/Documents/JAVA 2024/TuneVaultFX/src/main/java/com/example/tunevaultfx/users.txt";

    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(FILE_NAME);

        if (!file.exists()) return users;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                User user = User.fromFileString(line);
                if (user != null) users.add(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return users;
    }

    public static void saveUser(User user) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(user.toFileString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean usernameExists(String username) {
        return loadUsers().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    public static User validateLogin(String usernameOrEmail, String password) {
        return loadUsers().stream()
                .filter(u ->
                        (u.getUsername().equalsIgnoreCase(usernameOrEmail)
                                || u.getEmail().equalsIgnoreCase(usernameOrEmail))
                                && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }
}
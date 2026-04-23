package com.example.tunevaultfx.profile.media;

import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Stores profile avatar images on disk (per OS user), with DB holding relative keys.
 * Root: {@code ~/.tunevaultfx/profile-media}.
 */
public final class ProfileMediaStorage {

    private static final long MAX_BYTES = 8L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT =
            Set.of("png", "jpg", "jpeg", "gif", "bmp", "webp");

    private ProfileMediaStorage() {}

    public static Path rootDirectory() {
        return Path.of(System.getProperty("user.home"), ".tunevaultfx", "profile-media");
    }

    public static Path resolveFile(String relativeKey) {
        if (relativeKey == null || relativeKey.isBlank()) {
            return null;
        }
        String normalized = relativeKey.replace('\\', '/').trim();
        if (normalized.contains("..") || normalized.startsWith("/")) {
            return null;
        }
        Path p = rootDirectory().resolve(normalized).normalize();
        if (!p.startsWith(rootDirectory().normalize())) {
            return null;
        }
        return p;
    }

    /**
     * @return relative key e.g. {@code 12/avatar.jpg}
     */
    public static String importAvatar(int userId, Path sourceFile) throws IOException {
        if (userId <= 0) {
            throw new IOException("Invalid user");
        }
        if (sourceFile == null || !Files.isRegularFile(sourceFile)) {
            throw new IOException("Not a file");
        }
        long size = Files.size(sourceFile);
        if (size > MAX_BYTES) {
            throw new IOException("Image must be " + (MAX_BYTES / 1024 / 1024) + " MB or smaller.");
        }
        String ext = extensionOf(sourceFile);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IOException("Use PNG, JPEG, GIF, BMP, or WebP.");
        }
        validateDecodesAsImage(sourceFile);

        String baseName = "avatar";
        Path userDir = rootDirectory().resolve(String.valueOf(userId));
        Files.createDirectories(userDir);

        deleteFilesStartingWith(userDir, baseName);

        String relative = userId + "/" + baseName + "." + ext;
        Path dest = userDir.resolve(baseName + "." + ext);
        Files.copy(sourceFile, dest, StandardCopyOption.REPLACE_EXISTING);
        return relative;
    }

    public static void deleteStoredFile(String relativeKey) throws IOException {
        Path p = resolveFile(relativeKey);
        if (p != null && Files.isRegularFile(p)) {
            Files.deleteIfExists(p);
        }
    }

    /** Best-effort removal of all on-disk avatar files for {@code userId} (e.g. after account deletion). */
    public static void deleteUserMediaDir(int userId) {
        if (userId <= 0) {
            return;
        }
        Path root = rootDirectory().normalize();
        Path dir = root.resolve(String.valueOf(userId)).normalize();
        if (!dir.startsWith(root) || !Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(
                            p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException ignored) {
                                }
                            });
        } catch (IOException ignored) {
        }
    }

    private static void deleteFilesStartingWith(Path userDir, String baseName) throws IOException {
        if (!Files.isDirectory(userDir)) {
            return;
        }
        try (var stream = Files.list(userDir)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(
                            path -> {
                                String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                                return name.startsWith(baseName + ".");
                            })
                    .forEach(
                            path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException ignored) {
                                }
                            });
        }
    }

    private static void validateDecodesAsImage(Path file) throws IOException {
        try {
            Image img = new Image(file.toUri().toString(), false);
            if (img.isError()) {
                Throwable ex = img.getException();
                throw new IOException(ex != null ? ex.getMessage() : "Could not read image.");
            }
            double w = img.getWidth();
            double h = img.getHeight();
            if (w < 1 || h < 1) {
                throw new IOException("Could not read image dimensions.");
            }
            if (w < 16 || h < 16) {
                throw new IOException("Image is too small (try at least 16×16).");
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e.getMessage() != null ? e.getMessage() : "Invalid image.");
        }
    }

    private static String extensionOf(Path file) {
        String name = file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}

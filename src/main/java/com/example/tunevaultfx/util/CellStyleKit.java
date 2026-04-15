package com.example.tunevaultfx.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.function.Consumer;

/**
 * Shared factory for list cell components.
 *
 * Every list cell in the application — playlist songs, search results,
 * artist results, suggested songs, recent searches — uses these builders.
 * This guarantees visual consistency and means a color change requires
 * editing ONE file instead of every controller and cell class.
 *
 * Design system
 * ─────────────
 * TEXT_PRIMARY   #eeeef6  — headings, song titles, anything important
 * TEXT_SECONDARY #9d9db8  — artist, genre, meta (CLEARLY readable on dark)
 * TEXT_MUTED     #5c5c78  — timestamps, very minor info (use sparingly)
 *
 * BG_CARD        #0f0f1c  — cell background
 * BG_HOVER       rgba(139,92,246,0.06) — very subtle violet tint on hover
 * BG_SELECTED    rgba(139,92,246,0.12) — selected state
 *
 * PURPLE / ROSE / GREEN / AMBER — icon and tag colour families
 */
public final class CellStyleKit {

    private CellStyleKit() {}

    // ── Text colours (dark baseline; use getters for theme-aware UI) ─

    public static final String TEXT_PRIMARY   = "#eeeef6";
    public static final String TEXT_SECONDARY = "#9d9db8";
    public static final String TEXT_MUTED     = "#5c5c78";

    public static String getTextPrimary() {
        return AppTheme.isLightMode() ? "#0f172a" : TEXT_PRIMARY;
    }

    public static String getTextSecondary() {
        return AppTheme.isLightMode() ? "#475569" : TEXT_SECONDARY;
    }

    public static String getTextMuted() {
        return AppTheme.isLightMode() ? "#64748b" : TEXT_MUTED;
    }

    public static String getAccentTitle() {
        return AppTheme.isLightMode() ? "#5b21b6" : "#c4b5fd";
    }

    /** Row chrome: readable hover/playing on light backgrounds */
    public static String getRowDefault() {
        return "-fx-background-color: transparent; -fx-background-radius: 14;";
    }

    public static String getRowHover() {
        return AppTheme.isLightMode()
                ? "-fx-background-color: rgba(124,58,237,0.10); -fx-background-radius: 14;"
                : "-fx-background-color: rgba(139,92,246,0.06); -fx-background-radius: 14;";
    }

    public static String getRowPlaying() {
        return AppTheme.isLightMode()
                ? "-fx-background-color: rgba(124,58,237,0.16); -fx-background-radius: 14;"
                : "-fx-background-color: rgba(139,92,246,0.12); -fx-background-radius: 14;";
    }

    // ── Row backgrounds (dark defaults; prefer getRow* in new code) ─

    public static final String ROW_DEFAULT  =
            "-fx-background-color: transparent; -fx-background-radius: 14;";
    public static final String ROW_HOVER    =
            "-fx-background-color: rgba(139,92,246,0.06); -fx-background-radius: 14;";
    public static final String ROW_PLAYING  =
            "-fx-background-color: rgba(139,92,246,0.12); -fx-background-radius: 14;";

    // ── Colour families ───────────────────────────────────────────

    public enum Palette {
        PURPLE("rgba(139,92,246,0.14)", "rgba(139,92,246,0.24)", "#c4b5fd"),
        ROSE  ("rgba(244,63,94,0.12)",  "rgba(244,63,94,0.20)",  "#fda4af"),
        GREEN ("rgba(34,197,94,0.12)",  "rgba(34,197,94,0.20)",  "#86efac"),
        AMBER ("rgba(245,158,11,0.12)", "rgba(245,158,11,0.20)", "#fcd34d");

        public final String bg;
        public final String border;
        public final String text;

        Palette(String bg, String border, String text) {
            this.bg = bg; this.border = border; this.text = text;
        }
    }

    // ── Node builders ──────────────────────────────────────────────

    /**
     * Builds a square icon box (40×40) with centred symbol.
     * @param symbol  e.g. "♫", "◎", "♥"
     * @param shape   true = circle, false = rounded square
     */
    public static StackPane iconBox(String symbol, Palette p, boolean circle) {
        String radius = circle ? "21" : "11";

        StackPane box = new StackPane();
        box.setPrefSize(42, 42);
        box.setMinSize(42, 42);
        box.setMaxSize(42, 42);
        box.setStyle(
                "-fx-background-color: " + p.bg + ";" +
                        "-fx-background-radius: " + radius + ";" +
                        "-fx-border-color: " + p.border + ";" +
                        "-fx-border-radius: " + radius + ";" +
                        "-fx-border-width: 1;");

        Label lbl = new Label(symbol);
        lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: " + paletteIconText(p) + ";");
        box.getChildren().add(lbl);
        StackPane.setAlignment(lbl, Pos.CENTER);
        return box;
    }

    /**
     * Numbered icon box for ordered lists (artist discography, etc.)
     */
    public static Label trackNumber(int number) {
        Label lbl = new Label(String.format("%02d", number));
        lbl.setMinWidth(28);
        lbl.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + getTextMuted() + ";");
        return lbl;
    }

    /**
     * Primary label — song title, artist name.
     */
    public static Label primary(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                        "-fx-text-fill: " + getTextPrimary() + ";");
        return lbl;
    }

    /**
     * Secondary label — artist, genre, subtitle.
     */
    public static Label secondary(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: " + getTextSecondary() + ";");
        return lbl;
    }

    /**
     * Muted label — durations, timestamps, truly minor info.
     */
    public static Label muted(String text) {
        Label lbl = new Label(text);
        lbl.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: " + getTextMuted() + ";");
        return lbl;
    }

    /**
     * Duration label formatted as M:SS.
     */
    public static Label duration(int totalSeconds) {
        String text = totalSeconds > 0
                ? (totalSeconds / 60) + ":" + String.format("%02d", totalSeconds % 60)
                : "";
        return muted(text);
    }

    /**
     * Pill tag for type indicators ("Song", "Artist", genre names).
     */
    public static Label tag(String text, Palette p) {
        Label lbl = new Label(text);
        lbl.setStyle(tagStyle(p));
        return lbl;
    }

    /**
     * Builds a standard song info VBox (title on top, meta below).
     */
    public static VBox textBox(String title, String meta) {
        VBox box = new VBox(3, primary(title));
        if (meta != null && !meta.isBlank()) {
            box.getChildren().add(secondary(meta));
        }
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    /**
     * Song title plus meta row where the artist is a hyperlink when {@code onOpenArtist} is non-null.
     */
    public static VBox songTextBox(String title, String artist, String genre, Consumer<String> onOpenArtist) {
        VBox box = new VBox(3, primary(title));
        HBox meta = songMetaLine(artist, genre, onOpenArtist);
        if (!meta.getChildren().isEmpty()) {
            box.getChildren().add(meta);
        }
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    /**
     * Second line for song rows: artist (link or plain) and optional genre, separated by " · ".
     */
    public static HBox songMetaLine(String artist, String genre, Consumer<String> onOpenArtist) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);

        boolean hasArtist = artist != null && !artist.isBlank();
        boolean hasGenre  = genre != null && !genre.isBlank();
        if (!hasArtist && !hasGenre) {
            return row;
        }

        if (hasArtist) {
            String a = artist.trim();
            if (onOpenArtist != null) {
                Hyperlink link = new Hyperlink(a);
                link.setVisited(false);
                link.setFocusTraversable(false);
                link.setPadding(Insets.EMPTY);
                link.setUnderline(false);
                link.setStyle(
                        "-fx-font-size: 12px; -fx-text-fill: " + getTextSecondary() + ";"
                                + "-fx-border-width: 0; -fx-underline: false;"
                                + "-fx-background-color: transparent;");
                link.setOnAction(e -> {
                    onOpenArtist.accept(a);
                    e.consume();
                });
                row.getChildren().add(link);
            } else {
                row.getChildren().add(secondary(a));
            }
        }

        if (hasGenre) {
            if (!row.getChildren().isEmpty()) {
                Label dot = new Label(" \u00B7 ");
                dot.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getTextSecondary() + ";");
                row.getChildren().add(dot);
            }
            row.getChildren().add(secondary(genre.trim()));
        }

        return row;
    }

    /**
     * Builds the standard cell HBox row with padding and hover.
     */
    public static HBox row(javafx.scene.Node... children) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 14, 9, 14));
        row.setStyle(getRowDefault());
        row.getChildren().addAll(children);
        return row;
    }

    /**
     * Attaches standard hover styling to a row.
     * Call this after building the row with row().
     */
    public static void addHover(HBox row) {
        row.setOnMouseEntered(e -> row.setStyle(getRowHover()));
        row.setOnMouseExited(e  -> row.setStyle(getRowDefault()));
    }

    /**
     * Marks a row as currently playing (stronger highlight).
     */
    public static void markPlaying(HBox row, boolean playing) {
        if (playing) {
            row.setStyle(getRowPlaying());
            row.setOnMouseEntered(e -> row.setStyle(getRowPlaying()));
            row.setOnMouseExited(e  -> row.setStyle(getRowPlaying()));
        } else {
            addHover(row);
        }
    }

    // ── Common meta builders ───────────────────────────────────────

    private static String paletteIconText(Palette p) {
        if (!AppTheme.isLightMode()) {
            return p.text;
        }
        return switch (p) {
            case PURPLE -> "#5b21b6";
            case ROSE -> "#be123c";
            case GREEN -> "#15803d";
            case AMBER -> "#b45309";
        };
    }

    private static String tagStyle(Palette p) {
        if (!AppTheme.isLightMode()) {
            return "-fx-background-color: " + p.bg + ";"
                    + "-fx-text-fill: " + p.text + ";"
                    + "-fx-font-size: 11px; -fx-font-weight: bold;"
                    + "-fx-background-radius: 10;"
                    + "-fx-border-color: " + p.border + ";"
                    + "-fx-border-radius: 10; -fx-border-width: 1;"
                    + "-fx-padding: 3 10 3 10;";
        }
        return switch (p) {
            case PURPLE -> tagStyleLight(
                    "rgba(124,58,237,0.14)", "rgba(124,58,237,0.35)", "#5b21b6");
            case ROSE -> tagStyleLight(
                    "rgba(244,63,94,0.12)", "rgba(225,29,72,0.35)", "#be123c");
            case GREEN -> tagStyleLight(
                    "rgba(34,197,94,0.14)", "rgba(22,163,74,0.35)", "#15803d");
            case AMBER -> tagStyleLight(
                    "rgba(245,158,11,0.16)", "rgba(217,119,6,0.4)", "#b45309");
        };
    }

    private static String tagStyleLight(String bg, String border, String text) {
        return "-fx-background-color: " + bg + ";"
                + "-fx-text-fill: " + text + ";"
                + "-fx-font-size: 11px; -fx-font-weight: bold;"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-radius: 10; -fx-border-width: 1;"
                + "-fx-padding: 3 10 3 10;";
    }

    /**
     * Builds "Artist · Genre" meta string, gracefully handling nulls/blanks.
     */
    public static String songMeta(String artist, String genre) {
        StringBuilder sb = new StringBuilder();
        if (artist != null && !artist.isBlank()) sb.append(artist);
        if (genre  != null && !genre.isBlank()) {
            if (!sb.isEmpty()) sb.append(" \u00B7 ");
            sb.append(genre);
        }
        return sb.toString();
    }

    /**
     * "Album · Genre" for discography views.
     */
    public static String albumMeta(String album, String genre) {
        StringBuilder sb = new StringBuilder();
        if (album != null && !album.isBlank()) sb.append(album);
        if (genre != null && !genre.isBlank()) {
            if (!sb.isEmpty()) sb.append(" \u00B7 ");
            sb.append(genre);
        }
        return sb.toString();
    }
}
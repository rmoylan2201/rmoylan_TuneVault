package com.example.tunevaultfx.musicplayer;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.db.ListeningEventDAO;

/**
 * Tracks one active listening session in real time.
 * Handles listened seconds, play-threshold logic,
 * database syncing, and session finalization.
 */
public class ListeningSessionTracker {

    private static final int STRONG_NEGATIVE_SKIP_SECONDS = 10;
    private static final double NEGATIVE_SKIP_RATIO = 0.20;
    private static final double POSITIVE_RATIO = 0.60;
    private static final double STRONG_POSITIVE_RATIO = 0.80;

    private final ListeningEventDAO listeningEventDAO;

    private int currentSongListenedSeconds = 0;
    private Integer currentListeningEventId = null;
    private boolean currentSessionAlreadyCountedAsPlay = false;

    public ListeningSessionTracker(ListeningEventDAO listeningEventDAO) {
        this.listeningEventDAO = listeningEventDAO;
    }

    public void startSession(String username, Song song) {
        if (song == null || username == null || username.isBlank()) {
            reset();
            return;
        }

        currentListeningEventId = listeningEventDAO.startListeningSession(username, song);
        currentSongListenedSeconds = 0;
        currentSessionAlreadyCountedAsPlay = false;
    }

    public void tick(String username, Song song) {
        if (currentListeningEventId == null || song == null || username == null || username.isBlank()) {
            return;
        }

        currentSongListenedSeconds++;

        int duration = Math.max(song.durationSeconds(), 1);
        double completionRatio = Math.min(1.0, (double) currentSongListenedSeconds / duration);

        boolean countAsPlay = reachesPlayThreshold(song, currentSongListenedSeconds);
        if (countAsPlay) {
            currentSessionAlreadyCountedAsPlay = true;
        }

        listeningEventDAO.updateListeningSession(
                currentListeningEventId,
                currentSongListenedSeconds,
                duration,
                completionRatio,
                currentSessionAlreadyCountedAsPlay
        );
    }

    public void finish(String username, Song song, boolean skipped) {
        if (currentListeningEventId == null || song == null || username == null || username.isBlank()) {
            reset();
            return;
        }

        int duration = Math.max(song.durationSeconds(), 1);
        double completionRatio = Math.min(1.0, (double) currentSongListenedSeconds / duration);

        boolean countAsPlay = currentSessionAlreadyCountedAsPlay
                || reachesPlayThreshold(song, currentSongListenedSeconds);

        String finalAction = classifyFinalAction(song, skipped);

        listeningEventDAO.finalizeListeningSession(
                currentListeningEventId,
                finalAction,
                currentSongListenedSeconds,
                duration,
                completionRatio,
                countAsPlay
        );

        reset();
    }

    public void clearProgressButKeepSession() {
        currentSongListenedSeconds = 0;
    }

    public void reset() {
        currentSongListenedSeconds = 0;
        currentListeningEventId = null;
        currentSessionAlreadyCountedAsPlay = false;
    }

    public int getCurrentSongListenedSeconds() {
        return currentSongListenedSeconds;
    }

    public boolean hasActiveSession() {
        return currentListeningEventId != null;
    }

    private boolean reachesPlayThreshold(Song song, int listenedSeconds) {
        if (song == null || listenedSeconds <= 0) {
            return false;
        }

        int duration = Math.max(song.durationSeconds(), 1);
        int threshold = Math.min(240, Math.max(30, (int) Math.ceil(duration * STRONG_POSITIVE_RATIO)));

        return listenedSeconds >= threshold;
    }

    private String classifyFinalAction(Song song, boolean skipped) {
        if (song == null) {
            return "PLAY";
        }

        int duration = Math.max(song.durationSeconds(), 1);
        double completionRatio = (double) currentSongListenedSeconds / duration;

        if (skipped) {
            if (currentSongListenedSeconds < STRONG_NEGATIVE_SKIP_SECONDS) {
                return "SKIP_EARLY";
            }
            if (completionRatio < NEGATIVE_SKIP_RATIO) {
                return "SKIP";
            }
            return "STOPPED_MID";
        }

        if (completionRatio >= STRONG_POSITIVE_RATIO) {
            return "PLAY";
        }
        if (completionRatio >= POSITIVE_RATIO) {
            return "PLAY_PARTIAL_POSITIVE";
        }
        if (completionRatio >= NEGATIVE_SKIP_RATIO) {
            return "PLAY_PARTIAL";
        }

        return "PLAY_SHORT";
    }
}
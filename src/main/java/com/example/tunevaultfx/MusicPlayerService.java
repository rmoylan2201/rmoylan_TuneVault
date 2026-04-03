
package com.example.tunevaultfx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import java.util.Collections;

public class MusicPlayerService {

    private static final MusicPlayerService instance = new MusicPlayerService();

    public static MusicPlayerService getInstance() {
        return instance;
    }

    private final ObjectProperty<Song> currentSong = new SimpleObjectProperty<>(null);
    private final BooleanProperty playing = new SimpleBooleanProperty(false);
    private final StringProperty currentTitle = new SimpleStringProperty("No song playing");
    private final StringProperty currentArtist = new SimpleStringProperty("");
    private final IntegerProperty currentSecond = new SimpleIntegerProperty(0);
    private final IntegerProperty currentDuration = new SimpleIntegerProperty(0);
    private final StringProperty currentSourcePlaylistName = new SimpleStringProperty("");
    private final BooleanProperty loopEnabled = new SimpleBooleanProperty(false);
    private final BooleanProperty shuffleEnabled = new SimpleBooleanProperty(false);


    private ObservableList<Song> currentQueue = FXCollections.observableArrayList();
    private int currentIndex = -1;

    private final Timeline timeline;

    private MusicPlayerService() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        currentSong.addListener((obs, oldVal, newVal) -> refreshMetadata());
    }

    public void playQueue(ObservableList<Song> queue, int index) {
        playQueue(queue, index, "");
    }

    public void playQueue(ObservableList<Song> queue, int index, String playlistName) {
        if (queue == null || queue.isEmpty() || index < 0 || index >= queue.size()) return;

        currentQueue = FXCollections.observableArrayList(queue);
        currentIndex = index;
        currentSourcePlaylistName.set(playlistName == null ? "" : playlistName);
        currentSong.set(currentQueue.get(currentIndex));
        currentSecond.set(0);
        setPlaying(true);
    }

    public void togglePlayPause() {
        if (currentSong.get() == null) return;
        setPlaying(!playing.get());
    }

    public void next() {
        if (currentQueue == null || currentQueue.isEmpty()) {
            stop();
            return;
        }

        if (shuffleEnabled.get() && currentQueue.size() > 1) {
            int nextIndex;
            do {
                nextIndex = (int) (Math.random() * currentQueue.size());
            } while (nextIndex == currentIndex);

            currentIndex = nextIndex;
            currentSong.set(currentQueue.get(currentIndex));
            currentSecond.set(0);
            setPlaying(true);
            return;
        }

        if (currentIndex + 1 < currentQueue.size()) {
            currentIndex++;
            currentSong.set(currentQueue.get(currentIndex));
            currentSecond.set(0);
            setPlaying(true);
        } else if (loopEnabled.get()) {
            currentIndex = 0;
            currentSong.set(currentQueue.get(currentIndex));
            currentSecond.set(0);
            setPlaying(true);
        } else {
            stop();
        }
    }

    public void previous() {
        if (currentQueue == null || currentQueue.isEmpty()) return;

        if (currentSecond.get() > 3) {
            currentSecond.set(0);
            return;
        }

        if (shuffleEnabled.get() && currentQueue.size() > 1) {
            int prevIndex;
            do {
                prevIndex = (int) (Math.random() * currentQueue.size());
            } while (prevIndex == currentIndex);

            currentIndex = prevIndex;
            currentSong.set(currentQueue.get(currentIndex));
            currentSecond.set(0);
            return;
        }

        if (currentIndex > 0) {
            currentIndex--;
            currentSong.set(currentQueue.get(currentIndex));
            currentSecond.set(0);
        } else if (loopEnabled.get()) {
            currentIndex = currentQueue.size() - 1;
            currentSong.set(currentQueue.get(currentIndex));
            currentSecond.set(0);
        } else {
            currentSecond.set(0);
        }
    }

    public void seek(int second) {
        if (currentSong.get() == null) return;
        int clamped = Math.max(0, Math.min(second, currentDuration.get()));
        currentSecond.set(clamped);
    }

    public void stop() {
        timeline.stop();
        playing.set(false);
        currentQueue.clear();
        currentIndex = -1;
        currentSong.set(null);
        currentSecond.set(0);
        currentDuration.set(0);
        currentSourcePlaylistName.set("");
    }

    public void onSongRemovedFromPlaylist(String playlistName, Song removedSong) {
        if (removedSong == null) return;
        if (currentQueue == null || currentQueue.isEmpty()) return;
        if (playlistName == null || !playlistName.equals(currentSourcePlaylistName.get())) return;

        Song current = currentSong.get();
        boolean removedCurrent = current != null && current.equals(removedSong);

        int removedIndex = currentQueue.indexOf(removedSong);
        if (removedIndex == -1) return;

        currentQueue.remove(removedIndex);

        if (currentQueue.isEmpty()) {
            stop();
            return;
        }

        if (removedCurrent) {
            if (removedIndex < currentQueue.size()) {
                currentIndex = removedIndex;
            } else {
                currentIndex = currentQueue.size() - 1;
            }
            currentSong.set(currentQueue.get(currentIndex));
            currentSecond.set(0);
            setPlaying(true);
        } else {
            if (removedIndex < currentIndex) {
                currentIndex--;
            }
        }
    }

    public void toggleLikeCurrentSong() {
        if (currentSong.get() == null) return;
        UserProfile profile = SessionManager.getCurrentUserProfile();
        if (profile != null) {
            profile.toggleLike(currentSong.get());
            SessionManager.saveCurrentProfile();
        }
    }

    public boolean isCurrentSongLiked() {
        UserProfile profile = SessionManager.getCurrentUserProfile();
        return profile != null && currentSong.get() != null && profile.isLiked(currentSong.get());
    }

    public void addCurrentSongToPlaylist(String playlistName) {
        UserProfile profile = SessionManager.getCurrentUserProfile();
        if (profile == null || currentSong.get() == null) return;

        ObservableList<Song> playlist = profile.getPlaylists().get(playlistName);
        if (playlist != null && !playlist.contains(currentSong.get())) {
            playlist.add(currentSong.get());
            SessionManager.saveCurrentProfile();
        }
    }

    private void setPlaying(boolean value) {
        playing.set(value);
        if (value) timeline.play();
        else timeline.pause();
    }

    private void tick() {
        if (currentSong.get() == null || !playing.get()) return;

        currentSecond.set(currentSecond.get() + 1);

        if (currentSecond.get() >= currentDuration.get()) {
            next();
        }
    }

    private void refreshMetadata() {
        Song song = currentSong.get();

        if (song == null) {
            currentTitle.set("No song playing");
            currentArtist.set("");
            currentDuration.set(0);
            currentSecond.set(0);
            return;
        }

        currentTitle.set(song.title());
        currentArtist.set(song.artist());
        currentDuration.set(song.durationSeconds());
    }

    public void toggleLoop() {
        loopEnabled.set(!loopEnabled.get());
    }

    public void toggleShuffle() {
        shuffleEnabled.set(!shuffleEnabled.get());

        if (shuffleEnabled.get() && currentQueue != null && !currentQueue.isEmpty()) {
            Song current = currentSong.get();
            Collections.shuffle(currentQueue);

            if (current != null) {
                int idx = currentQueue.indexOf(current);
                if (idx > 0) {
                    Collections.swap(currentQueue, 0, idx);
                }
                currentIndex = currentQueue.indexOf(current);
            }
        }
    }



    public boolean isLoopEnabled() {
        return loopEnabled.get();
    }

    public boolean isShuffleEnabled() {
        return shuffleEnabled.get();
    }

    public BooleanProperty loopEnabledProperty() {
        return loopEnabled;
    }

    public BooleanProperty shuffleEnabledProperty() {
        return shuffleEnabled;
    }

    public ObjectProperty<Song> currentSongProperty() {
        return currentSong;
    }

    public BooleanProperty playingProperty() {
        return playing;
    }

    public StringProperty currentTitleProperty() {
        return currentTitle;
    }

    public StringProperty currentArtistProperty() {
        return currentArtist;
    }

    public IntegerProperty currentSecondProperty() {
        return currentSecond;
    }

    public IntegerProperty currentDurationProperty() {
        return currentDuration;
    }

    public StringProperty currentSourcePlaylistNameProperty() {
        return currentSourcePlaylistName;
    }

    public String getCurrentSourcePlaylistName() {
        return currentSourcePlaylistName.get();
    }
}
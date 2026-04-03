package com.example.tunevaultfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class PlaylistsPageController {

    @FXML private ListView<String> playlistListView;
    @FXML private ListView<Song> playlistSongsListView;
    @FXML private ListView<Song> searchResultsListView;

    @FXML private Label selectedPlaylistLabel;
    @FXML private Label songCountLabel;
    @FXML private Label totalDurationLabel;

    @FXML private TextField searchSongsField;
    @FXML private VBox searchSongsPanel;

    private final ObservableList<String> playlistNames = FXCollections.observableArrayList();
    private final ObservableList<Song> filteredLibrarySongs = FXCollections.observableArrayList();

    private final MusicPlayerService player = MusicPlayerService.getInstance();
    private UserProfile profile;

    @FXML
    public void initialize() {
        profile = SessionManager.getCurrentUserProfile();
        if (profile == null) {
            return;
        }

        playlistNames.setAll(profile.getPlaylists().keySet());
        playlistListView.setItems(playlistNames);

        String requestedPlaylist = SessionManager.consumeRequestedPlaylistToOpen();
        if (requestedPlaylist != null && playlistNames.contains(requestedPlaylist)) {
            playlistListView.getSelectionModel().select(requestedPlaylist);
        } else if (!playlistNames.isEmpty()) {
            playlistListView.getSelectionModel().selectFirst();
        }

        playlistListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateSelectedPlaylist());

        filteredLibrarySongs.setAll(DemoLibrary.getSongs());
        searchResultsListView.setItems(filteredLibrarySongs);

        searchSongsField.textProperty().addListener((obs, oldVal, newVal) -> updateSongSearch());

        setupPlaylistSongCells();
        setupSearchSongCells();

        updateSelectedPlaylist();
        hideSearchPanel();
    }

    private void setupPlaylistSongCells() {
        playlistSongsListView.setCellFactory(listView -> new ListCell<>() {
            private final Button playButton = new Button("▶");
            private final Label titleLabel = new Label();
            private final Label artistLabel = new Label();
            private final VBox textBox = new VBox(2, titleLabel, artistLabel);
            private final HBox row = new HBox(10, playButton, textBox);

            {
                playButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 18;");
                playButton.setPrefSize(34, 34);
                playButton.setMinSize(34, 34);
                playButton.setMaxSize(34, 34);

                titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                artistLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

                textBox.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(textBox, Priority.ALWAYS);
                row.setAlignment(Pos.CENTER_LEFT);

                playButton.setOnAction(event -> {
                    Song song = getItem();
                    String selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();

                    if (song == null || selectedPlaylist == null) {
                        return;
                    }

                    ObservableList<Song> songs = profile.getPlaylists().get(selectedPlaylist);
                    if (songs == null) {
                        return;
                    }

                    int index = songs.indexOf(song);
                    if (index >= 0) {
                        player.playQueue(songs, index, selectedPlaylist);
                    }
                });
            }

            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);

                if (empty || song == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    titleLabel.setText(song.title());
                    artistLabel.setText(song.artist());
                    setText(null);
                    setGraphic(row);
                }
            }
        });
    }

    private void setupSearchSongCells() {
        searchResultsListView.setCellFactory(listView -> new ListCell<>() {
            private final Button playButton = new Button("▶");
            private final Label titleLabel = new Label();
            private final Label artistLabel = new Label();
            private final VBox textBox = new VBox(2, titleLabel, artistLabel);
            private final HBox row = new HBox(10, playButton, textBox);

            {
                playButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 18;");
                playButton.setPrefSize(34, 34);
                playButton.setMinSize(34, 34);
                playButton.setMaxSize(34, 34);

                titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                artistLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

                textBox.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(textBox, Priority.ALWAYS);
                row.setAlignment(Pos.CENTER_LEFT);

                playButton.setOnAction(event -> {
                    Song song = getItem();
                    if (song == null) {
                        return;
                    }

                    ObservableList<Song> queue = FXCollections.observableArrayList(filteredLibrarySongs);
                    int index = queue.indexOf(song);
                    if (index >= 0) {
                        player.playQueue(queue, index, "");
                    }
                });
            }

            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);

                if (empty || song == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    titleLabel.setText(song.title());
                    artistLabel.setText(song.artist());
                    setText(null);
                    setGraphic(row);
                }
            }
        });
    }

    @FXML
    private void handleShowSearchSongs() {
        String selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();

        if (selectedPlaylist == null) {
            showAlert("No Playlist Selected", "Please select a playlist first.");
            return;
        }

        searchSongsPanel.setVisible(true);
        searchSongsPanel.setManaged(true);
        searchSongsField.requestFocus();
    }

    @FXML
    private void handleHideSearchSongs() {
        hideSearchPanel();
    }

    private void hideSearchPanel() {
        searchSongsPanel.setVisible(false);
        searchSongsPanel.setManaged(false);
    }

    @FXML
    private void handleCreatePlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Playlist");
        dialog.setHeaderText("Create a new playlist");
        dialog.setContentText("Playlist name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String name = result.get().trim();

        if (name.isEmpty()) {
            showAlert("Invalid Name", "Playlist name cannot be empty.");
            return;
        }

        if (profile.getPlaylists().containsKey(name)) {
            showAlert("Duplicate Playlist", "A playlist with that name already exists.");
            return;
        }

        String currentSelection = playlistListView.getSelectionModel().getSelectedItem();

        profile.getPlaylists().put(name, FXCollections.observableArrayList());
        playlistNames.add(name);
        SessionManager.saveCurrentProfile();

        if (currentSelection != null) {
            playlistListView.getSelectionModel().select(currentSelection);
        } else {
            playlistListView.getSelectionModel().select(name);
        }

        updateSelectedPlaylist();
    }

    @FXML
    private void handleDeletePlaylist() {
        String selected = playlistListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No Playlist Selected", "Please select a playlist to delete.");
            return;
        }

        if ("Liked Songs".equals(selected)) {
            showAlert("Protected Playlist", "Liked Songs cannot be deleted.");
            return;
        }

        profile.getPlaylists().remove(selected);
        playlistNames.remove(selected);
        SessionManager.saveCurrentProfile();

        if (!playlistNames.isEmpty()) {
            playlistListView.getSelectionModel().selectFirst();
        }

        updateSelectedPlaylist();
    }

    @FXML
    private void handleRemoveSong() {
        String selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
        Song selectedSong = playlistSongsListView.getSelectionModel().getSelectedItem();

        if (selectedPlaylist == null || selectedSong == null) {
            showAlert("Selection Needed", "Select a playlist and a song.");
            return;
        }

        profile.getPlaylists().get(selectedPlaylist).remove(selectedSong);
        player.onSongRemovedFromPlaylist(selectedPlaylist, selectedSong);
        SessionManager.saveCurrentProfile();
        updateSelectedPlaylist();
    }

    @FXML
    private void handleAddSelectedSearchSong() {
        String selectedPlaylist = playlistListView.getSelectionModel().getSelectedItem();
        Song selectedSong = searchResultsListView.getSelectionModel().getSelectedItem();

        if (selectedPlaylist == null) {
            showAlert("No Playlist Selected", "Please select a playlist first.");
            return;
        }

        if (selectedSong == null) {
            showAlert("No Song Selected", "Please select a song from search results.");
            return;
        }

        ObservableList<Song> songs = profile.getPlaylists().get(selectedPlaylist);

        if (!songs.contains(selectedSong)) {
            songs.add(selectedSong);
            SessionManager.saveCurrentProfile();
            updateSelectedPlaylist();
        } else {
            showAlert("Already Added", "That song is already in the playlist.");
        }
    }

    @FXML
    private void handleGoToNowPlaying(javafx.event.ActionEvent event) throws java.io.IOException {
        SceneUtil.switchScene((javafx.scene.Node) event.getSource(), "nowplaying-page.fxml");
    }

    @FXML
    private void handleBackToMenu(javafx.event.ActionEvent event) throws java.io.IOException {
        SceneUtil.switchScene((javafx.scene.Node) event.getSource(), "main-menu.fxml");
    }

    private void updateSelectedPlaylist() {
        String selected = playlistListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            playlistSongsListView.getItems().clear();
            selectedPlaylistLabel.setText("No playlist selected");
            songCountLabel.setText("Songs: 0");
            totalDurationLabel.setText("Duration: 0:00");
            return;
        }

        ObservableList<Song> songs = profile.getPlaylists().get(selected);
        if (songs == null) {
            songs = FXCollections.observableArrayList();
        }

        playlistSongsListView.setItems(songs);
        selectedPlaylistLabel.setText(selected);
        songCountLabel.setText("Songs: " + songs.size());

        int totalSeconds = songs.stream().mapToInt(Song::durationSeconds).sum();
        totalDurationLabel.setText("Duration: " + formatTime(totalSeconds));
    }

    private void updateSongSearch() {
        String search = searchSongsField.getText() == null ? "" : searchSongsField.getText().trim().toLowerCase();
        filteredLibrarySongs.clear();

        for (Song song : DemoLibrary.getSongs()) {
            if (search.isEmpty()
                    || song.title().toLowerCase().contains(search)
                    || song.artist().toLowerCase().contains(search)
                    || song.album().toLowerCase().contains(search)) {
                filteredLibrarySongs.add(song);
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }
}
package com.example.tunevaultfx.search;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.db.SongDAO;
import com.example.tunevaultfx.musicplayer.controller.MusicPlayerController;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.util.AlertUtil;
import com.example.tunevaultfx.util.SceneUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.IOException;

public class ArtistProfileController {

    @FXML private Label artistNameLabel;
    @FXML private Label artistSummaryLabel;
    @FXML private ListView<Song> artistSongsListView;

    private final ObservableList<Song> artistSongs = FXCollections.observableArrayList();

    private final SongDAO songDAO = new SongDAO();
    private final MusicPlayerController player = MusicPlayerController.getInstance();

    private String artistName;

    @FXML
    public void initialize() {
        artistName = SessionManager.getSelectedArtist();

        if (artistName == null || artistName.isBlank()) {
            artistNameLabel.setText("Unknown Artist");
            artistSummaryLabel.setText("No artist selected.");
            artistSongsListView.setItems(artistSongs);
            return;
        }

        artistNameLabel.setText(artistName);
        artistSongsListView.setItems(artistSongs);

        setupSongCells();
        setupDoubleClick();
        loadArtistSongs();
    }

    private void loadArtistSongs() {
        try {
            ObservableList<Song> allSongs = FXCollections.observableArrayList(songDAO.getAllSongs());

            artistSongs.clear();
            for (Song song : allSongs) {
                if (song != null && artistName.equalsIgnoreCase(song.artist())) {
                    artistSongs.add(song);
                }
            }

            artistSummaryLabel.setText("Songs available: " + artistSongs.size());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.info("Database Error", "Could not load songs for this artist.");
        }
    }

    private void setupSongCells() {
        artistSongsListView.setCellFactory(listView -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);

                if (empty || song == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                setText(song.title() + " | " + song.album() + " | " + song.genre());
            }
        });
    }

    private void setupDoubleClick() {
        artistSongsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song selectedSong = artistSongsListView.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    int index = artistSongs.indexOf(selectedSong);
                    player.playQueue(artistSongs, index, artistName);
                    SessionManager.setSelectedSong(selectedSong);

                    try {
                        SceneUtil.switchScene(artistSongsListView, "song-details-page.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @FXML
    private void handleBackToSearch(javafx.event.ActionEvent event) throws IOException {
        SceneUtil.switchScene((Node) event.getSource(), "search-page.fxml");
    }
}
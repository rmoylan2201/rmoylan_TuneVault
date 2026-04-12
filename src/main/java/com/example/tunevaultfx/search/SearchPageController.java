package com.example.tunevaultfx.search;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.db.SongDAO;
import com.example.tunevaultfx.musicplayer.controller.MusicPlayerController;
import com.example.tunevaultfx.playlist.service.SongSearchService;
import com.example.tunevaultfx.recommendation.RecommendationService;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.util.AlertUtil;
import com.example.tunevaultfx.util.SceneUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class SearchPageController {

    @FXML private TextField searchField;
    @FXML private ListView<Song> songResultsListView;
    @FXML private ListView<String> artistResultsListView;
    @FXML private ListView<SearchRecentItem> recentSearchesListView;
    @FXML private ListView<Song> suggestedSongsListView;
    @FXML private Label resultsSummaryLabel;
    @FXML private VBox recentSection;
    @FXML private HBox resultsSection;

    private final ObservableList<Song> allSongs = FXCollections.observableArrayList();
    private final ObservableList<Song> filteredSongs = FXCollections.observableArrayList();
    private final ObservableList<String> filteredArtists = FXCollections.observableArrayList();

    private final SongDAO songDAO = new SongDAO();
    private final SongSearchService songSearchService = new SongSearchService();
    private final RecommendationService recommendationService = new RecommendationService();
    private final MusicPlayerController player = MusicPlayerController.getInstance();

    @FXML
    public void initialize() {
        loadSongs();

        songResultsListView.setItems(filteredSongs);
        artistResultsListView.setItems(filteredArtists);
        recentSearchesListView.setItems(SessionManager.getRecentSearches());

        songResultsListView.setPlaceholder(new Label("No matching songs."));
        artistResultsListView.setPlaceholder(new Label("No matching artists."));
        recentSearchesListView.setPlaceholder(new Label("No recent searches yet."));
        suggestedSongsListView.setPlaceholder(new Label("No suggestions yet."));
        resultsSummaryLabel.setText("Start typing to search.");

        setupSongCells();
        setupArtistCells();
        setupRecentCells();
        setupSuggestedSongCells();
        setupListeners();
        setupDoubleClickActions();
        loadSuggestedSongs();
        showRecentMode();
    }

    private void loadSongs() {
        try {
            allSongs.setAll(songDAO.getAllSongs());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.info("Database Error", "Could not load songs from the database.");
        }
    }

    private void loadSuggestedSongs() {
        suggestedSongsListView.setItems(
                recommendationService.getSuggestedSongsForUser(SessionManager.getCurrentUsername(), 12)
        );
    }

    private void setupSongCells() {
        songResultsListView.setCellFactory(listView -> buildSongCell());
    }

    private void setupSuggestedSongCells() {
        suggestedSongsListView.setCellFactory(listView -> buildSongCell());

        suggestedSongsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song selectedSong = suggestedSongsListView.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    player.playSingleSong(selectedSong);
                    SessionManager.addRecentSearch(SearchRecentItem.song(selectedSong));
                }
            }
        });
    }

    private ListCell<Song> buildSongCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);

                if (empty || song == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label title = new Label(song.title());
                title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

                String metaText = song.artist();
                if (song.genre() != null && !song.genre().isBlank()) {
                    metaText += " • " + song.genre();
                }

                Label meta = new Label(metaText);
                meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

                VBox box = new VBox(4, title, meta);
                box.setPadding(new Insets(8, 6, 8, 6));

                setText(null);
                setGraphic(box);
            }
        };
    }

    private void setupArtistCells() {
        artistResultsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String artist, boolean empty) {
                super.updateItem(artist, empty);

                if (empty || artist == null || artist.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label name = new Label(artist);
                name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

                Label meta = new Label("Artist");
                meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

                VBox box = new VBox(4, name, meta);
                box.setPadding(new Insets(8, 6, 8, 6));

                setText(null);
                setGraphic(box);
            }
        });
    }

    private void setupRecentCells() {
        recentSearchesListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(SearchRecentItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label primary = new Label(item.getPrimaryText());
                primary.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

                Label secondary = new Label(item.getSecondaryText());
                secondary.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

                VBox textBox = new VBox(4, primary, secondary);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox row = new HBox(12, textBox, spacer);
                row.setPadding(new Insets(10, 6, 10, 6));

                setText(null);
                setGraphic(row);
            }
        });

        recentSearchesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SearchRecentItem selected = recentSearchesListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openRecentItem(selected);
                }
            }
        });
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> runSearch(newVal));
    }

    private void setupDoubleClickActions() {
        songResultsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song selectedSong = songResultsListView.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    player.playSingleSong(selectedSong);
                    SessionManager.addRecentSearch(SearchRecentItem.song(selectedSong));
                }
            }
        });

        artistResultsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedArtist = artistResultsListView.getSelectionModel().getSelectedItem();
                if (selectedArtist != null && !selectedArtist.isBlank()) {
                    SessionManager.setSelectedArtist(selectedArtist);
                    SessionManager.addRecentSearch(SearchRecentItem.artist(selectedArtist));
                    try {
                        SceneUtil.switchScene(artistResultsListView, "artist-profile-page.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void runSearch(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.trim();

        if (query.isBlank()) {
            filteredSongs.clear();
            filteredArtists.clear();
            resultsSummaryLabel.setText("Start typing to search.");
            showRecentMode();
            return;
        }

        ObservableList<Song> songMatches = songSearchService.filterSongs(allSongs, query);
        filteredSongs.setAll(songMatches);

        Set<String> artistSet = new LinkedHashSet<>();
        String lowerQuery = query.toLowerCase();

        for (Song song : allSongs) {
            if (song == null || song.artist() == null) {
                continue;
            }

            if (song.artist().toLowerCase().contains(lowerQuery)) {
                artistSet.add(song.artist());
            }
        }

        filteredArtists.setAll(artistSet);
        resultsSummaryLabel.setText("Songs: " + filteredSongs.size() + " | Artists: " + filteredArtists.size());
        showSearchResultsMode();
    }

    private void showRecentMode() {
        recentSection.setVisible(true);
        recentSection.setManaged(true);

        resultsSection.setVisible(false);
        resultsSection.setManaged(false);
    }

    private void showSearchResultsMode() {
        recentSection.setVisible(false);
        recentSection.setManaged(false);

        resultsSection.setVisible(true);
        resultsSection.setManaged(true);
    }

    private void openRecentItem(SearchRecentItem item) {
        if (item.getType() == SearchRecentItem.Type.SONG && item.getSong() != null) {
            player.playSingleSong(item.getSong());
            SessionManager.addRecentSearch(SearchRecentItem.song(item.getSong()));
            return;
        }

        if (item.getType() == SearchRecentItem.Type.ARTIST && item.getArtistName() != null) {
            SessionManager.setSelectedArtist(item.getArtistName());
            SessionManager.addRecentSearch(SearchRecentItem.artist(item.getArtistName()));
            try {
                SceneUtil.switchScene(recentSearchesListView, "artist-profile-page.fxml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleBackToMenu(javafx.event.ActionEvent event) throws IOException {
        SceneUtil.switchScene((Node) event.getSource(), "main-menu.fxml");
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        filteredSongs.clear();
        filteredArtists.clear();
        resultsSummaryLabel.setText("Start typing to search.");
        showRecentMode();
    }

    @FXML
    private void handleClearRecentSearches() {
        SessionManager.clearRecentSearches();
        recentSearchesListView.refresh();
    }
}
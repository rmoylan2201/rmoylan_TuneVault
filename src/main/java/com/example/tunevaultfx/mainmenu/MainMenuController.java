package com.example.tunevaultfx.mainmenu;

import com.example.tunevaultfx.core.PlaylistNames;
import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.db.ListeningEventDAO;
import com.example.tunevaultfx.db.SongDAO;
import com.example.tunevaultfx.db.UserGenreDiscoveryDAO;
import com.example.tunevaultfx.db.UserGenreDiscoverySummary;
import com.example.tunevaultfx.musicplayer.controller.MusicPlayerController;
import com.example.tunevaultfx.playlist.PlaylistLibraryContextMenu;
import com.example.tunevaultfx.playlist.service.PlaylistService;
import com.example.tunevaultfx.recommendation.RecommendationService;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.user.UserProfile;
import com.example.tunevaultfx.util.AppTheme;
import com.example.tunevaultfx.util.CellStyleKit;
import com.example.tunevaultfx.util.SceneUtil;
import com.example.tunevaultfx.util.SongContextMenuBuilder;
import com.example.tunevaultfx.util.UiMotionUtil;
import com.example.tunevaultfx.view.FxmlResources;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakMapChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Home dashboard: libraries, personalized picks, top artists by listening.
 */
public class MainMenuController {

    @FXML private VBox homeHeroSection;
    @FXML private VBox homeLibrariesSection;
    @FXML private VBox forYouFeedCard;
    @FXML private VBox freshTracksFeedCard;
    @FXML private VBox menuContent;
    @FXML private HBox homeFeedRow;
    @FXML private HBox homeStatsFooter;
    @FXML private TilePane playlistChipsContainer;
    @FXML private Label homeGreetingLabel;
    @FXML private Label homeStatsLabel;
    @FXML private Label homeVibeLabel;
    @FXML private Label homeVibeSubLabel;
    @FXML private Label noPlaylistsHint;
    @FXML private ListView<Song> forYouListView;
    @FXML private ListView<String> freshTracksListView;
    @FXML private VBox homeTodaySection;
    @FXML private FlowPane homeTodayFlow;
    @FXML private VBox homeTodayTracksHost;
    @FXML private VBox homeTodayArtistsHost;
    @FXML private VBox homeChartsSection;
    @FXML private FlowPane homeChartsFlow;
    @FXML private VBox homeTopSongsHost;
    @FXML private VBox homeTopArtistsHost;

    private final RecommendationService recommendationService = new RecommendationService();
    private final PlaylistService playlistService = new PlaylistService();
    private final MusicPlayerController player = MusicPlayerController.getInstance();
    private final SongDAO songDAO = new SongDAO();
    private final ListeningEventDAO listeningEventDAO = new ListeningEventDAO();
    private final UserGenreDiscoveryDAO genreDiscoveryDAO = new UserGenreDiscoveryDAO();
    private final HomeListeningChartsService homeChartsService = new HomeListeningChartsService();

    private final MapChangeListener<String, ObservableList<Song>> homePlaylistKeysChanged =
            c -> Platform.runLater(this::refreshHomeLibraryStrip);

    /** Inline ▶ / ⏸ on home chart rows; cleared before rebuilding today + all-time charts. */
    private record ChartPlayBinding(Button button, Song song) {}

    private final List<ChartPlayBinding> chartPlayBindings = new ArrayList<>();

    /** Re-applies now-playing chrome on static chart rows when {@link #player} changes. */
    private final List<Runnable> chartRowPlaybackSyncers = new ArrayList<>();

    @FXML
    public void initialize() {
        String user = SessionManager.getCurrentUsername();
        if (homeGreetingLabel != null) {
            homeGreetingLabel.setText(
                    user != null && !user.isBlank()
                            ? "Hey, " + user + " \u2014 here's your space."
                            : "Welcome to TuneVault");
        }

        UserProfile profile = SessionManager.getCurrentUserProfile();
        if (profile != null && profile.getPlaylists() != null) {
            profile.getPlaylists().addListener(new WeakMapChangeListener<>(homePlaylistKeysChanged));
        }
        int playlistCount =
                profile != null && profile.getPlaylists() != null ? profile.getPlaylists().size() : 0;
        int songTotal = countUniqueSavedSongs(profile);
        if (homeStatsLabel != null) {
            homeStatsLabel.setText(
                    playlistCount + " playlist" + (playlistCount != 1 ? "s" : "")
                            + "  \u00B7  "
                            + songTotal + " saved song" + (songTotal != 1 ? "s" : ""));
        }

        bindPlaylistTileSizingOnce();
        buildPlaylistChips(profile);
        loadFeedLists(user);
        applyHomeVibe(user);
        bindHomeChartsFlowWrap();
        bindHomeTodayFlowWrap();
        chartPlayBindings.clear();
        chartRowPlaybackSyncers.clear();
        loadTodayCharts(user);
        loadHomeCharts(user);

        player.currentSongProperty()
                .addListener((o, a, b) -> Platform.runLater(this::refreshHomePlaybackAffordances));
        player.playingProperty()
                .addListener((o, a, b) -> Platform.runLater(this::refreshHomePlaybackAffordances));

        setupHomeListView(forYouListView);
        setupTopArtistsListView(freshTracksListView);
        Platform.runLater(this::refreshHomePlaybackAffordances);

        if (menuContent != null) {
            menuContent.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) {
                    return;
                }
                applyResponsiveDensity(newScene.getWidth());
                newScene.widthProperty().addListener((o, oldW, newW) ->
                        applyResponsiveDensity(newW.doubleValue()));
            });
        }

        // Double defer: first layout pass applies sizes; then we hide + stagger (avoids a one-frame flash).
        Platform.runLater(
                () ->
                        Platform.runLater(
                                () -> {
                                    UiMotionUtil.playStaggeredEntrance(
                                            List.of(
                                                    homeHeroSection,
                                                    homeLibrariesSection,
                                                    forYouFeedCard,
                                                    freshTracksFeedCard,
                                                    homeTodaySection,
                                                    homeChartsSection,
                                                    homeStatsFooter));
                                    UiMotionUtil.applyHoverLift(forYouFeedCard);
                                    UiMotionUtil.applyHoverLift(freshTracksFeedCard);
                                    if (homeTodayTracksHost != null) {
                                        UiMotionUtil.applyHoverLift(homeTodayTracksHost);
                                    }
                                    if (homeTodayArtistsHost != null) {
                                        UiMotionUtil.applyHoverLift(homeTodayArtistsHost);
                                    }
                                    if (homeTopSongsHost != null) {
                                        UiMotionUtil.applyHoverLift(homeTopSongsHost);
                                    }
                                    if (homeTopArtistsHost != null) {
                                        UiMotionUtil.applyHoverLift(homeTopArtistsHost);
                                    }
                                    if (menuContent != null) {
                                        menuContent.setFocusTraversable(true);
                                        menuContent.requestFocus();
                                    }
                                }));
    }

    private void refreshHomePlaybackAffordances() {
        for (ChartPlayBinding b : chartPlayBindings) {
            if (b.button().getScene() == null) {
                continue;
            }
            Song cur = player.getCurrentSong();
            boolean same = cur != null && cur.songId() == b.song().songId();
            b.button().setText(same && player.isPlaying() ? "⏸" : "▶");
        }
        if (forYouListView != null) {
            forYouListView.refresh();
        }
        for (Runnable r : chartRowPlaybackSyncers) {
            r.run();
        }
    }

    private void bindHomeChartsFlowWrap() {
        if (homeChartsFlow == null || menuContent == null) {
            return;
        }
        homeChartsFlow.prefWrapLengthProperty()
                .bind(
                        Bindings.createDoubleBinding(
                                () -> Math.max(340, menuContent.getWidth() - 56),
                                menuContent.widthProperty()));
    }

    private void bindHomeTodayFlowWrap() {
        if (homeTodayFlow == null || menuContent == null) {
            return;
        }
        homeTodayFlow.prefWrapLengthProperty()
                .bind(
                        Bindings.createDoubleBinding(
                                () -> Math.max(340, menuContent.getWidth() - 56),
                                menuContent.widthProperty()));
    }

    private void loadTodayCharts(String username) {
        if (homeTodayTracksHost == null || homeTodayArtistsHost == null) {
            return;
        }
        homeTodayTracksHost.getChildren().clear();
        homeTodayArtistsHost.getChildren().clear();

        if (username == null || username.isBlank()) {
            Label signInTracks = new Label("Sign in to see what you\u2019ve played today.");
            signInTracks.getStyleClass().add("home-charts-column-empty");
            signInTracks.setWrapText(true);
            homeTodayTracksHost
                    .getChildren()
                    .addAll(
                            buildChartCardHeader("Today\u2019s top tracks", "Listening time from today only"),
                            new Separator(),
                            signInTracks);
            Label signInArtists = new Label("Sign in to see which artists you\u2019ve played today.");
            signInArtists.getStyleClass().add("home-charts-column-empty");
            signInArtists.setWrapText(true);
            homeTodayArtistsHost
                    .getChildren()
                    .addAll(
                            buildChartCardHeader("Today\u2019s top artists", "Listening time from today only"),
                            new Separator(),
                            signInArtists);
            return;
        }

        try {
            List<HomeListeningChartsService.ChartSongEntry> songs =
                    homeChartsService.loadTopSongsToday(username, 5);
            List<HomeListeningChartsService.ChartArtistEntry> artists =
                    homeChartsService.loadTopArtistsToday(username, 5);

            if (songs.isEmpty()) {
                Label empty = new Label("Nothing logged today yet \u2014 press play and check back later.");
                empty.getStyleClass().add("home-charts-column-empty");
                empty.setWrapText(true);
                homeTodayTracksHost
                        .getChildren()
                        .addAll(
                                buildChartCardHeader("Today\u2019s top tracks", "Listening time from today only"),
                                new Separator(),
                                empty);
            } else {
                populateSongsChartCard(
                        homeTodayTracksHost,
                        songs,
                        "Today\u2019s top tracks",
                        "Listening time from today only");
            }

            if (artists.isEmpty()) {
                Label empty = new Label("No artist time today yet \u2014 start a playlist.");
                empty.getStyleClass().add("home-charts-column-empty");
                empty.setWrapText(true);
                homeTodayArtistsHost
                        .getChildren()
                        .addAll(
                                buildChartCardHeader("Today\u2019s top artists", "Listening time from today only"),
                                new Separator(),
                                empty);
            } else {
                populateArtistsChartCard(
                        homeTodayArtistsHost,
                        artists,
                        "Today\u2019s top artists",
                        "Listening time from today only");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label err = new Label("Couldn\u2019t load today\u2019s charts.");
            err.getStyleClass().add("home-charts-column-empty");
            err.setWrapText(true);
            homeTodayTracksHost
                    .getChildren()
                    .addAll(buildChartCardHeader("Today\u2019s top tracks", ""), new Separator(), err);
            Label errArtists = new Label(err.getText());
            errArtists.getStyleClass().add("home-charts-column-empty");
            errArtists.setWrapText(true);
            homeTodayArtistsHost
                    .getChildren()
                    .addAll(buildChartCardHeader("Today\u2019s top artists", ""), new Separator(), errArtists);
        }
    }

    private void loadHomeCharts(String username) {
        if (homeTopSongsHost == null || homeTopArtistsHost == null) {
            return;
        }
        homeTopSongsHost.getChildren().clear();
        homeTopArtistsHost.getChildren().clear();
        homeTopArtistsHost.setVisible(true);
        homeTopArtistsHost.setManaged(true);

        if (username == null || username.isBlank()) {
            showHomeChartsPlaceholder(
                    "Sign in to see charts built from your listening time.");
            return;
        }

        try {
            List<HomeListeningChartsService.ChartSongEntry> songs =
                    homeChartsService.loadTopSongs(username, 5);
            List<HomeListeningChartsService.ChartArtistEntry> artists =
                    homeChartsService.loadTopArtists(username, 5);

            if (songs.isEmpty() && artists.isEmpty()) {
                showHomeChartsPlaceholder(
                        "Your charts will appear here after you listen — we rank tracks and artists by the time you spend with them.");
                return;
            }

            if (!songs.isEmpty()) {
                populateSongsChartCard(
                        homeTopSongsHost,
                        songs,
                        "Top tracks",
                        "By total listening time");
            } else {
                Label empty = new Label("No track data yet — press play on something you love.");
                empty.getStyleClass().add("home-charts-column-empty");
                empty.setWrapText(true);
                homeTopSongsHost.getChildren()
                        .addAll(buildChartCardHeader("Top tracks", "By total listening time"), new Separator(), empty);
            }

            if (!artists.isEmpty()) {
                populateArtistsChartCard(
                        homeTopArtistsHost,
                        artists,
                        "Top artists",
                        "By total listening time");
            } else {
                Label empty = new Label("Artist rankings will show up once you have listening history.");
                empty.getStyleClass().add("home-charts-column-empty");
                empty.setWrapText(true);
                homeTopArtistsHost.getChildren()
                        .addAll(buildChartCardHeader("Top artists", "By total listening time"), new Separator(), empty);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showHomeChartsPlaceholder("Couldn\u2019t load charts right now. Everything else still works.");
        }
    }

    private void showHomeChartsPlaceholder(String message) {
        homeTopArtistsHost.setVisible(false);
        homeTopArtistsHost.setManaged(false);
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        Label glyph = new Label("\u266B");
        glyph.getStyleClass().add("home-charts-empty-glyph");
        Label headline = new Label("Charts unlock with listening");
        headline.getStyleClass().add("home-charts-empty-title");
        Label body = new Label(message);
        body.getStyleClass().add("home-charts-empty-body");
        body.setWrapText(true);
        box.getChildren().addAll(glyph, headline, body);
        homeTopSongsHost.getChildren().add(box);
    }

    private VBox buildChartCardHeader(String title, String subtitle) {
        VBox v = new VBox(4);
        Label t = new Label(title);
        t.getStyleClass().add("home-charts-card-title");
        Label s = new Label(subtitle);
        s.getStyleClass().add("home-charts-card-subtitle");
        s.setWrapText(true);
        v.getChildren().addAll(t, s);
        return v;
    }

    private void populateSongsChartCard(
            VBox host,
            List<HomeListeningChartsService.ChartSongEntry> entries,
            String title,
            String subtitle) {
        int maxSec = entries.stream().mapToInt(HomeListeningChartsService.ChartSongEntry::listenedSeconds).max().orElse(1);
        host.getChildren().add(buildChartCardHeader(title, subtitle));
        host.getChildren().add(new Separator());
        VBox rows = new VBox(12);
        int rank = 1;
        for (HomeListeningChartsService.ChartSongEntry e : entries) {
            rows.getChildren().add(createSongChartRow(rank++, e, maxSec));
        }
        host.getChildren().add(rows);
    }

    private void populateArtistsChartCard(
            VBox host,
            List<HomeListeningChartsService.ChartArtistEntry> entries,
            String title,
            String subtitle) {
        int maxSec = entries.stream().mapToInt(HomeListeningChartsService.ChartArtistEntry::listenedSeconds).max().orElse(1);
        host.getChildren().add(buildChartCardHeader(title, subtitle));
        host.getChildren().add(new Separator());
        VBox rows = new VBox(12);
        int rank = 1;
        for (HomeListeningChartsService.ChartArtistEntry e : entries) {
            rows.getChildren().add(createArtistChartRow(rank++, e, maxSec));
        }
        host.getChildren().add(rows);
    }

    private HBox createSongChartRow(
            int rank, HomeListeningChartsService.ChartSongEntry entry, int maxSeconds) {
        Song song = entry.song();
        int sec = entry.listenedSeconds();

        StackPane rankBadge = new StackPane();
        rankBadge.setMinSize(40, 40);
        rankBadge.setPrefSize(40, 40);
        rankBadge.setMaxSize(40, 40);
        rankBadge.getStyleClass().addAll("home-charts-rank", "home-charts-rank--" + rank);
        Label rankLbl = new Label(Integer.toString(rank));
        rankLbl.getStyleClass().add("home-charts-rank-label");
        rankBadge.getChildren().add(rankLbl);

        Label title = new Label(song.title());
        title.getStyleClass().add("home-charts-row-title");
        title.setWrapText(true);

        Region edgeBar = CellStyleKit.nowPlayingEdgeBar();

        Hyperlink artistLink = new Hyperlink(song.artist());
        artistLink.getStyleClass().add("home-charts-row-meta-link");
        artistLink.setOnAction(ev -> openArtistProfile(song.artist()));

        VBox textCol = new VBox(2, title, artistLink);
        textCol.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textCol, Priority.ALWAYS);

        Label timeLbl = new Label(formatListenTime(sec));
        timeLbl.getStyleClass().add("home-charts-row-time");

        HBox topLine = new HBox(12, textCol, timeLbl);
        topLine.setAlignment(Pos.CENTER_LEFT);

        ProgressBar bar = new ProgressBar((double) sec / Math.max(1, maxSeconds));
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(5);
        bar.getStyleClass().add("home-charts-progress");

        VBox rightCol = new VBox(8, topLine, bar);
        rightCol.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        Button play = new Button("\u25B6");
        play.setStyle(homeFeedPlayButtonStyle());
        play.setPrefSize(36, 36);
        play.setMinSize(36, 36);
        play.setFocusTraversable(false);
        play.setOnAction(ev -> player.playSingleSong(song));
        chartPlayBindings.add(new ChartPlayBinding(play, song));

        HBox row = new HBox(14, edgeBar, rankBadge, rightCol, play);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 10));
        row.getStyleClass().add("home-charts-row");

        Runnable syncChartRowPlayingChrome =
                () -> {
                    if (row.getScene() == null) {
                        return;
                    }
                    Song cur = player.getCurrentSong();
                    boolean isCur = cur != null && cur.songId() == song.songId();
                    edgeBar.setVisible(isCur);
                    edgeBar.setManaged(isCur);
                    CellStyleKit.markPlaying(row, isCur);
                    if (isCur) {
                        title.setStyle("-fx-text-fill: " + CellStyleKit.getAccentTitle() + ";");
                    } else {
                        title.setStyle(null);
                    }
                };
        chartRowPlaybackSyncers.add(syncChartRowPlayingChrome);
        syncChartRowPlayingChrome.run();

        row.setOnMouseClicked(
                ev -> {
                    if (ev.getButton() == MouseButton.PRIMARY && ev.getClickCount() == 2) {
                        player.playSingleSong(song);
                        ev.consume();
                    }
                });
        row.addEventFilter(
                ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                ev -> {
                    ContextMenu cm =
                            SongContextMenuBuilder.build(
                                    song, row, SongContextMenuBuilder.Spec.general());
                    cm.show(row, ev.getScreenX(), ev.getScreenY());
                    ev.consume();
                });

        return row;
    }

    private HBox createArtistChartRow(
            int rank, HomeListeningChartsService.ChartArtistEntry entry, int maxSeconds) {
        String name = entry.artistName();
        int sec = entry.listenedSeconds();

        StackPane rankBadge = new StackPane();
        rankBadge.setMinSize(40, 40);
        rankBadge.setPrefSize(40, 40);
        rankBadge.setMaxSize(40, 40);
        rankBadge.getStyleClass().addAll("home-charts-rank", "home-charts-rank-artist--" + rank);
        Label rankLbl = new Label(Integer.toString(rank));
        rankLbl.getStyleClass().add("home-charts-rank-label");
        rankBadge.getChildren().add(rankLbl);

        Label title = new Label(name);
        title.getStyleClass().add("home-charts-row-title");
        title.setWrapText(true);

        Label meta = new Label("Artist");
        meta.getStyleClass().add("home-charts-row-meta");

        VBox textCol = new VBox(2, title, meta);
        textCol.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textCol, Priority.ALWAYS);

        Label timeLbl = new Label(formatListenTime(sec));
        timeLbl.getStyleClass().add("home-charts-row-time");

        HBox topLine = new HBox(12, textCol, timeLbl);
        topLine.setAlignment(Pos.CENTER_LEFT);

        ProgressBar bar = new ProgressBar((double) sec / Math.max(1, maxSeconds));
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(5);
        bar.getStyleClass().add("home-charts-progress home-charts-progress-artist");

        VBox rightCol = new VBox(8, topLine, bar);
        rightCol.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        HBox row = new HBox(14, rankBadge, rightCol);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 10));
        row.getStyleClass().add("home-charts-row");
        CellStyleKit.addHover(row);

        row.setOnMouseClicked(
                ev -> {
                    if (ev.getButton() == MouseButton.PRIMARY) {
                        openArtistProfile(name);
                        ev.consume();
                    }
                });

        return row;
    }

    private static String formatListenTime(int totalSeconds) {
        if (totalSeconds <= 0) {
            return "0 min";
        }
        if (totalSeconds < 60) {
            return totalSeconds + " sec";
        }
        int minutes = totalSeconds / 60;
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " min" : " min");
        }
        int hours = minutes / 60;
        int m = minutes % 60;
        return hours + "h " + m + "m";
    }

    private void refreshHomeLibraryStrip() {
        UserProfile profile = SessionManager.getCurrentUserProfile();
        int playlistCount =
                profile != null && profile.getPlaylists() != null ? profile.getPlaylists().size() : 0;
        int songTotal = countUniqueSavedSongs(profile);
        if (homeStatsLabel != null) {
            homeStatsLabel.setText(
                    playlistCount + " playlist" + (playlistCount != 1 ? "s" : "")
                            + "  \u00B7  "
                            + songTotal + " saved song" + (songTotal != 1 ? "s" : ""));
        }
        buildPlaylistChips(profile);
    }

    private void applyHomeVibe(String username) {
        if (homeVibeLabel == null) {
            return;
        }
        if (username == null || username.isBlank()) {
            homeVibeLabel.setVisible(false);
            homeVibeLabel.setManaged(false);
            if (homeVibeSubLabel != null) {
                homeVibeSubLabel.setVisible(false);
                homeVibeSubLabel.setManaged(false);
            }
            return;
        }
        try {
            Optional<UserGenreDiscoverySummary> opt = genreDiscoveryDAO.loadSummary(username);
            if (opt.isEmpty()) {
                homeVibeLabel.getStyleClass().setAll("caption");
                homeVibeLabel.setText(
                        "When you\u2019re ready, use Account \u2192 Profile (or Genre Quiz in the sidebar) "
                                + "to shape recommendations.");
                homeVibeLabel.setVisible(true);
                homeVibeLabel.setManaged(true);
                if (homeVibeSubLabel != null) {
                    homeVibeSubLabel.setText("");
                    homeVibeSubLabel.setVisible(false);
                    homeVibeSubLabel.setManaged(false);
                }
                return;
            }
            UserGenreDiscoverySummary s = opt.get();
            homeVibeLabel.getStyleClass().setAll("home-vibe-line");
            homeVibeLabel.setText("Your vibe: " + s.blendLine());
            homeVibeLabel.setVisible(true);
            homeVibeLabel.setManaged(true);
            if (homeVibeSubLabel != null) {
                String mode = s.quizModeLabel();
                String modeBit =
                        mode.isEmpty()
                                ? "Saved from Find Your Genre."
                                : "Saved from your " + mode + " quiz.";
                homeVibeSubLabel.setText(
                        modeBit
                                + " This nudges picks, search, and autoplay. Update from Account \u2192 Profile or Genre Quiz.");
                homeVibeSubLabel.setVisible(true);
                homeVibeSubLabel.setManaged(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            homeVibeLabel.getStyleClass().setAll("caption");
            homeVibeLabel.setText("Couldn\u2019t load your genre profile. Your music still works as usual.");
            homeVibeLabel.setVisible(true);
            homeVibeLabel.setManaged(true);
            if (homeVibeSubLabel != null) {
                homeVibeSubLabel.setText("");
                homeVibeSubLabel.setVisible(false);
                homeVibeSubLabel.setManaged(false);
            }
        }
    }

    private static int countUniqueSavedSongs(UserProfile profile) {
        if (profile == null || profile.getPlaylists() == null) {
            return 0;
        }
        Set<Integer> seen = new HashSet<>();
        for (ObservableList<Song> list : profile.getPlaylists().values()) {
            if (list == null) {
                continue;
            }
            for (Song s : list) {
                if (s != null) {
                    seen.add(s.songId());
                }
            }
        }
        return seen.size();
    }

    /** Five playlist columns; tile width tracks the home column width. */
    private void bindPlaylistTileSizingOnce() {
        if (menuContent == null || playlistChipsContainer == null) {
            return;
        }
        if (playlistChipsContainer.prefTileWidthProperty().isBound()) {
            return;
        }
        final double hgap = playlistChipsContainer.getHgap();
        playlistChipsContainer.setPrefColumns(5);
        playlistChipsContainer.prefTileWidthProperty()
                .bind(
                        Bindings.createDoubleBinding(
                                () -> {
                                    double w = menuContent.getWidth() - 48;
                                    if (w <= 0) {
                                        return 200.0;
                                    }
                                    double cols = 5;
                                    double tile = (w - (cols - 1) * hgap) / cols;
                                    return Math.min(420, Math.max(168, tile));
                                },
                                menuContent.widthProperty()));
        playlistChipsContainer.setPrefTileHeight(76);
    }

    private void buildPlaylistChips(UserProfile profile) {
        playlistChipsContainer.getChildren().clear();
        if (profile == null || profile.getPlaylists() == null || profile.getPlaylists().isEmpty()) {
            noPlaylistsHint.setVisible(true);
            noPlaylistsHint.setManaged(true);
            return;
        }
        noPlaylistsHint.setVisible(false);
        noPlaylistsHint.setManaged(false);

        List<String> names = new ArrayList<>(profile.getPlaylists().keySet());
        PlaylistNames.sortForDisplay(names);

        final int max = 16;
        for (int i = 0; i < Math.min(names.size(), max); i++) {
            String name = names.get(i);
            playlistChipsContainer.getChildren().add(createPlaylistTile(name));
        }
        if (names.size() > max) {
            int more = names.size() - max;
            playlistChipsContainer.getChildren().add(createMorePlaylistsTile(more));
        }
    }

    private HBox createPlaylistTile(String playlistName) {
        Label glyph = new Label(PlaylistNames.glyphForPlaylist(playlistName));
        glyph.getStyleClass().add("home-playlist-tile-glyph");
        StackPane art = new StackPane(glyph);
        art.setMinSize(56, 56);
        art.setPrefSize(56, 56);
        art.setMaxSize(56, 56);
        art.getStyleClass().add("home-playlist-tile-art");

        Label title = new Label(playlistName);
        title.getStyleClass().add("home-playlist-tile-title");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);

        HBox row = new HBox(12, art, title);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("home-playlist-tile");
        row.setPadding(new Insets(10, 14, 10, 12));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setOnMouseClicked(
                ev -> {
                    if (ev.getButton() == MouseButton.PRIMARY) {
                        goToPlaylist(playlistName);
                        ev.consume();
                    }
                });
        row.setOnContextMenuRequested(
                ev -> {
                    UserProfile p = SessionManager.getCurrentUserProfile();
                    if (p == null || row.getScene() == null) {
                        return;
                    }
                    ContextMenu menu =
                            PlaylistLibraryContextMenu.create(
                                    row.getScene(),
                                    p,
                                    playlistService,
                                    playlistName,
                                    this::refreshHomeLibraryStrip);
                    menu.show(row, ev.getScreenX(), ev.getScreenY());
                    ev.consume();
                });
        return row;
    }

    private HBox createMorePlaylistsTile(int moreCount) {
        Label glyph = new Label("+" + moreCount);
        glyph.getStyleClass().addAll("home-playlist-tile-glyph", "home-playlist-tile-glyph-more");
        StackPane art = new StackPane(glyph);
        art.setMinSize(56, 56);
        art.setPrefSize(56, 56);
        art.setMaxSize(56, 56);
        art.getStyleClass().addAll("home-playlist-tile-art", "home-playlist-tile-art-more");

        Label title = new Label("See all playlists");
        title.getStyleClass().add("home-playlist-tile-title");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(title, Priority.ALWAYS);

        HBox row = new HBox(12, art, title);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().addAll("home-playlist-tile", "home-playlist-tile-more");
        row.setPadding(new Insets(10, 14, 10, 12));
        row.setMaxWidth(Double.MAX_VALUE);
        row.setOnMouseClicked(
                ev -> {
                    if (ev.getButton() == MouseButton.PRIMARY) {
                        try {
                            openPlaylistsPageFromHome();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ev.consume();
                    }
                });
        return row;
    }

    private void goToPlaylist(String playlistName) {
        SessionManager.requestPlaylistToOpen(playlistName);
        try {
            SceneUtil.switchScene(menuContent, FxmlResources.PLAYLISTS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openPlaylistsPageFromHome() throws IOException {
        SceneUtil.switchScene(menuContent, FxmlResources.PLAYLISTS);
    }

    private void loadFeedLists(String username) {
        ObservableList<Song> forYou = FXCollections.observableArrayList();
        if (username != null && !username.isBlank()) {
            forYou.setAll(recommendationService.getSuggestedSongsForUser(username, 5));
        }
        forYouListView.setItems(forYou);

        List<String> topArtists = List.of();
        if (username != null && !username.isBlank()) {
            try {
                topArtists = listeningEventDAO.findTopArtistNamesByListening(username, 5);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        freshTracksListView.setItems(FXCollections.observableArrayList(topArtists));
    }

    private void setupHomeListView(ListView<Song> listView) {
        listView.setFixedCellSize(58);
        Label ph = new Label("Nothing here yet.");
        ph.setStyle("-fx-text-fill: " + CellStyleKit.getTextMuted() + "; -fx-font-size: 13px;");
        listView.setPlaceholder(ph);

        listView.setCellFactory(
                lv ->
                        new ListCell<>() {
                            @Override
                            protected void updateItem(Song song, boolean empty) {
                                super.updateItem(song, empty);
                                if (empty || song == null) {
                                    setText(null);
                                    setGraphic(null);
                                    return;
                                }

                                Label glyph = new Label("\u266A");
                                glyph.setStyle(homeFeedNoteGlyphStyle());
                                StackPane icon = new StackPane(glyph);
                                icon.setPrefSize(36, 36);
                                icon.setMinSize(36, 36);
                                icon.setMaxSize(36, 36);
                                icon.setStyle(homeFeedNoteIconBoxStyle());

                                Song curSong = player.getCurrentSong();
                                boolean current = curSong != null && curSong.songId() == song.songId();

                                Region nowPlayingBar = CellStyleKit.nowPlayingEdgeBar();
                                nowPlayingBar.setVisible(current);
                                nowPlayingBar.setManaged(current);

                                VBox text =
                                        CellStyleKit.songTextBox(
                                                song.title(),
                                                song.artist(),
                                                null,
                                                MainMenuController.this::openArtistProfile);
                                if (current && !text.getChildren().isEmpty()) {
                                    Node head = text.getChildren().get(0);
                                    if (head instanceof Label lab) {
                                        lab.setStyle(
                                                "-fx-font-size: 14px; -fx-font-weight: bold;-fx-text-fill: "
                                                        + CellStyleKit.getAccentTitle()
                                                        + ";");
                                    }
                                }

                                Region sp = new Region();
                                HBox.setHgrow(sp, Priority.ALWAYS);

                                Button play = new Button("\u25B6");
                                play.setStyle(homeFeedPlayButtonStyle());
                                play.setPrefSize(34, 34);
                                play.setFocusTraversable(false);
                                play.setText(current && player.isPlaying() ? "⏸" : "▶");
                                play.setOnAction(e -> player.playSingleSong(song));

                                HBox row = new HBox(12, nowPlayingBar, icon, text, sp, play);
                                row.setAlignment(Pos.CENTER_LEFT);
                                row.setPadding(new Insets(6, 10, 6, 10));
                                CellStyleKit.markPlaying(row, current);

                                row.setOnMouseClicked(
                                        ev -> {
                                            if (ev.getButton() == MouseButton.PRIMARY
                                                    && ev.getClickCount() == 2) {
                                                player.playSingleSong(song);
                                                ev.consume();
                                            }
                                        });

                                row.addEventFilter(
                                        ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                                        ev -> {
                                            Song s = getItem();
                                            if (s == null || isEmpty()) {
                                                return;
                                            }
                                            ContextMenu cm =
                                                    SongContextMenuBuilder.build(
                                                            s,
                                                            row,
                                                            SongContextMenuBuilder.Spec.general());
                                            cm.show(row, ev.getScreenX(), ev.getScreenY());
                                            ev.consume();
                                        });

                                setText(null);
                                setGraphic(row);
                            }
                        });
    }

    private void setupTopArtistsListView(ListView<String> listView) {
        listView.setFixedCellSize(58);
        Label ph = new Label("Listen to music to see your top artists here.");
        ph.setStyle("-fx-text-fill: " + CellStyleKit.getTextMuted() + "; -fx-font-size: 13px;");
        listView.setPlaceholder(ph);

        listView.setCellFactory(
                lv ->
                        new ListCell<>() {
                            @Override
                            protected void updateItem(String artist, boolean empty) {
                                super.updateItem(artist, empty);
                                if (empty || artist == null || artist.isBlank()) {
                                    setText(null);
                                    setGraphic(null);
                                    return;
                                }

                                String initial =
                                        artist.isEmpty() ? "?" : artist.substring(0, 1).toUpperCase();
                                StackPane icon =
                                        CellStyleKit.iconBox(
                                                initial, CellStyleKit.Palette.ROSE, true);
                                VBox text = CellStyleKit.textBox(artist, "Artist");
                                Region sp = new Region();
                                HBox.setHgrow(sp, Priority.ALWAYS);

                                HBox row = new HBox(12, icon, text, sp);
                                row.setAlignment(Pos.CENTER_LEFT);
                                row.setPadding(new Insets(6, 10, 6, 10));
                                row.setStyle(CellStyleKit.getRowDefault());
                                CellStyleKit.addHover(row);

                                row.setOnMouseClicked(
                                        ev -> {
                                            if (ev.getButton() == MouseButton.PRIMARY) {
                                                openArtistProfile(artist);
                                                ev.consume();
                                            }
                                        });

                                setText(null);
                                setGraphic(row);
                            }
                        });
    }

    private void openArtistProfile(String artist) {
        if (artist == null || artist.isBlank()) {
            return;
        }
        SessionManager.setSelectedArtist(artist.trim());
        try {
            Node n = forYouListView != null ? forYouListView : menuContent;
            SceneUtil.switchScene(n, FxmlResources.ARTIST_PROFILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void applyResponsiveDensity(double width) {
        boolean compact = width < 1400;
        menuContent.setSpacing(compact ? 18 : 26);
        if (homeFeedRow != null) {
            homeFeedRow.setSpacing(width < 1100 ? 16 : 20);
        }
    }

    private static String homeFeedNoteGlyphStyle() {
        String fill = AppTheme.isLightMode() ? "#5b21b6" : "#a78bfa";
        return "-fx-font-size: 15px; -fx-text-fill: " + fill + "; -fx-font-weight: bold;";
    }

    private static String homeFeedNoteIconBoxStyle() {
        if (AppTheme.isLightMode()) {
            return "-fx-background-color: rgba(124,58,237,0.14);"
                    + "-fx-background-radius: 10;"
                    + "-fx-border-color: rgba(124,58,237,0.32);"
                    + "-fx-border-radius: 10; -fx-border-width: 1;";
        }
        return "-fx-background-color: rgba(139,92,246,0.14);"
                + "-fx-background-radius: 10;"
                + "-fx-border-color: rgba(139,92,246,0.22);"
                + "-fx-border-radius: 10; -fx-border-width: 1;";
    }

    private static String homeFeedPlayButtonStyle() {
        String fill = AppTheme.isLightMode() ? "#5b21b6" : "#7c3aed";
        return "-fx-background-color: transparent; -fx-text-fill: "
                + fill
                + "; -fx-font-size: 12px; -fx-font-weight: bold;";
    }

}

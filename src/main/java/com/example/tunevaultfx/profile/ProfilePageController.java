package com.example.tunevaultfx.profile;

import com.example.tunevaultfx.db.ListeningEventDAO;
import com.example.tunevaultfx.db.UserDAO;
import com.example.tunevaultfx.db.UserGenreDiscoveryDAO;
import com.example.tunevaultfx.db.UserGenreDiscoverySummary;
import com.example.tunevaultfx.profile.media.ProfileAvatarCropDialog;
import com.example.tunevaultfx.profile.media.ProfileMediaStorage;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.user.User;
import com.example.tunevaultfx.util.AppTheme;
import com.example.tunevaultfx.user.UserProfile;
import com.example.tunevaultfx.util.ToastUtil;
import com.example.tunevaultfx.util.UiMotionUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

/**
 * Account profile: identity, avatar, library metrics, taste, listening aggregates.
 */
public class ProfilePageController {

    @FXML private VBox profilePageRoot;
    @FXML private ImageView profileAvatarImage;
    @FXML private Label profileAvatarInitial;
    @FXML private Label profileDisplayName;
    @FXML private Label profileHandleLabel;
    @FXML private Label profileEmailLabel;
    @FXML private Label profileAccountIdLabel;
    @FXML private Label statPlaylistsValue;
    @FXML private Label statSavedValue;
    @FXML private Label statListeningValue;
    @FXML private Label statListeningHint;
    @FXML private FlowPane genreChipsFlow;
    @FXML private Label genreSummaryLabel;
    @FXML private VBox clearGenreQuizBlock;
    @FXML private Button clearGenreQuizButton;
    @FXML private Label listeningSummaryLabel;
    @FXML private Button changeAvatarButton;
    @FXML private Button removeAvatarButton;

    private final UserDAO userDAO = new UserDAO();
    private final UserGenreDiscoveryDAO genreDiscoveryDAO = new UserGenreDiscoveryDAO();
    private final ListeningEventDAO listeningEventDAO = new ListeningEventDAO();
    private final ProfileMediaPersistence mediaPersistence = new ProfileMediaPersistence(userDAO);

    private String sessionUsername;
    private User loadedUser;

    @FXML
    public void initialize() {
        setupAvatarClip();

        sessionUsername = SessionManager.getCurrentUsername();
        if (sessionUsername == null || sessionUsername.isBlank()) {
            applySignedOutPlaceholder();
            return;
        }
        if (clearGenreQuizBlock != null) {
            clearGenreQuizBlock.setVisible(true);
            clearGenreQuizBlock.setManaged(true);
        }

        loadIdentity(sessionUsername);
        loadLibraryMetrics();
        loadListeningBlock(sessionUsername);
        loadTasteSection(sessionUsername);

        changeAvatarButton.setOnAction(e -> pickAndApplyAvatar());
        removeAvatarButton.setOnAction(e -> clearAvatar());

        Platform.runLater(
                () -> {
                    if (profilePageRoot != null) {
                        UiMotionUtil.playStaggeredEntrance(
                                profilePageRoot.getChildren().stream().toList());
                    }
                });
    }

    private void setupAvatarClip() {
        profileAvatarImage.setClip(new Circle(48, 48, 48));
    }

    private void applySignedOutPlaceholder() {
        profileAvatarImage.setImage(null);
        profileAvatarInitial.setText("?");
        profileAvatarInitial.setVisible(true);
        profileDisplayName.setText("Not signed in");
        profileHandleLabel.setText("");
        profileEmailLabel.setText("Sign in to view your TuneVault profile.");
        profileAccountIdLabel.setText("");
        statPlaylistsValue.setText("\u2014");
        statSavedValue.setText("\u2014");
        statListeningValue.setText("\u2014");
        statListeningHint.setText("");
        genreChipsFlow.getChildren().clear();
        Label ph = new Label("Unavailable");
        ph.getStyleClass().add("profile-genre-chip-muted");
        genreChipsFlow.getChildren().add(ph);
        genreSummaryLabel.setText("");
        listeningSummaryLabel.setText("Listening history is tied to your account.");
        changeAvatarButton.setDisable(true);
        removeAvatarButton.setDisable(true);
        if (clearGenreQuizBlock != null) {
            clearGenreQuizBlock.setVisible(false);
            clearGenreQuizBlock.setManaged(false);
        }
    }

    private void loadIdentity(String username) {
        profileDisplayName.setText(username);
        profileHandleLabel.setText("@" + username.replaceAll("\\s+", "").toLowerCase(Locale.ROOT));
        profileAvatarInitial.setText(ProfileTextFormat.initialsFor(username));

        try {
            Optional<User> row = userDAO.findByUsername(username);
            if (row.isPresent()) {
                loadedUser = row.get();
                User u = loadedUser;
                profileEmailLabel.setText(u.getEmail() != null ? u.getEmail() : "\u2014");
                profileAccountIdLabel.setText(
                        "Account ID \u00B7 " + u.getUserId() + "  \u00B7  Sign-in email is not public.");
                applyAvatarImage(u.getProfileAvatarKey());
                changeAvatarButton.setDisable(false);
                removeAvatarButton.setDisable(u.getProfileAvatarKey() == null || u.getProfileAvatarKey().isBlank());
            } else {
                loadedUser = null;
                profileEmailLabel.setText("\u2014");
                profileAccountIdLabel.setText("");
                applyAvatarImage(null);
                changeAvatarButton.setDisable(true);
                removeAvatarButton.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            loadedUser = null;
            profileEmailLabel.setText("Could not load account (database may need migration).");
            profileAccountIdLabel.setText("");
            applyAvatarImage(null);
            changeAvatarButton.setDisable(true);
            removeAvatarButton.setDisable(true);
        }
    }

    private void applyAvatarImage(String relativeKey) {
        Path p = ProfileMediaStorage.resolveFile(relativeKey);
        if (p != null && Files.isRegularFile(p)) {
            Image img = new Image(p.toUri().toString(), false);
            if (!img.isError()) {
                profileAvatarImage.setImage(img);
                profileAvatarInitial.setVisible(false);
                return;
            }
        }
        profileAvatarImage.setImage(null);
        profileAvatarInitial.setVisible(true);
    }

    private void pickAndApplyAvatar() {
        if (loadedUser == null) {
            return;
        }
        File f = showOpenDialog("Choose profile photo");
        if (f == null) {
            return;
        }
        Stage owner = ownerStage();
        if (owner == null) {
            return;
        }
        Optional<Path> cropped;
        try {
            cropped = ProfileAvatarCropDialog.showAndExport(owner, f.toPath());
        } catch (IOException ex) {
            ex.printStackTrace();
            ToastUtil.error(scene(), ex.getMessage() != null ? ex.getMessage() : "Could not open image.");
            return;
        }
        if (cropped.isEmpty()) {
            return;
        }
        try {
            mediaPersistence.saveAvatarFromCroppedTemp(loadedUser, cropped.get());
            reloadUserAndRefreshAvatar();
            ToastUtil.success(scene(), "Profile photo updated");
        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
            ToastUtil.error(scene(), ex.getMessage() != null ? ex.getMessage() : "Could not save photo.");
        }
    }

    private void clearAvatar() {
        if (loadedUser == null) {
            return;
        }
        try {
            mediaPersistence.clearAvatar(loadedUser);
            reloadUserAndRefreshAvatar();
            ToastUtil.info(scene(), "Profile photo removed");
        } catch (IOException | SQLException ex) {
            ex.printStackTrace();
            ToastUtil.error(scene(), "Could not remove photo.");
        }
    }

    private void reloadUserAndRefreshAvatar() throws SQLException {
        Optional<User> row = userDAO.findByUsername(sessionUsername);
        loadedUser = row.orElse(null);
        if (loadedUser != null) {
            applyAvatarImage(loadedUser.getProfileAvatarKey());
            removeAvatarButton.setDisable(
                    loadedUser.getProfileAvatarKey() == null || loadedUser.getProfileAvatarKey().isBlank());
        }
    }

    private File showOpenDialog(String title) {
        Stage stage = ownerStage();
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp"));
        return stage != null ? fc.showOpenDialog(stage) : null;
    }

    private Stage ownerStage() {
        return profilePageRoot.getScene() != null ? (Stage) profilePageRoot.getScene().getWindow() : null;
    }

    private javafx.scene.Scene scene() {
        return profilePageRoot != null ? profilePageRoot.getScene() : null;
    }

    private void loadLibraryMetrics() {
        UserProfile profile = SessionManager.getCurrentUserProfile();
        int playlistCount =
                profile != null && profile.getPlaylists() != null ? profile.getPlaylists().size() : 0;
        statPlaylistsValue.setText(String.valueOf(playlistCount));
        statSavedValue.setText(String.valueOf(ProfileLibraryStats.countUniqueSavedSongs(profile)));
    }

    private void loadListeningBlock(String username) {
        try {
            Optional<ListeningEventDAO.ListeningProfileStats> opt =
                    listeningEventDAO.loadListeningProfileStats(username);
            if (opt.isEmpty()) {
                statListeningValue.setText("0");
                statListeningHint.setText("Counted plays");
                listeningSummaryLabel.setText(
                        "No listening data yet. Play a few tracks — stats will show up after sessions complete.");
                return;
            }
            ListeningEventDAO.ListeningProfileStats s = opt.get();
            statListeningValue.setText(ProfileTextFormat.formatNumber(s.countedPlays()));
            statListeningHint.setText("Counted plays");
            listeningSummaryLabel.setText(
                    "You have "
                            + ProfileTextFormat.formatNumber(s.countedPlays())
                            + " counted play(s) and about "
                            + ProfileTextFormat.formatListeningDuration(s.listenedSeconds())
                            + " of tracked listening time in TuneVault.");
        } catch (SQLException e) {
            e.printStackTrace();
            statListeningValue.setText("\u2014");
            statListeningHint.setText("");
            listeningSummaryLabel.setText("Listening stats could not be loaded.");
        }
    }

    private void loadTasteSection(String username) {
        genreChipsFlow.getChildren().clear();
        try {
            Optional<UserGenreDiscoverySummary> opt = genreDiscoveryDAO.loadSummary(username);
            if (opt.isEmpty()) {
                Label chip = new Label("Not set");
                chip.getStyleClass().add("profile-genre-chip-muted");
                genreChipsFlow.getChildren().add(chip);
                genreSummaryLabel.setText(
                        "You haven\u2019t saved a genre profile yet. Open Find Your Genre in the sidebar when you "
                                + "want recommendations and search to lean into a style.");
                return;
            }
            UserGenreDiscoverySummary s = opt.get();
            for (String part : s.blendParts()) {
                Label chip = new Label(part);
                chip.getStyleClass().add("profile-genre-chip");
                genreChipsFlow.getChildren().add(chip);
            }
            String mode = s.quizModeLabel();
            String modePhrase =
                    mode.isEmpty()
                            ? "Last saved from Find Your Genre."
                            : "Last saved from your " + mode + " quiz.";
            genreSummaryLabel.setText(
                    modePhrase
                            + " Retake Find Your Genre from the sidebar anytime; your library and history stay as they are.");
        } catch (SQLException e) {
            e.printStackTrace();
            genreChipsFlow.getChildren().clear();
            Label err = new Label("Could not load");
            err.getStyleClass().add("profile-genre-chip-muted");
            genreChipsFlow.getChildren().add(err);
            genreSummaryLabel.setText(ProfileGenreMessages.loadFailureHint(e));
        } finally {
            updateClearGenreQuizButton(username);
        }
    }

    @FXML
    private void handleClearGenreQuiz() {
        if (sessionUsername == null || sessionUsername.isBlank()) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear genre quiz?");
        confirm.setHeaderText("Remove Find Your Genre from recommendations?");
        confirm.setContentText(
                "This deletes only your saved quiz blend in the database. Listening history, playlists, "
                        + "and play counts are not changed. You can retake the quiz anytime.");
        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) {
            return;
        }
        try {
            boolean removed = genreDiscoveryDAO.deleteForUser(sessionUsername);
            if (!removed) {
                ToastUtil.info(scene(), "No saved genre quiz to clear.");
            } else {
                ToastUtil.success(scene(), "Genre quiz cleared. Recommendations now use your listening only.");
            }
            loadTasteSection(sessionUsername);
            if (profilePageRoot != null) {
                AppTheme.refreshAllListViews(profilePageRoot);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ToastUtil.error(scene(), "Could not clear genre quiz. Check your database connection.");
        }
    }

    private void updateClearGenreQuizButton(String username) {
        if (clearGenreQuizButton == null) {
            return;
        }
        if (username == null || username.isBlank()) {
            clearGenreQuizButton.setDisable(true);
            return;
        }
        try {
            clearGenreQuizButton.setDisable(!genreDiscoveryDAO.hasSavedProfile(username));
        } catch (SQLException e) {
            clearGenreQuizButton.setDisable(true);
        }
    }
}

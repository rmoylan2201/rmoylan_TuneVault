package com.example.tunevaultfx.findyourgenre;

import com.example.tunevaultfx.core.Song;
import com.example.tunevaultfx.db.SongDAO;
import com.example.tunevaultfx.db.UserGenreDiscoveryDAO;
import com.example.tunevaultfx.musicplayer.controller.MusicPlayerController;
import com.example.tunevaultfx.session.SessionManager;
import com.example.tunevaultfx.util.ToastUtil;
import com.example.tunevaultfx.util.UiMotionUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Find Your Genre — fast, low-pressure quiz with Quick/Full modes, skip, keyboard shortcuts,
 * DB persistence, and recommendation integration via {@link UserGenreDiscoveryDAO}.
 */
public class FindYourGenrePageController {

    private static final String RESULTS_CARD_STYLE = "genre-quiz-card--results";

    private static final String SUBTITLE_DEFAULT =
            "Choose Quick or Full, then Start quiz. Quick taps, no timer, no wrong answers — you can redo anytime. Keys 1–4 pick answers while the quiz runs.";
    private static final String SUBTITLE_RESULTS =
            "Here’s what we heard from your picks. Play a mix to hear it in your library, or run the quiz again — nothing is permanent.";

    @FXML private Label pageSubtitleLabel;
    @FXML private Label progressLabel;
    @FXML private Label questionLabel;
    @FXML private ProgressBar quizProgressBar;

    @FXML private Button answerButton1;
    @FXML private Button answerButton2;
    @FXML private Button answerButton3;
    @FXML private Button answerButton4;
    @FXML private Button skipQuestionButton;
    @FXML private Button restartButton;
    @FXML private Button playMixButton;
    @FXML private Button startQuizButton;

    @FXML private VBox quizContainer;
    @FXML private VBox quizCard;
    @FXML private VBox lengthPanel;
    @FXML private VBox quizActiveSection;
    @FXML private VBox quizInteractiveSection;
    @FXML private VBox resultSection;
    @FXML private Label resultsEyebrowLabel;
    @FXML private Label resultHeadlineLabel;
    @FXML private Label resultSummaryLabel;
    @FXML private Label savedHintLabel;
    @FXML private FlowPane resultChips;
    @FXML private HBox resultActionsRow;

    @FXML private RadioButton modeQuickRadio;
    @FXML private RadioButton modeFullRadio;

    private final ToggleGroup modeGroup = new ToggleGroup();

    private final UserGenreDiscoveryDAO genreDiscoveryDAO = new UserGenreDiscoveryDAO();
    private final SongDAO songDAO = new SongDAO();
    private final MusicPlayerController player = MusicPlayerController.getInstance();

    private List<GenreQuiz.Question> questions = List.of();
    private final Map<String, Integer> scores = new LinkedHashMap<>();
    private int currentIndex = 0;
    private boolean quizComplete = false;
    /** True before the user confirms length with Start quiz (and after Try again). */
    private boolean awaitingStart = true;

    @FXML
    public void initialize() {
        modeQuickRadio.setToggleGroup(modeGroup);
        modeFullRadio.setToggleGroup(modeGroup);

        enterPickLengthState();

        modeGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                return;
            }
            if (awaitingStart) {
                syncPickLengthProgressHint();
            }
        });

        Platform.runLater(() -> {
            UiMotionUtil.playStaggeredEntrance(List.of(quizCard, lengthPanel));
            UiMotionUtil.applyHoverLift(quizCard);
        });

        quizContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }
            applyResponsiveDensity(newScene.getWidth());
            newScene.widthProperty().addListener((o, oldW, newW) ->
                    applyResponsiveDensity(newW.doubleValue()));

            if (newScene.getProperties().containsKey("genreQuizKeysInstalled")) {
                return;
            }
            newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleSceneKey);
            newScene.getProperties().put("genreQuizKeysInstalled", true);
        });
    }

    private void handleSceneKey(KeyEvent event) {
        if (awaitingStart || quizComplete || questions.isEmpty()) {
            return;
        }
        KeyCode c = event.getCode();
        int idx = switch (c) {
            case DIGIT1, NUMPAD1 -> 0;
            case DIGIT2, NUMPAD2 -> 1;
            case DIGIT3, NUMPAD3 -> 2;
            case DIGIT4, NUMPAD4 -> 3;
            default -> -1;
        };
        if (idx >= 0) {
            chooseAnswer(idx);
            event.consume();
        }
    }

    private void applyResponsiveDensity(double width) {
        boolean compact = width < 1300;
        quizCard.setPrefWidth(compact ? 640 : 760);
    }

    @FXML private void handleAnswer1() { chooseAnswer(0); }
    @FXML private void handleAnswer2() { chooseAnswer(1); }
    @FXML private void handleAnswer3() { chooseAnswer(2); }
    @FXML private void handleAnswer4() { chooseAnswer(3); }

    @FXML private void handleSkipQuestion() {
        if (awaitingStart || quizComplete || questions.isEmpty()) {
            return;
        }
        currentIndex++;
        if (currentIndex >= questions.size()) {
            showResult();
        } else {
            loadQuestion();
        }
    }

    @FXML private void handleRestartQuiz() {
        enterPickLengthState();
    }

    @FXML
    private void handleStartQuiz() {
        if (!awaitingStart) {
            return;
        }
        questions = GenreQuiz.questionsFor(currentMode());
        if (questions.isEmpty()) {
            questionLabel.setText("No questions loaded for this mode.");
            return;
        }
        awaitingStart = false;
        setStartQuizButtonVisible(false);
        setLengthPanelVisible(false);
        setQuizInteractiveVisible(true);
        setModeSelectionEnabled(false);
        currentIndex = 0;
        if (pageSubtitleLabel != null) {
            pageSubtitleLabel.setText(SUBTITLE_DEFAULT);
        }
        loadQuestion();
    }

    @FXML private void handlePlayMix() {
        if (scores.isEmpty()) {
            return;
        }
        List<String> keys = GenreQuiz.topGenreKeys(scores, 5);
        if (keys.isEmpty()) {
            return;
        }

        ObservableList<Song> lib;
        try {
            if (SessionManager.isSongLibraryReady()) {
                lib = SessionManager.getSongLibrary();
            } else {
                lib = songDAO.getAllSongs();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ToastUtil.error(quizCard.getScene(), "Could not load songs for your mix.");
            return;
        }

        List<Song> mix = lib.stream()
                .filter(s -> s != null && genreMatchesAny(s, keys))
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(mix);
        if (mix.isEmpty()) {
            mix = new ArrayList<>(lib);
            Collections.shuffle(mix);
        }
        mix = mix.stream().limit(40).collect(Collectors.toList());
        if (mix.isEmpty()) {
            ToastUtil.info(quizCard.getScene(), "No songs in your library yet — add tracks to hear a mix.");
            return;
        }
        player.playQueue(FXCollections.observableArrayList(mix), 0, "Genre mix");
        ToastUtil.success(quizCard.getScene(), "Playing your genre mix");
    }

    private static boolean genreMatchesAny(Song s, List<String> normalizedKeys) {
        String g = normalizeGenre(s.genre());
        if (g.isEmpty()) {
            return false;
        }
        for (String k : normalizedKeys) {
            if (g.equals(k) || g.contains(k) || k.contains(g)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeGenre(String g) {
        return g == null ? "" : g.trim().toLowerCase();
    }

    private void chooseAnswer(int answerIndex) {
        if (awaitingStart || quizComplete || questions.isEmpty()) {
            return;
        }
        GenreQuiz.Question q = questions.get(currentIndex);
        scores.merge(q.genreFor(answerIndex), q.weightFor(answerIndex), Integer::sum);
        currentIndex++;
        if (currentIndex >= questions.size()) {
            showResult();
        } else {
            loadQuestion();
        }
    }

    private void loadQuestion() {
        quizComplete = false;
        setResultsCardStyle(false);
        setStartQuizButtonVisible(false);
        setQuizActiveVisible(true);
        setQuizInteractiveVisible(!awaitingStart);
        setLengthPanelVisible(awaitingStart);
        resultSection.setVisible(false);
        resultSection.setManaged(false);
        if (pageSubtitleLabel != null && !awaitingStart) {
            pageSubtitleLabel.setText(SUBTITLE_DEFAULT);
        }

        GenreQuiz.Question q = questions.get(currentIndex);
        int total = questions.size();
        progressLabel.setText("Question " + (currentIndex + 1) + " of " + total);
        quizProgressBar.setProgress(total == 0 ? 0 : (currentIndex + 1.0) / total);
        questionLabel.setText(q.prompt());
        answerButton1.setText(q.optionFor(0));
        answerButton2.setText(q.optionFor(1));
        answerButton3.setText(q.optionFor(2));
        answerButton4.setText(q.optionFor(3));
        setAnswerButtonsDisabled(false);
        skipQuestionButton.setDisable(false);
        restartButton.setDisable(false);
        playMixButton.setDisable(true);
        setModeSelectionEnabled(false);
    }

    private void showResult() {
        quizComplete = true;
        setStartQuizButtonVisible(false);
        setLengthPanelVisible(false);
        setQuizActiveVisible(false);
        setQuizInteractiveVisible(false);

        setResultsCardStyle(true);
        if (pageSubtitleLabel != null) {
            pageSubtitleLabel.setText(SUBTITLE_RESULTS);
        }

        List<String> top = GenreQuiz.topGenres(scores, 3);
        resultChips.getChildren().clear();

        if (resultsEyebrowLabel != null) {
            resultsEyebrowLabel.setText("YOUR GENRE PROFILE");
        }

        String headline;
        String summary;
        if (top.isEmpty()) {
            headline = "We need a clearer read";
            summary = "Try again and tap the answers that feel closest today — or skip less so we get a stronger signal.";
        } else if (top.size() == 1) {
            headline = top.get(0);
            summary = "We’ll steer playlists, suggestions, and search toward this world. Change your mind anytime with Try again.";
        } else {
            headline = "Your blend";
            summary = "Recommendations will mix these together. Nothing is locked — redo the quiz whenever your taste shifts.";
            for (String g : top) {
                Label chip = new Label(g);
                chip.getStyleClass().addAll("genre-result-chip", "genre-result-chip-hero");
                resultChips.getChildren().add(chip);
            }
        }
        if (resultChips != null) {
            boolean showChips = top.size() > 1;
            resultChips.setVisible(showChips);
            resultChips.setManaged(showChips);
        }
        resultHeadlineLabel.setText(headline);
        resultSummaryLabel.setText(summary);

        playMixButton.setDisable(top.isEmpty());
        restartButton.setDisable(false);
        setModeSelectionEnabled(true);

        resultSection.setVisible(true);
        resultSection.setManaged(true);

        persistDiscovery(top);

        Platform.runLater(() -> {
            List<Node> entrance = new ArrayList<>(List.of(
                    resultsEyebrowLabel,
                    resultHeadlineLabel,
                    resultSummaryLabel));
            if (top.size() > 1 && resultChips != null) {
                entrance.add(resultChips);
            }
            entrance.add(savedHintLabel);
            entrance.add(resultActionsRow);
            UiMotionUtil.playStaggeredEntrance(entrance);
        });
    }

    private void persistDiscovery(List<String> topDisplay) {
        String user = SessionManager.getCurrentUsername();
        if (user == null || user.isBlank()) {
            savedHintLabel.setText("Sign in to save your mix to your account.");
            return;
        }

        GenreQuiz.QuizMode mode = currentMode();
        Map<String, Double> boosts = GenreQuiz.toRecommendationBoosts(scores);
        if (boosts.isEmpty()) {
            savedHintLabel.setText("");
            return;
        }

        String one;
        if (!topDisplay.isEmpty()) {
            one = topDisplay.get(0);
        } else {
            one = GenreQuiz.formatGenreTitle(
                    Collections.max(scores.entrySet(), Map.Entry.comparingByValue()).getKey());
        }
        String two = topDisplay.size() > 1 ? topDisplay.get(1) : null;
        String three = topDisplay.size() > 2 ? topDisplay.get(2) : null;

        try {
            genreDiscoveryDAO.save(user, mode.name(), one, two, three, boosts);
            savedHintLabel.setText("Saved to your profile — playlists, autoplay, and search now blend this in.");
        } catch (SQLException e) {
            e.printStackTrace();
            savedHintLabel.setText("Could not save to the database. Your mix still plays locally.");
            ToastUtil.error(quizCard.getScene(), "Could not save genre profile. Check your database connection.");
        }
    }

    private GenreQuiz.QuizMode currentMode() {
        return modeFullRadio.isSelected() ? GenreQuiz.QuizMode.FULL : GenreQuiz.QuizMode.QUICK;
    }

    private void enterPickLengthState() {
        scores.clear();
        currentIndex = 0;
        quizComplete = false;
        awaitingStart = true;
        questions = List.of();

        setResultsCardStyle(false);
        setLengthPanelVisible(true);
        setQuizActiveVisible(true);
        setQuizInteractiveVisible(false);
        resultSection.setVisible(false);
        resultSection.setManaged(false);
        resultSummaryLabel.setText("");
        resultHeadlineLabel.setText("");
        savedHintLabel.setText("");
        resultChips.getChildren().clear();
        if (resultChips != null) {
            resultChips.setVisible(true);
            resultChips.setManaged(true);
        }

        if (pageSubtitleLabel != null) {
            pageSubtitleLabel.setText(SUBTITLE_DEFAULT);
        }
        if (resultsEyebrowLabel != null) {
            resultsEyebrowLabel.setText("YOUR GENRE PROFILE");
        }

        quizProgressBar.setProgress(0);
        questionLabel.setText("Pick a length above, then tap Start quiz.");
        answerButton1.setText("");
        answerButton2.setText("");
        answerButton3.setText("");
        answerButton4.setText("");
        setAnswerButtonsDisabled(true);
        skipQuestionButton.setDisable(true);
        playMixButton.setDisable(true);

        setModeSelectionEnabled(true);
        setStartQuizButtonVisible(true);
        syncPickLengthProgressHint();
    }

    private void syncPickLengthProgressHint() {
        if (!awaitingStart) {
            return;
        }
        int n = GenreQuiz.questionCount(currentMode());
        boolean quick = currentMode() == GenreQuiz.QuizMode.QUICK;
        progressLabel.setText((quick ? "Quick" : "Full") + " — " + n + " question" + (n == 1 ? "" : "s"));
        quizProgressBar.setProgress(0);
    }

    private void setStartQuizButtonVisible(boolean visible) {
        if (startQuizButton == null) {
            return;
        }
        startQuizButton.setVisible(visible);
        startQuizButton.setManaged(visible);
    }

    private void setLengthPanelVisible(boolean visible) {
        if (lengthPanel == null) {
            return;
        }
        lengthPanel.setVisible(visible);
        lengthPanel.setManaged(visible);
    }

    private void setQuizActiveVisible(boolean visible) {
        if (quizActiveSection == null) {
            return;
        }
        quizActiveSection.setVisible(visible);
        quizActiveSection.setManaged(visible);
    }

    private void setQuizInteractiveVisible(boolean visible) {
        if (quizInteractiveSection == null) {
            return;
        }
        quizInteractiveSection.setVisible(visible);
        quizInteractiveSection.setManaged(visible);
    }

    private void setResultsCardStyle(boolean results) {
        if (quizCard == null) {
            return;
        }
        ObservableList<String> classes = quizCard.getStyleClass();
        if (results) {
            if (!classes.contains(RESULTS_CARD_STYLE)) {
                classes.add(RESULTS_CARD_STYLE);
            }
        } else {
            classes.remove(RESULTS_CARD_STYLE);
        }
    }

    private void setModeSelectionEnabled(boolean enabled) {
        modeQuickRadio.setDisable(!enabled);
        modeFullRadio.setDisable(!enabled);
    }

    private void setAnswerButtonsDisabled(boolean disabled) {
        answerButton1.setDisable(disabled);
        answerButton2.setDisable(disabled);
        answerButton3.setDisable(disabled);
        answerButton4.setDisable(disabled);
    }
}

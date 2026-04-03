package com.example.tunevaultfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindYourGenrePageController {

    @FXML private Label questionLabel;
    @FXML private Label progressLabel;
    @FXML private Label resultLabel;

    @FXML private Button answerButton1;
    @FXML private Button answerButton2;
    @FXML private Button answerButton3;
    @FXML private Button answerButton4;
    @FXML private Button restartButton;

    private final List<Question> questions = List.of(
            new Question("What kind of night sounds best to you?", "Big party", "Long drive", "Coffee shop", "Loud concert", "Pop", "Synthwave", "Jazz", "Rock"),
            new Question("Pick a music vibe:", "Catchy", "Dreamy", "Emotional", "Heavy", "Pop", "Indie", "R&B", "Rock"),
            new Question("What instrument grabs your attention most?", "Vocals", "Synths", "Saxophone", "Guitar", "Pop", "Synthwave", "Jazz", "Rock"),
            new Question("Which setting fits your taste most?", "Dance floor", "Late-night city", "Lounge", "Festival stage", "Pop", "Synthwave", "Jazz", "Rock")
    );

    private final Map<String, Integer> scores = new HashMap<>();
    private int currentQuestionIndex = 0;

    @FXML
    public void initialize() {
        resetQuiz();
    }

    @FXML private void handleAnswer1() { chooseAnswer(0); }
    @FXML private void handleAnswer2() { chooseAnswer(1); }
    @FXML private void handleAnswer3() { chooseAnswer(2); }
    @FXML private void handleAnswer4() { chooseAnswer(3); }
    @FXML private void handleRestartQuiz() { resetQuiz(); }

    @FXML
    private void handleBackToMenu(ActionEvent e) throws IOException {
        SceneUtil.switchScene((Node) e.getSource(), "main-menu.fxml");
    }

    private void chooseAnswer(int index) {
        Question q = questions.get(currentQuestionIndex);
        String genre = q.genreFor(index);
        scores.put(genre, scores.getOrDefault(genre, 0) + 1);
        currentQuestionIndex++;

        if (currentQuestionIndex >= questions.size()) {
            showResult();
        } else {
            loadQuestion();
        }
    }

    private void loadQuestion() {
        Question q = questions.get(currentQuestionIndex);
        progressLabel.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());
        questionLabel.setText(q.prompt());
        answerButton1.setText(q.option1());
        answerButton2.setText(q.option2());
        answerButton3.setText(q.option3());
        answerButton4.setText(q.option4());

        resultLabel.setText("");
        restartButton.setVisible(false);

        answerButton1.setDisable(false);
        answerButton2.setDisable(false);
        answerButton3.setDisable(false);
        answerButton4.setDisable(false);
    }

    private void showResult() {
        String bestGenre = "Pop";
        int bestScore = -1;

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestGenre = entry.getKey();
                bestScore = entry.getValue();
            }
        }

        progressLabel.setText("Quiz Complete");
        questionLabel.setText("Your best genre match is...");
        resultLabel.setText(bestGenre + " 🎵");

        answerButton1.setDisable(true);
        answerButton2.setDisable(true);
        answerButton3.setDisable(true);
        answerButton4.setDisable(true);
        restartButton.setVisible(true);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Genre Match");
        alert.setHeaderText("Quiz Result");
        alert.setContentText("Your genre is " + bestGenre + ".");
        alert.showAndWait();
    }

    private void resetQuiz() {
        scores.clear();
        currentQuestionIndex = 0;
        loadQuestion();
    }

    private record Question(
            String prompt,
            String option1,
            String option2,
            String option3,
            String option4,
            String genre1,
            String genre2,
            String genre3,
            String genre4
    ) {
        public String genreFor(int index) {
            return switch (index) {
                case 0 -> genre1;
                case 1 -> genre2;
                case 2 -> genre3;
                default -> genre4;
            };
        }
    }
}
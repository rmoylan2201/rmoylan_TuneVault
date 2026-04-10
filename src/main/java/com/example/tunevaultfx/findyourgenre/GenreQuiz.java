package com.example.tunevaultfx.findyourgenre;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the internal quiz logic for the genre quiz.
 * Stores questions, tracks answers, and calculates the final genre result.
 */
public class GenreQuiz {

    private final List<Question> questions = List.of(
            new Question(
                    "What kind of night sounds best to you?",
                    "Big party", "Long drive", "Coffee shop", "Loud concert",
                    "Pop", "Synthwave", "Jazz", "Rock"
            ),
            new Question(
                    "Pick a music vibe:",
                    "Catchy", "Dreamy", "Emotional", "Heavy",
                    "Pop", "Indie", "R&B", "Rock"
            ),
            new Question(
                    "What instrument grabs your attention most?",
                    "Vocals", "Synths", "Saxophone", "Guitar",
                    "Pop", "Synthwave", "Jazz", "Rock"
            ),
            new Question(
                    "Which setting fits your taste most?",
                    "Dance floor", "Late-night city", "Lounge", "Festival stage",
                    "Pop", "Synthwave", "Jazz", "Rock"
            )
    );

    private final Map<String, Integer> scores = new HashMap<>();
    private int currentQuestionIndex = 0;

    public void reset() {
        scores.clear();
        currentQuestionIndex = 0;
    }

    public Question getCurrentQuestion() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return null;
        }
        return questions.get(currentQuestionIndex);
    }

    public int getCurrentQuestionNumber() {
        return currentQuestionIndex + 1;
    }

    public int getTotalQuestions() {
        return questions.size();
    }

    public boolean isFinished() {
        return currentQuestionIndex >= questions.size();
    }

    public void answer(int index) {
        if (isFinished()) {
            return;
        }

        Question question = questions.get(currentQuestionIndex);
        String genre = question.genreFor(index);
        scores.put(genre, scores.getOrDefault(genre, 0) + 1);
        currentQuestionIndex++;
    }

    public String getBestGenre() {
        String bestGenre = "Pop";
        int bestScore = -1;

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestGenre = entry.getKey();
                bestScore = entry.getValue();
            }
        }

        return bestGenre;
    }
}
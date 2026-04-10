package com.example.tunevaultfx.findyourgenre;

/**
 * Represents one quiz question and its answer choices.
 * Also stores which genre each answer corresponds to.
 */
public class Question {

    private final String prompt;
    private final String option1;
    private final String option2;
    private final String option3;
    private final String option4;
    private final String genre1;
    private final String genre2;
    private final String genre3;
    private final String genre4;

    public Question(String prompt,
                    String option1,
                    String option2,
                    String option3,
                    String option4,
                    String genre1,
                    String genre2,
                    String genre3,
                    String genre4) {
        this.prompt = prompt;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.genre1 = genre1;
        this.genre2 = genre2;
        this.genre3 = genre3;
        this.genre4 = genre4;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }

    public String genreFor(int index) {
        return switch (index) {
            case 0 -> genre1;
            case 1 -> genre2;
            case 2 -> genre3;
            default -> genre4;
        };
    }
}
package com.axono.questionmodel;

import java.util.List;

public class Question {

    private final int correctAns;

    private final int selectedAns;
    private final String quess;

    private final List<String> answers;

    public Question(String quess,
            List<String> answers,
            int correctAns,
            int selectedAns) {

        // weak error handling for now
        if (quess.isBlank()) {
            this.quess = "**Error* in displaying question.Your results below are still valid:*";
        } else {
            this.quess = quess;
        }

        if (answers.isEmpty()) {
            this.answers = List
                    .of("*Unable to display selected/correct answers. Your overall result is indicated on the header* ");
        } else {
            this.answers = answers;
        }

        this.correctAns = correctAns;

        this.selectedAns = selectedAns;

    }

    public int getCorrectAns() {
        return correctAns;
    }

    public int getSelectedAns() {
        return selectedAns;
    }

    public String getQuess() {
        return quess;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public boolean isCorrect() {
        return selectedAns == correctAns;
    }

    public boolean choseCorrectAndCorrect(int ans) {

        return ans == correctAns && ans == selectedAns;
    }

    public boolean choseIncorrect(int ans) {
        return ans == selectedAns && ans != correctAns;

    }

    public boolean unchosenCorrect(int ans) {

        return ans == correctAns && ans != selectedAns;
    }

}

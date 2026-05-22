package com.axono.questionmodel;

import java.util.List;

/**
 * Immutable model representing a single quiz question: the prompt
 * text, the possible answers, the index of the correct answer, and
 * the index of the answer the user selected.
 */
public final class Question {

    /** Fallback prompt shown when the supplied question text is blank. */
    private static final String BLANK_QUESTION_FALLBACK = "**Error* in displaying question."
            + "Your results below are still valid:*";

    /** Fallback row shown when no answer options were supplied. */
    private static final String EMPTY_ANSWERS_FALLBACK = "*Unable to display selected/correct answers. "
            + "Your overall result is indicated on the header* ";

    /** Zero-based index of the correct answer in the answer list. */
    private final int correctAns;

    /** Zero-based index of the user's selected answer. */
    private final int selectedAns;

    /** The question prompt text. */
    private final String quess;

    /** The list of possible answer strings. */
    private final List<String> answers;

    /**
     * Constructs a Question. If {@code questionText} is blank or
     * {@code answerOptions} is empty, fallback placeholder content
     * is substituted so the UI can still render something sensible.
     *
     * @param questionText  the question prompt
     * @param answerOptions the list of possible answers
     * @param correct       index of the correct answer
     * @param selected      index of the user's selected answer
     */
    public Question(final String questionText,
            final List<String> answerOptions,
            final int correct,
            final int selected) {

        if (questionText.isBlank()) {
            this.quess = BLANK_QUESTION_FALLBACK;
        } else {
            this.quess = questionText;
        }

        if (answerOptions.isEmpty()) {
            this.answers = List.of(EMPTY_ANSWERS_FALLBACK);
        } else {
            this.answers = answerOptions;
        }

        this.correctAns = correct;
        this.selectedAns = selected;
    }

    /**
     * Returns the index of the correct answer.
     *
     * @return the correct-answer index
     */
    public int getCorrectAns() {
        return correctAns;
    }

    /**
     * Returns the index of the user's selected answer.
     *
     * @return the selected-answer index
     */
    public int getSelectedAns() {
        return selectedAns;
    }

    /**
     * Returns the question prompt text.
     *
     * @return the question text
     */
    public String getQuess() {
        return quess;
    }

    /**
     * Returns the list of possible answers.
     *
     * @return the answer list
     */
    public List<String> getAnswers() {
        return answers;
    }

    /**
     * Indicates whether the user's selection matches the correct answer.
     *
     * @return {@code true} if the selected answer equals the correct one
     */
    public boolean isCorrect() {
        return selectedAns == correctAns;
    }

    /**
     * Returns {@code true} when the supplied index is both the correct
     * answer and the answer the user selected.
     *
     * @param ans the answer index to test
     * @return whether this index is correct and was selected
     */
    public boolean choseCorrectAndCorrect(final int ans) {
        return ans == correctAns && ans == selectedAns;
    }

    /**
     * Returns {@code true} when the supplied index is the user's
     * selection but is not the correct answer.
     *
     * @param ans the answer index to test
     * @return whether this index was selected and is incorrect
     */
    public boolean choseIncorrect(final int ans) {
        return ans == selectedAns && ans != correctAns;
    }

    /**
     * Returns {@code true} when the supplied index is the correct
     * answer but the user did not select it.
     *
     * @param ans the answer index to test
     * @return whether this index is correct and was not selected
     */
    public boolean unchosenCorrect(final int ans) {
        return ans == correctAns && ans != selectedAns;
    }
}
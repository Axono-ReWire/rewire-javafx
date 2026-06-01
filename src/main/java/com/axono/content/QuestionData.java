package com.axono.content;

import java.util.Collections;
import java.util.List;

/**
 * Immutable data structure for quiz question with answers and correct answer
 * index. Extracted from XML {@code <question>} elements during content
 * parsing.
 */
public final class QuestionData {

    /** Constraint: quizzes must have exactly this many answer options. */
    private static final int ANSWER_OPTION_COUNT = 4;

    /** Constraint: correct answer index must be at least this value. */
    private static final int MIN_CORRECT_INDEX = 1;

    /** Constraint: correct answer index must not exceed this value. */
    private static final int MAX_CORRECT_INDEX = 4;

    /** The question text/prompt. */
    private final String questionText;

    /** Unmodifiable list of exactly 4 answer options. */
    private final List<String> answerOptions;

    /** 1-based index into answerOptions indicating the correct answer. */
    private final int correctAnswerIndex;

    /** Explanation text providing context for the correct answer. */
    private final String explanation;

    /**
     * Constructs a {@code QuestionData}.
     *
     * @param text        the question text; must not be blank.
     * @param options     exactly 4 answer option strings.
     * @param correctIdx  1-based index (1-4) of the correct answer.
     * @param explain     explanation text; must not be null.
     * @throws IllegalArgumentException if validation fails.
     */
    public QuestionData(final String text,
            final List<String> options,
            final int correctIdx,
            final String explain) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Question text cannot be null or blank");
        }
        if (options == null || options.size() != ANSWER_OPTION_COUNT) {
            throw new IllegalArgumentException(
                    "Must have exactly 4 answer options");
        }
        if (correctIdx < MIN_CORRECT_INDEX
                || correctIdx > MAX_CORRECT_INDEX) {
            throw new IllegalArgumentException(
                    "Correct answer index must be between 1 and 4");
        }
        if (explain == null) {
            throw new IllegalArgumentException(
                    "Explanation cannot be null");
        }

        this.questionText = text;
        this.answerOptions = Collections.unmodifiableList(options);
        this.correctAnswerIndex = correctIdx;
        this.explanation = explain;
    }

    /** @return the question text. */
    public String questionText() {
        return questionText;
    }

    /** @return an unmodifiable list of answer options. */
    public List<String> answerOptions() {
        return answerOptions;
    }

    /** @return the 1-based index of the correct answer. */
    public int correctAnswerIndex() {
        return correctAnswerIndex;
    }

    /** @return the explanation text. */
    public String explanation() {
        return explanation;
    }
}

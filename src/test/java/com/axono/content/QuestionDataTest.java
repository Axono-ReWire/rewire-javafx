package com.axono.content;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class QuestionDataTest {

    /** Valid answer options for question tests. */
    private static final List<String> VALID_OPTIONS =
            List.of("A", "B", "C", "D");
    /** Valid question text constant. */
    private static final String VALID_TEXT = "What is 2 + 2?";
    /** Valid explanation constant. */
    private static final String VALID_EXPLANATION = "Basic arithmetic.";
    /** Valid correct answer index. */
    private static final int VALID_INDEX = 1;
    /** Minimum valid correct answer index. */
    private static final int MIN_VALID_INDEX = 1;
    /** Maximum valid correct answer index. */
    private static final int MAX_VALID_INDEX = 4;
    /** Invalid index below range. */
    private static final int INVALID_INDEX_ZERO = 0;
    /** Invalid index above range. */
    private static final int INVALID_INDEX_FIVE = 5;
    /** Invalid option count too low. */
    private static final int INVALID_OPTION_COUNT_THREE = 3;
    /** Invalid option count too high. */
    private static final int INVALID_OPTION_COUNT_FIVE = 5;
    /** Expected number of answer options. */
    private static final int EXPECTED_OPTION_COUNT = 4;

    @Test
    void validConstructionSucceeds() {
        assertDoesNotThrow(() ->
                new QuestionData(VALID_TEXT, VALID_OPTIONS,
                        VALID_INDEX, VALID_EXPLANATION));
    }

    @Test
    void nullTextThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new QuestionData(null, VALID_OPTIONS,
                        VALID_INDEX, VALID_EXPLANATION));
    }

    @Test
    void blankTextThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new QuestionData("   ", VALID_OPTIONS,
                        VALID_INDEX, VALID_EXPLANATION));
    }

    @Test
    void wrongOptionCountThreeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new QuestionData(VALID_TEXT, List.of("A", "B", "C"),
                        VALID_INDEX, VALID_EXPLANATION));
    }

    @Test
    void wrongOptionCountFiveThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new QuestionData(VALID_TEXT,
                        List.of("A", "B", "C", "D", "E"),
                        VALID_INDEX, VALID_EXPLANATION));
    }

    @Test
    void correctIndexZeroThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new QuestionData(VALID_TEXT, VALID_OPTIONS,
                        INVALID_INDEX_ZERO, VALID_EXPLANATION));
    }

    @Test
    void correctIndexFiveThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                new QuestionData(VALID_TEXT, VALID_OPTIONS,
                        INVALID_INDEX_FIVE, VALID_EXPLANATION));
    }

    @Test
    void correctIndexOneSucceeds() {
        assertDoesNotThrow(() ->
                new QuestionData(VALID_TEXT, VALID_OPTIONS,
                        MIN_VALID_INDEX, VALID_EXPLANATION));
    }

    @Test
    void correctIndexFourSucceeds() {
        assertDoesNotThrow(() ->
                new QuestionData(VALID_TEXT, VALID_OPTIONS,
                        MAX_VALID_INDEX, VALID_EXPLANATION));
    }

    @Test
    void nullExplanationThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new QuestionData(VALID_TEXT, VALID_OPTIONS,
                        VALID_INDEX, null));
    }

    @Test
    void answerOptionsAreImmutable() {
        QuestionData qd = new QuestionData(VALID_TEXT,
                new ArrayList<>(VALID_OPTIONS), VALID_INDEX, VALID_EXPLANATION);
        assertThrows(UnsupportedOperationException.class, () ->
                qd.answerOptions().add("E"));
    }

    @Test
    void gettersReturnConstructedValues() {
        int testIndex = 2;
        QuestionData qd = new QuestionData(VALID_TEXT, VALID_OPTIONS,
                testIndex, VALID_EXPLANATION);
        assertEquals(VALID_TEXT, qd.questionText());
        assertEquals(testIndex, qd.correctAnswerIndex());
        assertEquals(VALID_EXPLANATION, qd.explanation());
        assertEquals(EXPECTED_OPTION_COUNT, qd.answerOptions().size());
    }
}

package com.axono.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class QuizResultTest {

    /** First test user ID. */
    private static final long TEST_USER_ID = 1L;
    /** Second test user ID. */
    private static final long ANOTHER_USER_ID = 2L;
    /** Third test user ID. */
    private static final long YET_ANOTHER_USER_ID = 7L;
    /** First test quiz ID. */
    private static final String TEST_QUIZ_ID = "quiz-1";
    /** Second test quiz ID. */
    private static final String ANOTHER_QUIZ_ID = "quiz-2";
    /** Third test quiz ID. */
    private static final String YET_ANOTHER_QUIZ_ID = "quiz-abc";
    /** Partial test score. */
    private static final int TEST_SCORE = 3;
    /** Perfect score constant. */
    private static final int PERFECT_SCORE = 5;
    /** Maximum score for five-question quiz. */
    private static final int MAX_SCORE_FIVE = 5;
    /** Maximum score for ten-question quiz. */
    private static final int MAX_SCORE_TEN = 10;
    /** Test timestamp constant. */
    private static final String TEST_TIMESTAMP = "2024-01-01";
    /** Test answers JSON constant. */
    private static final String TEST_ANSWERS = "[1,2]";
    /** Alternative answers JSON constant. */
    private static final String ANOTHER_ANSWERS = "[1,3,2,4]";
    /** Another alternative answers JSON constant. */
    private static final String YET_ANOTHER_ANSWERS = "[2,3]";
    /** Single answer JSON constant. */
    private static final String SINGLE_ANSWER = "[4]";
    /** Default empty answers JSON. */
    private static final String DEFAULT_ANSWERS = "[]";

    @Test
    void nullCompletedAtNormalizedToEmpty() {
        QuizResult r = new QuizResult(TEST_USER_ID, TEST_QUIZ_ID,
                TEST_SCORE, MAX_SCORE_FIVE, null, TEST_ANSWERS);
        assertEquals("", r.getCompletedAt());
    }

    @Test
    void nullAnswersJsonNormalizedToDefaultArray() {
        QuizResult r = new QuizResult(TEST_USER_ID, TEST_QUIZ_ID,
                TEST_SCORE, MAX_SCORE_FIVE, TEST_TIMESTAMP, null);
        assertEquals(DEFAULT_ANSWERS, r.getAnswersJson());
    }

    @Test
    void validValuesPreserved() {
        QuizResult r = new QuizResult(ANOTHER_USER_ID, ANOTHER_QUIZ_ID,
                PERFECT_SCORE, MAX_SCORE_FIVE, TEST_TIMESTAMP,
                ANOTHER_ANSWERS);
        assertEquals(TEST_TIMESTAMP, r.getCompletedAt());
        assertEquals(ANOTHER_ANSWERS, r.getAnswersJson());
    }

    @Test
    void fiveArgConstructorSetsEmptyCompletedAt() {
        QuizResult r = new QuizResult(ANOTHER_USER_ID, ANOTHER_QUIZ_ID,
                PERFECT_SCORE, MAX_SCORE_FIVE, SINGLE_ANSWER);
        assertEquals("", r.getCompletedAt());
    }

    @Test
    void fiveArgConstructorPreservesAnswersJson() {
        QuizResult r = new QuizResult(ANOTHER_USER_ID, ANOTHER_QUIZ_ID,
                PERFECT_SCORE, MAX_SCORE_FIVE, YET_ANOTHER_ANSWERS);
        assertEquals(YET_ANOTHER_ANSWERS, r.getAnswersJson());
    }

    @Test
    void gettersReturnCorrectValues() {
        QuizResult r = new QuizResult(YET_ANOTHER_USER_ID,
                YET_ANOTHER_QUIZ_ID, TEST_SCORE, MAX_SCORE_TEN,
                TEST_TIMESTAMP, SINGLE_ANSWER);
        assertEquals(YET_ANOTHER_USER_ID, r.getUserId());
        assertEquals(YET_ANOTHER_QUIZ_ID, r.getPresentationId());
        assertEquals(TEST_SCORE, r.getScore());
        assertEquals(MAX_SCORE_TEN, r.getMaxScore());
    }
}

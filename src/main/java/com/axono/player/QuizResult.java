package com.axono.player;

/**
 * Immutable record-like value object holding a single quiz attempt as it
 * is about to be persisted, or as it was just read back from the
 * {@code quiz_results} table.
 */
public final class QuizResult {

    /** Foreign-key user id. */
    private final long userId;

    /** Stable id of the parent presentation. */
    private final String presentationId;

    /** Number of correctly answered questions. */
    private final int score;

    /** Total number of questions in the quiz. */
    private final int maxScore;

    /**
     * Database-supplied completion timestamp as a string. Set on rows
     * loaded by {@link QuizResultRepository#findByUser(long)} and empty
     * on instances being prepared for insert.
     */
    private final String completedAt;

    /**
     * JSON array of 1-based answer indices selected by the user.
     * Empty array "[]" for legacy results. Used to reconstruct detailed
     * answer review for past quiz attempts.
     */
    private final String answersJson;

    /**
     * Constructs a {@code QuizResult} suitable for inserting with answers.
     *
     * @param attemptUserId         the user attempting the quiz.
     * @param attemptPresentationId the quiz's id.
     * @param attemptScore          the score achieved.
     * @param attemptMaxScore       the maximum possible score.
     * @param answersJsonStr        JSON array of 1-based answer indices.
     */
    public QuizResult(final long attemptUserId,
            final String attemptPresentationId,
            final int attemptScore,
            final int attemptMaxScore,
            final String answersJsonStr) {
        this(attemptUserId, attemptPresentationId,
                attemptScore, attemptMaxScore, "", answersJsonStr);
    }

    /**
     * Constructs a {@code QuizResult} populated from a database row.
     *
     * @param attemptUserId         the user attempting the quiz.
     * @param attemptPresentationId the quiz's id.
     * @param attemptScore          the score achieved.
     * @param attemptMaxScore       the maximum possible score.
     * @param attemptCompletedAt    the {@code completed_at} timestamp
     *                              from the database; may be empty.
     * @param answersJsonStr        JSON array of answer indices from database.
     */
    public QuizResult(final long attemptUserId,
            final String attemptPresentationId,
            final int attemptScore,
            final int attemptMaxScore,
            final String attemptCompletedAt,
            final String answersJsonStr) {
        this.userId = attemptUserId;
        this.presentationId = attemptPresentationId;
        this.score = attemptScore;
        this.maxScore = attemptMaxScore;
        this.completedAt = attemptCompletedAt == null
                ? "" : attemptCompletedAt;
        this.answersJson = answersJsonStr == null ? "[]" : answersJsonStr;
    }

    /** @return the foreign-key user id. */
    public long getUserId() {
        return userId;
    }

    /** @return the presentation id. */
    public String getPresentationId() {
        return presentationId;
    }

    /** @return the score achieved. */
    public int getScore() {
        return score;
    }

    /** @return the maximum possible score. */
    public int getMaxScore() {
        return maxScore;
    }

    /** @return the completed-at timestamp string, or empty. */
    public String getCompletedAt() {
        return completedAt;
    }

    /** @return the JSON array of answer indices, or "[]" if none stored. */
    public String getAnswersJson() {
        return answersJson;
    }
}

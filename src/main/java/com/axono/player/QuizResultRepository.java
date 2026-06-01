package com.axono.player;

import com.axono.database.Database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data-access object for the {@code quiz_results} table. Every public
 * method opens a fresh {@link Connection} via {@link Database#open()} and
 * closes it (along with statements and result sets) using
 * try-with-resources, so no leaks are possible.
 */
public final class QuizResultRepository {

    /** PreparedStatement column index for score. */
    private static final int COL_SCORE = 3;

    /** PreparedStatement column index for max_score. */
    private static final int COL_MAX_SCORE = 4;

    /** PreparedStatement column index for answers_json. */
    private static final int COL_ANSWERS = 5;

    /** Insert SQL with a placeholder per column. */
    private static final String INSERT_SQL =
            "INSERT INTO quiz_results "
                    + "(user_id, presentation_id, score, "
                    + "max_score, answers_json) "
                    + "VALUES (?, ?, ?, ?, ?)";

    /** Select SQL for a user's history, newest attempts first. */
    private static final String FIND_BY_USER_SQL =
            "SELECT user_id, presentation_id, score, max_score, "
                    + "completed_at, answers_json "
                    + "FROM quiz_results WHERE user_id = ? "
                    + "ORDER BY completed_at DESC, id DESC";

    /**
     * JDBC URL used by this repository; {@code null} means delegate to
     * {@link Database#open()} (production behaviour).
     */
    private final String jdbcUrl;

    /** Default constructor; opens connections via {@link Database#open()}. */
    public QuizResultRepository() {
        this.jdbcUrl = null;
    }

    /**
     * Constructs a repository that opens connections to the given JDBC URL.
     * Intended for use with temporary or in-memory databases.
     *
     * @param url the JDBC URL to target.
     */
    public QuizResultRepository(final String url) {
        this.jdbcUrl = url;
    }

    /**
     * Opens a JDBC connection — delegates to {@link Database#open()} in
     * production or to {@link DriverManager} when a custom URL was supplied.
     *
     * @return a fresh connection.
     * @throws SQLException if the connection cannot be established.
     */
    private Connection openConnection() throws SQLException {
        if (jdbcUrl == null) {
            return Database.open();
        }
        return DriverManager.getConnection(jdbcUrl);
    }

    /**
     * Persists a single quiz attempt.
     *
     * @param result the result to insert; all fields are required.
     * @throws SQLException if the insert fails.
     */
    public void save(final QuizResult result) throws SQLException {
        try (Connection conn = openConnection();
                PreparedStatement ps =
                        conn.prepareStatement(INSERT_SQL)) {
            ps.setLong(1, result.getUserId());
            ps.setString(2, result.getPresentationId());
            ps.setInt(COL_SCORE, result.getScore());
            ps.setInt(COL_MAX_SCORE, result.getMaxScore());
            ps.setString(COL_ANSWERS, result.getAnswersJson());
            ps.executeUpdate();
        }
    }

    /**
     * Returns every quiz attempt by the given user, newest first.
     *
     * @param userId the foreign-key user id.
     * @return an immutable list of attempts; empty if none exist.
     * @throws SQLException if the query fails.
     */
    public List<QuizResult> findByUser(final long userId)
            throws SQLException {
        List<QuizResult> out = new ArrayList<>();
        try (Connection conn = openConnection();
                PreparedStatement ps =
                        conn.prepareStatement(FIND_BY_USER_SQL)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String answersJson = rs.getString("answers_json");
                    out.add(new QuizResult(
                            rs.getLong("user_id"),
                            rs.getString("presentation_id"),
                            rs.getInt("score"),
                            rs.getInt("max_score"),
                            rs.getString("completed_at"),
                            answersJson));
                }
            }
        }
        return Collections.unmodifiableList(out);
    }
}

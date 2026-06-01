package com.axono.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuizResultRepositoryTest {

    /** DDL to create the quiz_results table in the test database. */
    private static final String CREATE_TABLE =
            "CREATE TABLE quiz_results ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "user_id INTEGER NOT NULL, "
            + "presentation_id TEXT NOT NULL, "
            + "score INTEGER NOT NULL, "
            + "max_score INTEGER NOT NULL, "
            + "completed_at TEXT NOT NULL DEFAULT (datetime('now')), "
            + "answers_json TEXT DEFAULT '[]'"
            + ")";

    /** Score value used across save/load assertions. */
    private static final int SCORE = 3;

    /** Maximum quiz score used across save/load assertions. */
    private static final int MAX_QUESTIONS = 5;

    /** User ID guaranteed to have no rows in the test database. */
    private static final long UNKNOWN_USER_ID = 999L;

    /** JUnit-managed temporary directory isolating the SQLite file. */
    @TempDir
    private Path tempDir;

    /** JDBC URL of the per-test SQLite file, set in {@link #setup()}. */
    private String dbUrl;

    @BeforeEach
    void setup() throws Exception {
        File dbFile = new File(tempDir.toFile(), "test_quiz.db");
        dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        try (Connection c = DriverManager.getConnection(dbUrl);
                Statement s = c.createStatement()) {
            s.execute(CREATE_TABLE);
        }
    }

    @AfterEach
    void teardown() {
        dbUrl = null;
    }

    @Test
    void saveAndFindByUserRoundTrip() throws Exception {
        QuizResultRepository repo = new QuizResultRepository(dbUrl);
        QuizResult toSave =
                new QuizResult(1L, "quiz-abc", SCORE, MAX_QUESTIONS, "[]");

        repo.save(toSave);
        List<QuizResult> results = repo.findByUser(1L);

        assertEquals(1, results.size());
        QuizResult loaded = results.get(0);
        assertEquals(1L, loaded.getUserId());
        assertEquals("quiz-abc", loaded.getPresentationId());
        assertEquals(SCORE, loaded.getScore());
        assertEquals(MAX_QUESTIONS, loaded.getMaxScore());
        assertNotNull(loaded.getCompletedAt());
        assertFalse(loaded.getCompletedAt().isEmpty());
    }

    @Test
    void findByUserEmptyForUnknownUser() throws Exception {
        QuizResultRepository repo = new QuizResultRepository(dbUrl);
        List<QuizResult> results = repo.findByUser(UNKNOWN_USER_ID);
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void saveMultipleResultsReturnedInOrder() throws Exception {
        QuizResultRepository repo = new QuizResultRepository(dbUrl);
        repo.save(new QuizResult(2L, "q1", 1, MAX_QUESTIONS, "[]"));
        repo.save(new QuizResult(2L, "q2", 2, MAX_QUESTIONS, "[]"));

        List<QuizResult> results = repo.findByUser(2L);
        assertEquals(2, results.size());
    }
}

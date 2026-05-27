package com.axono.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Statement;
import java.util.Map;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Unit tests for the {@link DatabaseHelper} class.
 * Validates low-level generic database actions, dynamic row mapping
 * capabilities,
 * and primary key query behaviors using an isolated in-memory relational
 * driver.
 */
class DatabaseHelperTest {

    /** The generic database helper utility instance under test. */
    private DatabaseHelper databaseHelper;

    /** The underlying SQL connection managing the in-memory testing context. */
    private Connection testConnection;

    /**
     * Sets up an in-memory SQLite environment, provisions mock schemas,
     * injects a seed record, and reflects the test connection into the helper
     * utility
     * before each test execution to isolate state changes.
     */
    @BeforeEach
    void setup() throws Exception {
        // Establish an isolated SQLite in-memory instance
        String testDbUrl = "jdbc:sqlite::memory:";
        testConnection = DriverManager.getConnection(testDbUrl);

        // Generate schemas and seed required test profiles
        try (Statement statement = testConnection.createStatement()) {
            statement.execute(
                    "CREATE TABLE user (id INTERGER PRIMARY KEY, first_name TEXT, last_name TEXT, university_id INTEGER)");
            statement.execute(
                    "INSERT INTO user (id, first_name, last_name, university_id) VALUES (1, 'Joe', 'Bloggs', 1)");
        }

        databaseHelper = new DatabaseHelper();

        // Force entry via Reflection to bypass encapsulation and override the instance
        // connection
        Field connectionField = DatabaseHelper.class.getDeclaredField("connection");
        connectionField.setAccessible(true);

        // Safely clean up any default connection pool setups
        Connection autoConnection = (Connection) connectionField.get(databaseHelper);
        if (autoConnection != null) {
            autoConnection.close();
        }

        // Swap production connection pointers with in-memory testing driver stub
        connectionField.set(databaseHelper, testConnection);
    }

    /**
     * Ensures proper teardown of system contexts, freeing up file descriptors
     * and closing active relational connections immediately following execution.
     */
    @AfterEach
    void tearDown() throws Exception {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    /**
     * A basic sanity check to ensure the JUnit 5 test runner
     * is configured and executing properly.
     */
    @Test
    void sampleTest() {
        assertEquals(4, 2 + 2);
    }

    /**
     * Verifies that looking up an existing record ID dynamically extracts the table
     * row fields and populates them correctly within a key-value data map.
     */
    @Test
    void testGetByIdSuccess() throws Exception {
        Map<String, Object> result = databaseHelper.getById("user", 1);

        // Validate that fields map to their structural column schemas
        assertNotNull(result);
        assertEquals("Joe", result.get("first_name"));
        assertEquals("Bloggs", result.get("last_name"));
        assertEquals(1, result.get("university_id"));
    }

    /**
     * Verifies that looking up a non-existent primary key id value safely returns
     * an initialized but completely empty map representation instead of returning
     * null.
     */
    @Test
    void testGetByIdNotFound() throws Exception {
        Map<String, Object> result = databaseHelper.getById("user", 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

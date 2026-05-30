package com.axono.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link UserService} class.
 * Validates database query operations, user profile data extraction,
 * and relational lookup logic using an isolated in-memory database instance.
 */
class UserServiceTest {

    /** Sample university ID for test data. */
    private static final int SAMPLE_UNIVERSITY_ID = 1;

    /** Sample user ID for test data. */
    private static final int SAMPLE_USER_ID = 1;

    /** Non-existent user ID for test data. */
    private static final int NONEXISTENT_USER_ID = 10;

    /** The database abstraction layer used to run queries. */
    private DatabaseHelper databaseHelper;

    /** The target service instance under test. */
    private UserService userService;

    /** The underlying SQL connection managing the in-memory testing context. */
    private Connection testConnection;

    /**
     * Sets up an in-memory SQLite environment, provisions mock tables,
     * injects seed data, and reflects the connection into the service
     * architecture before each test execution to isolate operations.
     */
    @BeforeEach
    void setup() throws Exception {
        String testDbUrl = "jdbc:sqlite::memory:";
        testConnection = DriverManager.getConnection(testDbUrl);

        try (Statement statement = testConnection.createStatement()) {
            statement.execute(
                    "CREATE TABLE user (id INTERGER PRIMARY KEY, first_name "
                    + "TEXT, last_name TEXT, university_id INTEGER)");
            statement.execute(
                    "CREATE TABLE university (id INTERGER PRIMARY KEY, name "
                    + "TEXT)");

            statement.execute(
                    "INSERT INTO user (id, first_name, last_name, "
                    + "university_id) "
                    + "VALUES (1, 'Joe', 'Bloggs', 1)");
            statement.execute(
                    "INSERT INTO university (id, name) VALUES (1, "
                    + "'University of York')");
        }

        databaseHelper = new DatabaseHelper();

        Field connectionField = DatabaseHelper.class
                .getDeclaredField("connection");
        connectionField.setAccessible(true);

        Connection autoConnection = (Connection) connectionField
                .get(databaseHelper);
        if (autoConnection != null) {
            autoConnection.close();
        }

        connectionField.set(databaseHelper, testConnection);

        userService = new UserService(databaseHelper);
    }

    /**
     * Ensures proper teardown of system contexts, freeing up file descriptors
     * and closing active relational connections immediately following
     * execution.
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
     * Verifies that the service retrieves and joins string parts accurately
     * into a single formatted value when looking up an existing record ID.
     */
    @Test
    void testGetUserFullNameFound() throws Exception {
        String fullName = userService.getUserFullName(SAMPLE_USER_ID);
        assertEquals("Joe Bloggs", fullName);
    }

    /**
     * Verifies string generation behaviors when looking up an unmapped
     * or non-existent primary key id value.
     */
    @Test
    void testGetUserFullNameNotFound() throws Exception {
        String fullName = userService.getUserFullName(NONEXISTENT_USER_ID);
        assertEquals("null null", fullName);
    }

    /**
     * Verifies relational data map extractions function correctly, confirming
     * mapped values contain accurate relational property links.
     */
    @Test
    void testGetUserUniversitySuccess() throws Exception {
        Map<String, Object> university = userService.getUserUniversity(
                SAMPLE_USER_ID);

        assertNotNull(university);
        assertEquals("University of York", university.get("name"));
    }

}


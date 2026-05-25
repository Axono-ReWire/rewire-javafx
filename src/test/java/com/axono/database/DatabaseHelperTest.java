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

class DatabaseHelperTest {

    private DatabaseHelper databaseHelper;
    private Connection testConnection;

    @BeforeEach
    void setup() throws Exception {
        String testDbUrl = "jdbc:sqlite::memory:";
        testConnection = DriverManager.getConnection(testDbUrl);

        try (Statement statement = testConnection.createStatement()) {
            statement.execute(
                    "CREATE TABLE user (id INTERGER PRIMARY KEY, first_name TEXT, last_name TEXT, university_id INTEGER)");
            statement.execute(
                    "INSERT INTO user (id, first_name, last_name, university_id) VALUES (1, 'Joe', 'Bloggs', 1)");
        }

        databaseHelper = new DatabaseHelper();

        Field connectionField = DatabaseHelper.class.getDeclaredField("connection");
        connectionField.setAccessible(true);

        Connection autoConnection = (Connection) connectionField.get(databaseHelper);
        if (autoConnection != null) {
            autoConnection.close();
        }
        connectionField.set(databaseHelper, testConnection);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @Test
    void sampleTest() {
        assertEquals(4, 2 + 2);
    }

    @Test
    void testGetByIdSuccess() throws Exception {
        Map<String, Object> result = databaseHelper.getById("user", 1);

        assertNotNull(result);
        assertEquals("Joe", result.get("first_name"));
        assertEquals("Bloggs", result.get("last_name"));
        assertEquals(1, result.get("university_id"));
    }

    @Test
    void testGetByIdNotFound() throws Exception {
        Map<String, Object> result = databaseHelper.getById("user", 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
